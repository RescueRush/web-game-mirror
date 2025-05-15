package lu.rescue_rush.game_backend.endpoints;

import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import lu.pcy113.pclib.PCUtils;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.data.Locales;
import lu.rescue_rush.game_backend.db.data.monitor.interaction.UserPassResetData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionReasonData;
import lu.rescue_rush.game_backend.db.tables.monitor.user.UserPassResetTable;
import lu.rescue_rush.game_backend.db.tables.user.UserSanctionReasonTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.game.GameStats_FilteredLeaderboard_View;
import lu.rescue_rush.game_backend.integrations.discord.DiscordSender;
import lu.rescue_rush.game_backend.integrations.discord.embeds.user.EmbedUserLogon;
import lu.rescue_rush.game_backend.integrations.email.EmailSender;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.types.UserTypes;
import lu.rescue_rush.game_backend.types.UserTypes.BannedResponse;
import lu.rescue_rush.game_backend.types.UserTypes.ForgotPassRequest;
import lu.rescue_rush.game_backend.types.UserTypes.LeaderboardEntry;
import lu.rescue_rush.game_backend.types.UserTypes.LoginRequest;
import lu.rescue_rush.game_backend.types.UserTypes.LoginResponse;
import lu.rescue_rush.game_backend.types.UserTypes.LogonRequest;
import lu.rescue_rush.game_backend.types.UserTypes.LogonResponse;
import lu.rescue_rush.game_backend.types.UserTypes.ResetPassRequest;
import lu.rescue_rush.game_backend.types.UserTypes.UpdateEmailRequest;
import lu.rescue_rush.game_backend.types.UserTypes.UpdateEmailResponse;
import lu.rescue_rush.game_backend.types.UserTypes.UpdateNameRequest;
import lu.rescue_rush.game_backend.types.UserTypes.UpdatePassRequest;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.AllowAnonymous;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UserEndpoints {

	private static final Logger LOGGER = Logger.getLogger(UserEndpoints.class.getName());

	@Autowired
	private UserTable USERS;

	@Autowired
	private GameStats_FilteredLeaderboard_View GAME_STATS_FILTERED_LEADERBOARD;

	@Autowired
	private UserSanctionReasonTable USER_SANCTION_REASONS;

	@Autowired
	private DiscordSender DISCORD_SENDER;

	@Autowired
	private EmbedUserLogon EMBED_USER_LOGON;

	@Autowired
	private EmailSender EMAIL_SENDER;

	@Autowired
	private UserPassResetTable PASS_RESETS;

	@Autowired
	private LocaleResolver localeResolver;

	/**
	 * Returns a new token and sets the cookie via the HTTP response.
	 * 
	 * @param login
	 * @param response
	 * @param request
	 * 
	 * @return {@link LoginResponse} or {@link BannedResponse}
	 * 
	 * @throws {@link HttpStatus#NOT_FOUND} User not found.
	 * @throws {@link HttpStatus#FORBIDDEN} {@link BannedResponse}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> login(@RequestBody LoginRequest login, HttpServletRequest request, HttpServletResponse response) {
		final UserData ud = USERS.requestSafe_byLogin(login.user, login.pass, () -> InteractionMonitor.pushDeniedDesc("Invalid credentials: '" + login.user + "', '" + login.pass + "'"));

		ud.loadSanctions();
		if (ud.hasSanctions()) {
			ud.loadSanctionReasonDatas();
			if (ud.isBanned()) {
				ud.updateLogin().updateSanctionStatus();
				ud.push();

				InteractionMonitor.pushDeniedDesc("User is banned.");

				return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
						.body(UserTypes.buildBannedResponse(UserTypes.buildSanctionReasonResponse(USER_SANCTION_REASONS.byKey(UserSanctionReasonData.KEY_BAN))));
			}
		}

		final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		request.getSession(true); // force session creation

		InteractionMonitor.pushAccepted();

		final String newToken = ud.genNewToken();

		ud.updateLogin();
		ud.push();

		response.addCookie(SpringUtils.newTokenCookie(newToken));

		return ResponseEntity.accepted().body(new LoginResponse(newToken));
	}

	@ExecutionTrack
	@PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> logout(HttpServletResponse response) {
		final UserData ud = SpringUtils.needContextUser();
		InteractionMonitor.pushAccepted();

		response.addCookie(SpringUtils.newTokenCookie(null));

		SecurityContextHolder.clearContext();

		return ResponseEntity.accepted().build();
	}

	/**
	 * Creates a new account and returns a new token and sets the cookie via the
	 * HTTP response.
	 * 
	 * @param logon
	 * @param response
	 * @param request
	 * 
	 * @return {@link LogonResponse}
	 * 
	 * @throws {@link HttpStatus#BAD_REQUEST} Invalid email address.
	 * @throws {@link HttpStatus#BAD_REQUEST} Empty password.
	 * @throws {@link HttpStatus#FORBIDDEN} User already exists.
	 * @throws {@link HttpStatus#FORBIDDEN} User already exists with this email.
	 * @throws {@link HttpStatus#INTERNAL_SERVER_ERROR} Couldn't insert user.
	 */
	@ExecutionTrack
	@AllowAnonymous
	@PostMapping(value = "/logon", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> logon(@RequestBody LogonRequest logon, HttpServletResponse response, HttpServletRequest request) {
		SpringUtils.forbidden(USERS.nameExists(logon.user), "User already exists.", () -> InteractionMonitor.pushDeniedDesc("User already exists: '" + logon.user + "'"));
		SpringUtils.badRequest(!PCUtils.validEmail(logon.email), "Invalid email address.", () -> InteractionMonitor.pushDeniedDesc("Invalid email address: '" + logon.email + "'"));
		SpringUtils.badRequest(!SpringUtils.validPass(logon.pass), "No password provided.", () -> InteractionMonitor.pushDeniedDesc("Invalid password"));
		SpringUtils.forbidden(USERS.emailExists(logon.email), "User already exists with this email.", () -> InteractionMonitor.pushDeniedDesc("Email already exists: '" + logon.email + "'"));
		SpringUtils.badRequest(Language.byCode(logon.lang) == null, "Invalid language.", () -> InteractionMonitor.pushDeniedDesc("Invalid language: " + logon.lang));

		final UserData ud = USERS.insertAndReload(new UserData(logon.email, logon.user, logon.pass, Language.byCode(logon.lang)))
				.catch_(SpringUtils.catch_(LOGGER, "Error when creating new user for '" + logon.email + "'")).run();

		SpringUtils.internalServerError(ud == null, "Error when creating new user.", () -> InteractionMonitor.pushDeniedDesc("Error when creating new user"));

		SpringUtils.setContextUser(ud);

		InteractionMonitor.pushAccepted();

		final String newToken = ud.genNewToken();

		ud.updateLogin();
		ud.push();

		response.addCookie(SpringUtils.newTokenCookie(newToken));

		DISCORD_SENDER.prepareSendEmbed().catch_(SpringUtils.catch_(LOGGER, e -> "Couldn't send embed: " + e.getClass().getName() + " (" + e.getMessage() + ")"))
				.runAsync(EMBED_USER_LOGON.build(ud, request.getRemoteAddr()));

		EMAIL_SENDER.prepareUserCreatedEmail().catch_(SpringUtils.catch_(LOGGER, e -> "Couldn't send email: " + e.getClass().getName() + " (" + e.getMessage() + ")")).runAsync(ud);

		return ResponseEntity.accepted().body(new LogonResponse(newToken));
	}

	/**
	 * Returns {@link HttpStatus#ACCEPTED} if the token is valid and extends the
	 * client's cookie lifetime.
	 * 
	 * @param token
	 * @param response
	 * @param request
	 * 
	 * @return {@link HttpStatus#ACCEPTED}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@RequestMapping(value = "/check-auth")
	public ResponseEntity<?> tokenValid(@CookieValue(required = false) String token, HttpServletResponse response) {
		if (SpringUtils.isAnonymous()) {
			InteractionMonitor.pushAccepted();

			return ResponseEntity.ok().build();
		}

		final UserData ud = SpringUtils.needContextUser();

		SpringUtils.badRequest(ud == null, "Invalid token.", () -> InteractionMonitor.pushDeniedDesc("Invalid token"));
		// SpringUtils.badRequest(SpringUtils.validString(token), "Invalid token.", ()
		// -> InteractionMonitor.pushDeniedDesc("Invalid token"));

		response.addCookie(SpringUtils.newCookie("token", token, true, 3600));

		InteractionMonitor.pushAccepted();

		return ResponseEntity.accepted().build();
	}

	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "/get-lang")
	public ResponseEntity<?> getLang(HttpServletResponse response) {
		if (SpringUtils.isAnonymous()) {
			final HashMap<String, Object> principal = (HashMap<String, Object>) SpringUtils.getContextPrincipal();

			return ResponseEntity.accepted().body(PCUtils.hashMap("lang", SpringUtils.getContextLocale().getLanguage()));
		}

		final UserData ud = SpringUtils.needContextUser();

		return ResponseEntity.accepted().body(PCUtils.hashMap("lang", SpringUtils.getContextLocale().getLanguage()));
	}

	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "/set-lang")
	public ResponseEntity<?> setLang(@RequestParam String lang, HttpServletRequest request, HttpServletResponse response) {
		final Locale locale = Locales.byCode(lang);
		SpringUtils.badRequest(locale == null, "Invalid language.", () -> InteractionMonitor.pushDeniedDesc("Invalid language: " + lang));

		request.getSession(true).setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);

		if (SpringUtils.isAnonymous()) {
			final HashMap<String, Object> principal = (HashMap<String, Object>) SpringUtils.getContextPrincipal();

			principal.put("lang", locale);
			localeResolver.setLocale(request, response, locale);

			return ResponseEntity.accepted().body(PCUtils.hashMap("lang", ((Locale) principal.get("lang")).getLanguage()));
		}

		final UserData ud = SpringUtils.needContextUser();

		ud.setLocale(locale);
		localeResolver.setLocale(request, response, locale);

		return ResponseEntity.accepted().body(PCUtils.hashMap("lang", ud.getLanguage().getCode()));
	}

	/**
	 * Returns the currently logged in user's position in the leaderboard.
	 * 
	 * @param token
	 * @param response
	 * @param request
	 * 
	 * @return {@link LeaderboardEntry}
	 */

	@ExecutionTrack
	@GetMapping(value = "/leaderboard/single", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> leaderboard_single() {
		final UserData ud = SpringUtils.needContextUser();

		return ResponseEntity.accepted().body(GAME_STATS_FILTERED_LEADERBOARD.leaderboard(ud));
	}

	/**
	 * Returns a page of the leaderboard.
	 * 
	 * @param token
	 * @param page
	 * @param response
	 * @param request
	 * 
	 * @return {@link List<LeaderboardEntry>}
	 * 
	 * @throws {@link HttpStatus#NOT_FOUND} User not found.
	 */

	@ExecutionTrack
	@GetMapping(value = "/leaderboard/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> leaderboard_list(@RequestParam int page, HttpServletResponse response, HttpServletRequest request) {
		final UserData ud = SpringUtils.needContextUser();

		return ResponseEntity.accepted().body(GAME_STATS_FILTERED_LEADERBOARD.leaderboard(page));
	}

	/**
	 * Updates the name of given user
	 * 
	 * @param token
	 * @param request
	 * @param response
	 * @param request2
	 * 
	 * @return {@link UpdateNameResponse}
	 * 
	 * @throws {@link HttpStatus#FORBIDDEN} Incorrect password.
	 * @throws {@link HttpStatus#NOT_FOUND} User not found.
	 * @throws {@link HttpStatus#FORBIDDEN} Name already used.
	 */

	@ExecutionTrack
	@PutMapping(value = "/profile/update_name")
	public ResponseEntity<?> updateName(@RequestBody UpdateNameRequest request, HttpServletResponse response, HttpServletRequest request2) {
		final UserData ud = SpringUtils.needContextUser();

		SpringUtils.forbidden(!ud.getPass().equals(UserData.hashPass(request.pass)), "Incorrect password.", () -> InteractionMonitor.pushDeniedDesc("Invalid password."));
		SpringUtils.badRequest(!SpringUtils.validString(request.newName), "No name provided.", () -> InteractionMonitor.pushDeniedDesc("No name provided"));
		SpringUtils.forbidden(USERS.nameExists(request.newName), "Name already used.", () -> InteractionMonitor.pushDeniedDesc("Name already used: " + request.newName));

		ud.updateName(request.newName);
		ud.push();

		InteractionMonitor.pushAccepted();

		return ResponseEntity.accepted().body(PCUtils.hashMap("newName", ud.getName()));
	}

	/**
	 * Updates the password of given user.
	 * 
	 * @param request
	 * @param response
	 * @param request2
	 * 
	 * @return {@link UpdateNameResponse}
	 * @throws MessagingException
	 * 
	 * @throws {@link             HttpStatus#FORBIDDEN} Incorrect password.
	 * @throws {@link             HttpStatus#BAD_REQUEST} No password provided.
	 */

	@ExecutionTrack
	@PutMapping(value = "/profile/update_pass")
	public ResponseEntity<?> updatePass(@RequestBody UpdatePassRequest request, HttpServletResponse response, HttpServletRequest request2) throws MessagingException {
		final UserData ud = SpringUtils.needContextUser();

		SpringUtils.forbidden(!ud.getPass().equals(UserData.hashPass(request.currentPass)), "Incorrect password.", () -> InteractionMonitor.pushDeniedDesc("Incorrect password."));
		SpringUtils.badRequest(!SpringUtils.validPass(request.newPass), "No password provided.", () -> InteractionMonitor.pushDeniedDesc("No password provided."));

		ud.updatePass(UserData.hashPass(request.newPass));
		ud.updateLogin();
		final String newToken = ud.genNewToken();

		InteractionMonitor.pushAccepted();

		EMAIL_SENDER.preparePassResettedEmail().runAsync(ud);

		ud.push();

		response.addCookie(SpringUtils.newTokenCookie(newToken));

		return ResponseEntity.accepted().body(new LoginResponse(newToken));
	}

	/**
	 * Updates the email of given user
	 * 
	 * @param request
	 * @param response
	 * @param request2
	 * 
	 * @return {@link UpdateEmailResponse}
	 * 
	 * @throws {@link HttpStatus#FORBIDDEN} Incorrect password.
	 * @throws {@link HttpStatus#BAD_REQUEST} Invalid email.
	 */

	@ExecutionTrack
	@PutMapping(value = "/profile/update_email")
	public ResponseEntity<?> updateEmail(@RequestBody UpdateEmailRequest request, HttpServletResponse response, HttpServletRequest request2) {
		final UserData ud = SpringUtils.needContextUser();

		SpringUtils.forbidden(!ud.getPass().equals(UserData.hashPass(request.pass)), "Incorrect password.", () -> InteractionMonitor.pushDeniedDesc("Incorrect password."));
		SpringUtils.badRequest(!PCUtils.validEmail(request.newEmail), "Invalid email.", () -> InteractionMonitor.pushDeniedDesc("Invalid email: " + request.newEmail));
		SpringUtils.forbidden(USERS.emailExists(request.newEmail), "Email already used.", () -> InteractionMonitor.pushDeniedDesc("Email already used: " + request.newEmail));

		final String oldEmail = ud.getEmail();

		ud.updateEmail(request.newEmail);
		ud.updateLogin();
		final String newToken = ud.genNewToken();
		ud.push();

		InteractionMonitor.pushAccepted();

		EMAIL_SENDER.prepareUserEmailResetEmail().runAsync(ud);
		EMAIL_SENDER.prepareUserEmailResetEmail().runAsync(new UserData(oldEmail, ud.getLanguage()));

		response.addCookie(SpringUtils.newTokenCookie(newToken));

		return ResponseEntity.accepted().body(new UpdateEmailResponse(ud.getEmail(), newToken));
	}

	@ExecutionTrack

	@PostMapping(value = "/profile/request-pass-reset")
	public ResponseEntity<?> requestPassReset(@RequestBody ResetPassRequest request, HttpServletResponse response) throws MessagingException {
		if (!USERS.emailExists(request.email)) {
			InteractionMonitor.pushDeniedDesc("Email doesn't exist.");
			return ResponseEntity.accepted().build();
		}
		UserData target = USERS.byEmail(request.email);

		if (target != null) {
			UserPassResetData uprd = new UserPassResetData(target);
			uprd.genResetToken();

			PASS_RESETS.insertAndReload(uprd).runAsync();
			EMAIL_SENDER.preparePassResetEmail().runAsync(uprd);
		}
		return ResponseEntity.accepted().build();
	}

	@ExecutionTrack

	@PostMapping(value = "/profile/validate-pass-request")
	public ResponseEntity<?> validatePassReset(@RequestBody ForgotPassRequest request, HttpServletResponse response) {
		UserPassResetData uprd = PASS_RESETS.requestSafe_byToken(request.token);
		SpringUtils.badRequest(!uprd.isTokenValid(request.token), "Unvalid token.");

		UserData ud = uprd.loadUser();
		ud.updatePass(UserData.hashPass(request.newPass));

		uprd.setToken_used(true);

		PASS_RESETS.update(uprd).run();

		// log out all devices
		ud.genNewToken();
		ud.push();

		EMAIL_SENDER.preparePassResettedEmail().runAsync(ud);
		// require separate login
		response.addCookie(SpringUtils.newTokenCookie(null));

		return ResponseEntity.accepted().build();
	}
}