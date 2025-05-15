package lu.rescue_rush.game_backend.integrations.email;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.async.NextTask;
import lu.pcy113.pclib.impl.ExceptionConsumer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lu.rescue_rush.game_backend.data.HTMLEmailData;
import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.db.data.monitor.interaction.UserPassResetData;
import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.data.support.SupportFormData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.monitor.email.VerifiedEmailTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.remote.RemoteProvider;

@Service
public class EmailSender {

	private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());

	@Autowired
	private JavaMailSender jms;

	@Value("${support.email}")
	private String supportAddress;

	@Autowired
	private EmailSenderConfig config;

	@Autowired
	private RemoteProvider remote;

	@Autowired
	private VerifiedEmailTable VERIFIED_EMAIL;

	public void sendEmail(String receiver, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		// message.setFrom(config.from);
		message.setTo(receiver);
		message.setSubject(subject);
		message.setText(body);
		message.setReplyTo(supportAddress);
		jms.send(message);

		VERIFIED_EMAIL.loadOrInsertByEmail(receiver);
	}

	public void sendHTMLEmail(String receiver, String subject, String htmlDoc) throws MessagingException {
		MimeMessage mimeMessage = jms.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
		helper.setText(htmlDoc, true);
		helper.setTo(receiver);
		helper.setSubject(subject);
		// helper.setFrom(config.from);
		helper.setReplyTo(supportAddress);
		jms.send(mimeMessage);

		VERIFIED_EMAIL.loadOrInsertByEmail(receiver);
	}

	public void sendHTMLEmail(HTMLEmailData data) throws MessagingException {
		sendHTMLEmail(data.getReceiver(), data.getSubject(), data.getHtmlDoc());
	}

	public NextTask<HTMLEmailData, Void> prepareSendHTMLEmail() {
		return NextTask.withArg((ExceptionConsumer<HTMLEmailData>) this::sendHTMLEmail);
	}

	public String getSupportAddress() {
		return supportAddress;
	}

	public boolean sendSupportFormEmail(SupportFormData uefd) throws MessagingException {
		Objects.requireNonNull(uefd);

		final String subject = "Rescue-rush.lu | Support | Ticket#" + uefd.getId();
		final Language lang = PCUtils.defaultIfNull(Language.byCode(uefd.getLang()), Language.LUXEMBOURISH);

		String body = remote.fetchRemoteData("emails/ticket_" + lang.getCode() + ".html");

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data: " + lang);
			return false;
		}

		body = body.replace("{id}", Integer.toString(uefd.getId())).replace("{name}", uefd.getName()).replace("{email}", uefd.getEmail())
				.replace("{submit_date}", new SimpleDateFormat("dd/MM/yyyy hh:mm").format(uefd.getSubmitDate())).replace("{message}", SpringUtils.sanitizeHtml(uefd.getMessage()));

		this.sendHTMLEmail(uefd.getEmail(), subject, body, "info@rescue-rush.lu");

		LOGGER.info("Sent email to: " + uefd.getEmail());

		return true;
	}

	public boolean sendNewsletterSubscribeEmail(NewsletterSubscriptionData ned) throws MessagingException {
		Objects.requireNonNull(ned);

		final String subject = "Rescue-rush.lu | Newsletter";
		final Language lang = PCUtils.defaultIfNull(Language.byCode(ned.getLang()), Language.LUXEMBOURISH);

		String body = remote.fetchRemoteData("emails/subscribed_" + lang.getCode() + ".html");

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data!");
			return false;
		}

		body = body.replace("{id}", Integer.toString(ned.getId())).replace("{email}", ned.getEmail()).replace("{token}", ned.getHash());

		this.sendHTMLEmail(ned.getEmail(), subject, body);

		LOGGER.info("Sent email to: " + ned.getEmail() + " (" + lang + ")");

		return true;
	}

	public boolean sendNewsletterEmail(NewsletterSubscriptionData ned, String url, Function<Language, String> title) throws MessagingException {
		Objects.requireNonNull(ned);

		final Language lang = PCUtils.defaultIfNull(Language.byCode(ned.getLang()), Language.LUXEMBOURISH);
		final String subject = "Rescue-rush.lu | Newsletter: " + title.apply(lang);
		final String fullUrl = url + lang.getCode().toUpperCase() + ".html";

		String body = remote.fetchRemoteData(fullUrl);
		String langTitle = title.apply(lang);

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data (" + fullUrl + ")!");
			return false;
		}

		//@formatter:off
		body = body.replace("{id}", SpringUtils.nullString(Integer.toString(ned.getId())))
				.replace("{title}", langTitle)
				.replace("{fullUrl}", "https://rescue-rush.lu/" + fullUrl)
				.replace("{email}", ned.getEmail())
				.replace("{token}", SpringUtils.safeString(ned.getHash()))
				/*.replace("{lang}", SpringUtils.safeString(() -> lang.getName()))*/;
		//@formatter:on

		this.sendHTMLEmail(ned.getEmail(), subject, body);

		LOGGER.info("Sent email to: " + ned.getEmail());

		return true;
	}

	public boolean sendNewsletterEmail(NewsletterSubscriptionData ned, String url, String title) throws MessagingException {
		return sendNewsletterEmail(ned, url, (Function<Language, String>) (lang) -> title);
	}

	public boolean sendUserHiddenEmail(UserData ud) throws MessagingException {
		Objects.requireNonNull(ud);

		final Language lang = ud.getLanguage();

		final String subject = "Rescue-rush.lu | " + switch (ud.getLanguage()) {
		case LUXEMBOURISH -> "Benotzer verstoppt";
		case FRENCH -> "Utilisateur caché";
		case ENGLISH -> "User hidden";
		case GERMAN -> "Benutzer verborgen";
		};

		String body = remote.fetchRemoteData("emails/userHidden_" + lang.getCode() + ".html");

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data!");
			return false;
		}

		body = body.replace("{id}", Integer.toString(ud.getId())).replace("{email}", ud.getEmail()).replace("{name}", ud.getName());

		this.sendHTMLEmail(ud.getEmail(), subject, body);

		LOGGER.info("Sent email to: " + ud.getEmail());

		return true;

	}

	public boolean sendUserCreatedEmail(UserData ud) throws MessagingException {
		Objects.requireNonNull(ud);

		final Language lang = ud.getLanguage();

		final String subject = "Rescue-rush.lu | " + switch (ud.getLanguage()) {
		case LUXEMBOURISH -> "Benotzer krééiert";
		case FRENCH -> "Utilisateur crée";
		case ENGLISH -> "User created";
		case GERMAN -> "Benutzer erstellt";
		};

		String body = remote.fetchRemoteData("emails/userCreated_" + lang.getCode() + ".html");

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data!");
			return false;
		}

		body = body.replace("{email}", ud.getEmail()).replace("{name}", ud.getName()).replace("{ip}", ud.getIp());

		// TODO: take a look at this, i don't think it works
		// this.sendHTMLEmail(ud.getEmail(), subject, body);

		LOGGER.info("Sent email to: " + ud.getEmail());

		return true;
	}

	public boolean sendPassResetEmail(UserPassResetData uprd) throws MessagingException {
		Objects.requireNonNull(uprd);

		uprd.loadUser();

		final Language lang = uprd.getUserData().getLanguage();

		final String subject = "Rescue-rush.lu | " + switch (lang) {
		case GERMAN -> "Link zum Zurücksetzen des Passworts";
		case FRENCH -> "Lien de réinitialisation du mot de passe";
		case ENGLISH -> "Password Reset Link";
		case LUXEMBOURISH -> "Link fir d'Passwuert zeréckzesetzen";
		};

		String body = remote.fetchRemoteData("emails/passwdReset_" + lang.getCode() + ".html");
		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data!");
			return false;
		}

		// https://rescue-rush.lu
		final String link = "https://rescue-rush.lu/game/connect/password-reset/?token=" + uprd.getClientToken();

		body = body.replace("{resetlink}", link);

		this.sendHTMLEmail(uprd.getUserData().getEmail(), subject, body);

		LOGGER.info("Sent email to " + uprd.getUserData().getEmail() + "!");

		return true;
	}

	public boolean sendPassResettedEmail(UserData ud) throws MessagingException {
		final Language lang = ud.getLanguage();

		final String subject = "Rescue-rush.lu | " + switch (lang) {
		case GERMAN -> "Ihr Passwort wurde zurückgesetzt";
		case FRENCH -> "Mise à jour de votre mot de passe";
		case ENGLISH -> "Your password was resetted";
		case LUXEMBOURISH -> "Äert Passwuert gouf zeréckgesat";
		};

		String body = remote.fetchRemoteData("emails/passwdResetted_" + lang.getCode() + ".html");

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data!");
			return false;
		}

		this.sendHTMLEmail(ud.getEmail(), subject, body);

		LOGGER.info("Sent email to " + ud.getEmail() + "!");

		return true;
	}

	public boolean sendEmailResetEmail(UserData ud) throws MessagingException {
		Objects.requireNonNull(ud);

		final Language lang = ud.getLanguage();

		final String subject = "Rescue-rush.lu | " + switch (lang) {
		case GERMAN -> "Link zum Zurücksetzen des Passworts";
		case FRENCH -> "Lien de réinitialisation du mot de passe";
		case ENGLISH -> "Password Reset Link";
		case LUXEMBOURISH -> "Link fir d'Passwuert zeréckzesetzen";
		};

		String body = remote.fetchRemoteData("emails/passwdResetted_" + lang.getCode() + ".html");

		if (body == null) {
			LOGGER.warning("Couldn't fetch remote email data!");
			return false;
		}

		body = body.replace("{resetlink}", "N/A");

		this.sendHTMLEmail(ud.getEmail(), subject, body);

		LOGGER.info("Sent email to: " + ud.getEmail());

		return true;
	}

	public void sendHTMLEmail(String receiver, String subject, String htmlDoc, String... copy) throws MessagingException {
		MimeMessage mimeMessage = jms.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
		helper.setBcc(copy);
		helper.setText(htmlDoc, true);
		helper.setTo(receiver);
		helper.setSubject(receiver);
		helper.setFrom(supportAddress);
		jms.send(mimeMessage);
	}

	public NextTask<SupportFormData, Boolean> prepareSupportFormEmail() {
		return NextTask.<SupportFormData, Boolean>withArg(this::sendSupportFormEmail);
	}

	public NextTask<NewsletterSubscriptionData, Boolean> prepareNewsletterSubscribeEmail() {
		return NextTask.<NewsletterSubscriptionData, Boolean>withArg(this::sendNewsletterSubscribeEmail);
	}

	public NextTask<UserData, Boolean> prepareUserHiddenEmail() {
		return NextTask.<UserData, Boolean>withArg(this::sendUserHiddenEmail);
	}

	public NextTask<UserData, Boolean> prepareUserCreatedEmail() {
		return NextTask.<UserData, Boolean>withArg(this::sendUserCreatedEmail);
	}

	public NextTask<UserPassResetData, Boolean> preparePassResetEmail() {
		return NextTask.<UserPassResetData, Boolean>withArg(this::sendPassResetEmail);
	}

	public NextTask<UserData, Boolean> prepareUserEmailResetEmail() {
		return NextTask.<UserData, Boolean>withArg(this::sendEmailResetEmail);
	}

	public NextTask<UserData, Boolean> preparePassResettedEmail() {
		return NextTask.<UserData, Boolean>withArg(this::sendPassResettedEmail);
	}
}
