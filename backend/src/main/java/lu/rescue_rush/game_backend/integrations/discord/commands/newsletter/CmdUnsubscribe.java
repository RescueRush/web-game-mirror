package lu.rescue_rush.game_backend.integrations.discord.commands.newsletter;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.tables.newsletter.NewsletterSubscriptionTable;
import lu.rescue_rush.game_backend.integrations.discord.DiscordSender;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandExecutor;
import lu.rescue_rush.game_backend.integrations.discord.embeds.newsletter.EmbedNewsletterUnsubscribe;
import lu.rescue_rush.game_backend.integrations.email.EmailSender;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class CmdUnsubscribe implements SlashCommandExecutor {

	private static final Logger LOGGER = Logger.getLogger(CmdUnsubscribe.class.getName());

	@Autowired
	private NewsletterSubscriptionTable NEWSLETTER_SUBSCRIPTION;

	@Autowired
	private EmbedNewsletterUnsubscribe NEWSLETTER_UNSUBSCRIBE;

	@Autowired
	private DiscordSender discordSender;

	@Autowired
	private EmailSender emailSender;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final String inputtype = event.getOption("inputtype").getAsString();
		final String emailHash = event.getOption("value").getAsString();

		event.deferReply().queue();

		if (!(inputtype.equals("email") ? NEWSLETTER_SUBSCRIPTION.existsEmail(emailHash) : NEWSLETTER_SUBSCRIPTION.existsHash(emailHash))) {
			event.getHook().sendMessage("`" + emailHash + "` (" + inputtype + ") isn't subscribed to the newsletter.").queue();
			return;
		}

		final NewsletterSubscriptionData nds = (inputtype.equals("email") ? NEWSLETTER_SUBSCRIPTION.byEmail(emailHash) : NEWSLETTER_SUBSCRIPTION.byHash(emailHash));
		if (nds == null) {
			event.getHook().sendMessage("An error occured when trying to load NewsletterSubscriptionData.").queue();
			return;
		}

		final String email = nds.getEmail();
		NEWSLETTER_SUBSCRIPTION.requestSafe_anonymize(nds);

		discordSender.sendEmbed(NEWSLETTER_UNSUBSCRIBE.build(nds, email, "discord: " + event.getUser().getAsMention() + " (" + event.getUser().getId() + ")"));
	}

	@Override
	public String id() {
		return "unsubscribe";
	}

	@Override
	public OptionData[] options() {
		//@formatter:off
		return new OptionData[] {
				new OptionData(OptionType.STRING, "inputtype", "Input method: Email, Hash.", true, false)
						.addChoice("Email", "email")
						.addChoice("Hash", "hash"),
				new OptionData(OptionType.STRING, "value", "Input value.", true)
		};
		//@formatter:off
	}

	@Override
	public String description() {
		return "Unsubscribe the given email/hash from the newsletter.";
	}

}
