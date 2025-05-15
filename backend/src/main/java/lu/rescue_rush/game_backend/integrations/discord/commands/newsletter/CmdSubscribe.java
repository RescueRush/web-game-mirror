package lu.rescue_rush.game_backend.integrations.discord.commands.newsletter;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.pcy113.pclib.db.TableHelper;

import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.tables.newsletter.NewsletterSubscriptionTable;
import lu.rescue_rush.game_backend.integrations.discord.DiscordSender;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandExecutor;
import lu.rescue_rush.game_backend.integrations.discord.embeds.newsletter.EmbedNewsletterSubscribe;
import lu.rescue_rush.game_backend.integrations.email.EmailSender;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class CmdSubscribe implements SlashCommandExecutor {

	private static final Logger LOGGER = Logger.getLogger(CmdSubscribe.class.getName());

	@Autowired
	private NewsletterSubscriptionTable NEWSLETTER_SUBSCRIPTION;

	@Autowired
	private EmbedNewsletterSubscribe NEWSLETTER_SUBSCRIBE;

	@Autowired
	private DiscordSender discordSender;

	@Autowired
	private EmailSender emailSender;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final String email = event.getOption("email").getAsString();
		final String source = event.getOption("source", "api", OptionMapping::getAsString);
		final Language lang = Language.byCode(event.getOption("lang").getAsString());
		final boolean silent = event.getOption("silent", false, OptionMapping::getAsBoolean);

		event.deferReply().queue();

		if (NEWSLETTER_SUBSCRIPTION.existsEmail(email)) {
			event.getHook().sendMessage("`" + email + "` is already subscribed to the newsletter.").queue();
			return;
		}

		final NewsletterSubscriptionData nds = TableHelper
				.insertOrLoad(NEWSLETTER_SUBSCRIPTION, new NewsletterSubscriptionData(email, source, lang.getCode()), () -> NewsletterSubscriptionData.byEmail(email)).catch_(e -> {
					SpringUtils.catch_(LOGGER, "An error occured when trying to insert/reload NewsletterSubscriptionData: ", e);
					event.getHook().sendMessage("An error occured when trying to insert/reload NewsletterSubscriptionData: `" + e.getMessage() + "`").queue();
				}).run();

		discordSender.prepareSendEmbed().catch_(e -> {
			SpringUtils.catch_(LOGGER, "An error occured when trying to send the newsletter subscribe embed: ", e);
			event.getHook().sendMessage("An error occured when trying to send the newsletter subscribe embed: `" + e.getMessage() + "`").queue();
		}).runAsync(NEWSLETTER_SUBSCRIBE.build(nds, "discord: " + event.getUser().getAsMention() + " (" + event.getUser().getId() + ")"));

		if (!silent) {
			emailSender.prepareNewsletterSubscribeEmail().catch_(e -> {
				SpringUtils.catch_(LOGGER, "An error occured when trying to send the newsletter subscribe email: ", e);
				event.getHook().sendMessage("An error occured when trying to send the newsletter subscribe email: `" + e.getMessage() + "`").queue();
			}).runAsync(nds);
		}
	}

	@Override
	public String id() {
		return "subscribe";
	}

	@Override
	public OptionData[] options() {
		//@formatter:off
		return new OptionData[] {
				new OptionData(OptionType.STRING, "email", "The email to subscribe to the newsletter.", true),
				new OptionData(OptionType.STRING, "lang", "The language of the newsletter.", true, false)
						.addChoice("Luxembourgish", "lb")
						.addChoice("French", "fr")
						.addChoice("German", "de")
						.addChoice("English", "en"),
				new OptionData(OptionType.STRING, "source", "The source of the subscription (default: api)."),
				new OptionData(OptionType.BOOLEAN, "silent", "Silent mode, no confirmation email sent to the user.")
		};
		//@formatter:off
	}

	@Override
	public String description() {
		return "Subscribe the given email to the newsletter.";
	}

}
