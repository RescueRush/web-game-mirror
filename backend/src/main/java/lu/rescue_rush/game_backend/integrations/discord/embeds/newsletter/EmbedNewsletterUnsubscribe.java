package lu.rescue_rush.game_backend.integrations.discord.embeds.newsletter;

import java.awt.Color;
import java.util.Objects;

import org.springframework.stereotype.Service;

import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbed;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

@Service
public class EmbedNewsletterUnsubscribe implements DiscordEmbedBuilder {

	public DiscordEmbed build(NewsletterSubscriptionData data, String email, String source) {
		Objects.requireNonNull(data);
		return new DiscordEmbed() {

			@Override
			public MessageEmbed build() {
				EmbedBuilder builder = new EmbedBuilder();

				data.loadUserData();
				final UserData ud = data.getUserData();

				Field infoField = new Field("Infos",
						"**E-mail:** " + (data.getEmail() == null ? (email == null ? "*NULL*" : email) : data.getEmail()) + "\n**User ID:** "
								+ (ud == null ? "*NULL*" : ud.getId() + " (" + data.getUserData().getName() + ")") + "\n**Since:** " + data.getSince() + "\n**Source:** " + source + "\n**Left:** "
								+ (data.getLeft() == null ? "*NULL*" : data.getLeft()),
						true);
				Field contentField = new Field("State", "**Source:** " + data.getSource() + "\n:x: Unsubscribed", true);

				builder.setTitle("Rescue-rush.lu | Newsletter#" + data.getId());
				builder.setColor(Color.GREEN);

				builder.addField(infoField);
				builder.addField(contentField);

				return builder.build();
			}

		};
	}

}
