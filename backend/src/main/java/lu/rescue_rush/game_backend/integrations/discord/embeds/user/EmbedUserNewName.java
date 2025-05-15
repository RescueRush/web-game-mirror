package lu.rescue_rush.game_backend.integrations.discord.embeds.user;

import java.awt.Color;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbed;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

@Service
public class EmbedUserNewName implements DiscordEmbedBuilder {

	private static final Logger LOGGER = Logger.getLogger(EmbedUserNewName.class.getName());

	private static final String TITLE_INFOS = "Infos";

	public static final int extractId(Message msg) {
		if (msg.getEmbeds() == null || msg.getEmbeds().size() == 0) {
			final String content = msg.getContentRaw();

			Pattern pattern = Pattern.compile("```User ID: *([0-9]+) \\(.+\\)```");
			Matcher matcher = pattern.matcher(content);

			if (matcher.find()) {
				String extractedText = matcher.group(1);
				return PCUtils.parseInteger(extractedText, -1);
			} else {
				return -1;
			}
		} else {
			final String content = msg.getEmbeds().get(0).getFields().stream().filter(c -> c.getName().equals(TITLE_INFOS)).map(c -> c.getValue()).findFirst().orElse(null);

			Pattern pattern = Pattern.compile("\\**User ID:\\**\\s*([0-9]+)");
			Matcher matcher = pattern.matcher(content);

			if (matcher.find()) {
				String extractedText = matcher.group(1);
				return PCUtils.parseInteger(extractedText, -1);
			} else {
				return -1;
			}
		}
	}

	public DiscordEmbed build(UserData data, String source) {
		return new DiscordEmbed() {

			@Override
			public MessageEmbed build() {
				Objects.requireNonNull(data);

				EmbedBuilder builder = new EmbedBuilder();

				Field infoField = new Field(TITLE_INFOS, "**E-mail:** " + data.getEmail() + "\n**Name**: " + data.getName() + "\n**User ID:** " + data.getId() + " (" + data.getName() + ")"
						+ "\n**Timestamp:** " + data.getJoinDate() + "\n**Source:** " + source, true);

				builder.setTitle("Rescue-rush.lu | User Registered#" + data.getId());
				builder.setColor(Color.GREEN);

				builder.addField(infoField);

				return builder.build();
			}

		};
	}

}
