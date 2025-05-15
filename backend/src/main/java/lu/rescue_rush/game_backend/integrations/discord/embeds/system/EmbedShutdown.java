package lu.rescue_rush.game_backend.integrations.discord.embeds.system;

import java.awt.Color;

import org.springframework.stereotype.Service;

import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbed;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class EmbedShutdown implements DiscordEmbedBuilder {

	public DiscordEmbed build(final long startTime) {
		return new DiscordEmbed() {

			@Override
			public MessageEmbed build() {
				EmbedBuilder builder = new EmbedBuilder();

				builder.setTitle("Rescue-rush.lu | Shutdown");
				builder.setColor(Color.BLUE);

				long uptimeMillis = System.currentTimeMillis() - startTime;

				long uptimeSeconds = uptimeMillis / 1000;

				long hours = uptimeSeconds / 3600;
				long minutes = (uptimeSeconds % 3600) / 60;
				long seconds = uptimeSeconds % 60;

				builder.addField("Infos", String.format("**Uptime:** %d hours, %d minutes, %d seconds", hours, minutes, seconds), false);

				return builder.build();
			}

		};
	}

}
