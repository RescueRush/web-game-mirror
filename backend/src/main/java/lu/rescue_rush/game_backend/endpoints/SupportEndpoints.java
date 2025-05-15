package lu.rescue_rush.game_backend.endpoints;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lu.pcy113.pclib.PCUtils;

import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.db.data.support.SupportFormData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.support.SupportFormTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.integrations.discord.DiscordSender;
import lu.rescue_rush.game_backend.integrations.discord.embeds.support.EmbedSupportForm;
import lu.rescue_rush.game_backend.integrations.email.EmailSender;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.types.SupportTypes.EmailFormRequest;
import lu.rescue_rush.game_backend.types.SupportTypes.EmailFormResponse;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.AllowAnonymous;

@CrossOrigin
@RestController
@RequestMapping(value = "/support")
public class SupportEndpoints {

	private static final Logger LOGGER = Logger.getLogger(SupportEndpoints.class.getName());

	@Autowired
	private UserTable USERS;

	@Autowired
	private SupportFormTable SUPPORT_EMAIL_FORMS;

	@Autowired
	private EmailSender EMAIL_SENDER;

	@Autowired
	private DiscordSender DISCORD_SENDER;

	@Autowired
	private EmbedSupportForm EMBED_SUPPORT_FORM;

	/**
	 * Takes in an {@link EmailFormRequest} and creates a support ticket.
	 * 
	 * @param token
	 * @param formRequest
	 * 
	 * @return {@link EmailFormResponse}
	 * 
	 * @throws {@link HttpStatus#NOT_FOUND} User not found. (iff token is provided
	 *                but invalid)
	 * @throws {@link HttpStatus#INTERNAL_SERVER_ERROR} Couldn't insert/reload
	 *                Support Ticket into database.
	 * @throws {@link HttpStatus#INTERNAL_SERVER_ERROR} Couldn't send email.
	 */

	@ExecutionTrack
	@AllowAnonymous
	@PostMapping(value = "/form/submit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> submitEmailForm(@CookieValue(value = "token", required = false) @Nullable String token, @RequestBody EmailFormRequest formRequest, HttpServletRequest request) {
		SpringUtils.badRequest(!PCUtils.validEmail(formRequest.email), "Invalid email.");

		String email = formRequest.email, name = formRequest.name;
		int userId;

		if (token != null) {
			UserData ud = USERS.requestSafe_byToken(token);
			userId = ud.getId();
		} else {
			email = formRequest.getEmail();
			userId = -1;
		}

		final SupportFormData sefd = SUPPORT_EMAIL_FORMS.requestSafe_insertAndReload(new SupportFormData(userId, name, email, formRequest.message, formRequest.lang));

		// we copy the remote address bc: java.lang.IllegalStateException: The request
		// object has been recycled and is no longer associated with this facade
		final String remoteAddr = request.getRemoteAddr();

		sefd.setSource(remoteAddr);

		// @format:off
		DISCORD_SENDER.prepareSendEmbed().catch_(SpringUtils.catch_(LOGGER, "Couldn't send embed.")).thenConsume((v) -> {
			sefd.setDiscordConfirmed(true);
			SUPPORT_EMAIL_FORMS.updateSupportEmailFormData(sefd);
		}).runAsync(EMBED_SUPPORT_FORM.build(sefd));

		EMAIL_SENDER.prepareSupportFormEmail().catch_(SpringUtils.catch_(LOGGER, "Couldn't send email.")).thenConsume((v) -> {
			sefd.setEmailConfirmed(v);
			SUPPORT_EMAIL_FORMS.updateSupportEmailFormData(sefd);
		}).runAsync(sefd);
		// @format:on

		return ResponseEntity.accepted().body(new EmailFormResponse(sefd.getId()));
	}

}
