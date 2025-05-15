package lu.rescue_rush.game_backend.endpoints;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lu.pcy113.pclib.PCUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lu.rescue_rush.game_backend.data.Locales;
import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.tables.newsletter.NewsletterSubscriptionTable;
import lu.rescue_rush.game_backend.integrations.discord.DiscordSender;
import lu.rescue_rush.game_backend.integrations.discord.embeds.newsletter.EmbedNewsletterSubscribe;
import lu.rescue_rush.game_backend.integrations.discord.embeds.newsletter.EmbedNewsletterUnsubscribe;
import lu.rescue_rush.game_backend.integrations.email.EmailSender;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.types.NewsletterTypes.SubscribeRequest;
import lu.rescue_rush.game_backend.types.NewsletterTypes.UnsubscribeRequest;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.AllowAnonymous;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@CrossOrigin
@RestController
@RequestMapping(value = "/newsletter")
public class NewsletterEndpoints {

	private static final Logger LOGGER = Logger.getLogger(NewsletterEndpoints.class.getName());

	@Autowired
	private NewsletterSubscriptionTable NEWSLETTER_EMAILS;

	@Autowired
	private DiscordSender DISCORD_SENDER;

	@Autowired
	private EmailSender EMAIL_SENDER;

	@Autowired
	private EmbedNewsletterSubscribe EMBED_NEWSLETTER_SUBSCRIBE;

	@Autowired
	private EmbedNewsletterUnsubscribe EMBED_NEWSLETTER_UNSUBSCRIBE;

	/**
	 * Subscribes the given {@link SubscribeRequest#email email} to the newsletter,
	 * using the given {@link SubscribeRequest#source source} (default: {@code api})
	 * and {@link SubscribeRequest#lang language}.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} Invalid language.</li>
	 * <li>{@link HttpStatus#BAD_REQUEST} Invalid email.</li>
	 * <li>{@link HttpStatus#BAD_REQUEST} Invalid source.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@PostMapping(value = "/subscribe", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> subscribe(@RequestBody SubscribeRequest formRequest, HttpServletRequest request) {
		SpringUtils.badRequest(Locales.byCode(formRequest.lang) == null, "Invalid language.",
				() -> InteractionMonitor.pushOutcomeDesc(false, "Invalid language: " + formRequest.lang + " from " + formRequest.source));
		SpringUtils.badRequest(!PCUtils.validEmail(formRequest.email), "Invalid email.",
				() -> InteractionMonitor.pushOutcomeDesc(false, "Invalid email: " + formRequest.email + " from " + formRequest.source));
		SpringUtils.badRequest("action".equals(formRequest.lang), "Invalid source.",
				() -> InteractionMonitor.pushOutcomeDesc(false, "Invalid source: " + formRequest.email + " from " + formRequest.source));

		formRequest.source = formRequest.source == null ? "api" : formRequest.source.trim();

		final boolean alreadySubscribed = NEWSLETTER_EMAILS.existsEmail(formRequest.email);

		if (alreadySubscribed) {
			InteractionMonitor.pushOutcomeDesc(false, formRequest.email + " from " + formRequest.source);
			return ResponseEntity.accepted().build();
		}

		final NewsletterSubscriptionData data = NEWSLETTER_EMAILS.requestSafe_insertAndReload(formRequest.email, formRequest.source, formRequest.lang);
		InteractionMonitor.pushOutcomeDesc(true, formRequest.email + " from " + formRequest.source + " (" + formRequest.lang + ")");

		DISCORD_SENDER.prepareSendEmbed().catch_(SpringUtils.catch_(LOGGER, e -> "Couldn't send newsletter subscription embed: " + e.getMessage()))
				.runAsync(EMBED_NEWSLETTER_SUBSCRIBE.build(data, request.getRemoteAddr()));
		EMAIL_SENDER.prepareNewsletterSubscribeEmail().catch_(SpringUtils.catch_(LOGGER, e -> "Couldn't send newsletter subscription email: " + e.getMessage())).runAsync(data);

		return ResponseEntity.accepted().build();
	}

	/**
	 * Unsubscribes the given {@link UnsubscribeRequest#email hash} from the
	 * newsletter.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} Unknown email.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED}
	 */
	@AllowAnonymous
	@ExecutionTrack
	@PostMapping(value = "/unsubscribe", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> unsubscribe(@RequestBody UnsubscribeRequest formRequest, HttpServletResponse response, HttpServletRequest request) {
		SpringUtils.badRequest(!NEWSLETTER_EMAILS.existsHash(formRequest.email), "Unknown email.", () -> InteractionMonitor.pushOutcomeDesc(false, formRequest.email));

		final NewsletterSubscriptionData data = NEWSLETTER_EMAILS.requestSafe_byHash(formRequest.email);

		if (data.getEmail() == null) {
			InteractionMonitor.pushOutcomeDesc(false, formRequest.email);
			return ResponseEntity.accepted().build();
		}

		final String email = data.getEmail();

		NEWSLETTER_EMAILS.requestSafe_anonymize(data);

		InteractionMonitor.pushOutcomeDesc(true, formRequest.email);

		DISCORD_SENDER.prepareSendEmbed().catch_(SpringUtils.catch_(LOGGER, e -> "Couldn't send newsletter unsubscription embed: " + e.getMessage()))
				.runAsync(EMBED_NEWSLETTER_UNSUBSCRIBE.build(data, email + " (||" + formRequest.email + "||)", request.getRemoteAddr()));

		return ResponseEntity.accepted().build();
	}

}
