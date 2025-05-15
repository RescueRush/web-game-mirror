package lu.rescue_rush.game_backend.endpoints.game;

import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.data.MaterialCard;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.AllowAnonymous;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@CrossOrigin
@RestController
@RequestMapping(value = "/game/translation/")
public class GameTranslationEndpoints {

	private Logger LOGGER = Logger.getLogger(GameTranslationEndpoints.class.getName());

	/**
	 * Returns the {@link MaterialCard MaterialCards'}
	 * {@link MaterialCard#forLocale(Locale) name} for the current locale.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} Invalid language.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED} : {@link HashMap<Integer, String>}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "cards", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> cards(HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();
		final Locale lang = SpringUtils.getContextLocale();
		SpringUtils.badRequest(lang == null, "Invalid language.");

		/*
		 * if (auth instanceof AnonymousAuthenticationToken) { final HashMap<String,
		 * Object> principal = (HashMap<String, Object>)
		 * SpringUtils.getContextPrincipal(); lang = (Locale)
		 * principal.getOrDefault("locale", Locales.LUXEMBOURISH); } else { final
		 * UserData ud = SpringUtils.needContextUser(); lang = ud.getLocale(); }
		 */

		InteractionMonitor.pushAcceptedDesc("Locale: " + lang);

		final HashMap<Integer, String> cards = new HashMap<>();

		for (MaterialCard mc : MaterialCard.values()) {
			cards.put(mc.getId(), mc.forLocale(lang));
		}

		return ResponseEntity.accepted().body(cards);
	}

	/**
	 * Returns the {@link MaterialCard MaterialCards'}
	 * {@link MaterialCard#descriptionForLocale(Locale) description} for the current
	 * locale.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} Invalid language.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED} : {@link HashMap<Integer, String>}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "desc", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> desc(HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();
		final Locale lang = SpringUtils.getContextLocale();
		SpringUtils.badRequest(lang == null, "Invalid language.");

		/*
		 * if (auth instanceof AnonymousAuthenticationToken) { final HashMap<String,
		 * Object> principal = (HashMap<String, Object>)
		 * SpringUtils.getContextPrincipal(); lang = (Locale)
		 * principal.getOrDefault("locale", Locales.LUXEMBOURISH); } else { final
		 * UserData ud = SpringUtils.needContextUser(); lang = ud.getLocale(); }
		 */

		InteractionMonitor.pushAcceptedDesc("Locale: " + lang);

		final HashMap<Integer, String> messages = new HashMap<>();

		for (MaterialCard mc : MaterialCard.values()) {
			messages.put(mc.getId(), mc.descriptionForLocale(lang));
		}

		return ResponseEntity.accepted().body(messages);
	}

}
