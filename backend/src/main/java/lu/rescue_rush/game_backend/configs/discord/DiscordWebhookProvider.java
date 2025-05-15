package lu.rescue_rush.game_backend.configs.discord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lu.pcy113.pclib.config.ConfigLoader;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.DiscordWebhookConfig;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@Configuration
public class DiscordWebhookProvider {

	@Bean
	public DiscordWebhookConfig discordWebhookConfig() throws FileNotFoundException, IOException {
		final Logger logger = Logger.getLogger(DiscordWebhookProvider.class.getName());

		// -- Load config
		if (SpringUtils.extractFile("discord_sender.json", R2ApiMain.CONFIG_DIR, "discord_sender.json")) {
			logger.info("Extracted default discord_sender.json to " + R2ApiMain.CONFIG_DIR);
		} else {
			logger.info("Config file discord_sender.json already existed found. Using default values.");
		}

		DiscordWebhookConfig discordSenderConfig = ConfigLoader.loadFromJSONFile(new DiscordWebhookConfig(), new File(R2ApiMain.CONFIG_DIR, "discord_sender.json"));

		if (R2ApiMain.DEBUG) {
			logger.info(discordSenderConfig.toString());
		}

		return discordSenderConfig;
	}

}
