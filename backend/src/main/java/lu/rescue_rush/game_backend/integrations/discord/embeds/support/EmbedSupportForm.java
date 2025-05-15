package lu.rescue_rush.game_backend.integrations.discord.embeds.support;

import java.awt.Color;
import java.util.Objects;

import org.springframework.stereotype.Service;

import lu.rescue_rush.game_backend.db.data.support.SupportFormData;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbed;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

@Service
public class EmbedSupportForm implements DiscordEmbedBuilder {

	public DiscordEmbed build(SupportFormData data) {
		Objects.requireNonNull(data);
		return new DiscordEmbed() {

			@Override
			public MessageEmbed build() {
				EmbedBuilder builder = new EmbedBuilder();

				data.loadUserData();

				Field infoField = new Field("Infos",
						"**E-mail:** " + data.getEmail() + "\n**Name**: " + data.getName() + "\n**User ID:** "
								+ (data.getUserId() == -1 ? "*NULL*" : data.getUserId() + " (" + data.getUserData().getName() + ")") + "\n**Timestamp:** " + data.getSubmitDate() + "\n**Source:** "
								+ data.getSource(),
						true);
				Field contentField = new Field("Message",
						"```" + (data.getMessage().length() > MessageEmbed.VALUE_MAX_LENGTH ? "[Message too long to be displayed on discord, please look in the db :sob:]" : data.getMessage()) + "```",
						true);

				builder.setTitle("Rescue-rush.lu | Support | Ticket#" + data.getId());
				builder.setColor(Color.GREEN);

				builder.addField(infoField);
				builder.addField(contentField);

				return builder.build();
			}

		};
	}

}
