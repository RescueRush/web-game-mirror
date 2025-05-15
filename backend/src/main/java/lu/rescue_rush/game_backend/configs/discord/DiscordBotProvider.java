package lu.rescue_rush.game_backend.configs.discord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lu.pcy113.pclib.config.ConfigLoader;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.DiscordBotConfig;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@Configuration
public class DiscordBotProvider {

	@Bean
	public DiscordBotConfig discordBotConfig() throws FileNotFoundException, IOException {
		final Logger logger = Logger.getLogger(DiscordBotProvider.class.getName());

		// -- Load config
		if (SpringUtils.extractFile("discord_bot.json", R2ApiMain.CONFIG_DIR, "discord_bot.json")) {
			logger.info("Extracted default discord_bot.json to " + R2ApiMain.CONFIG_DIR);
		} else {
			logger.info("Config file discord_bot.json already existed found. Using default values.");
		}

		DiscordBotConfig discordBotConfig = ConfigLoader.loadFromJSONFile(new DiscordBotConfig(), new File(R2ApiMain.CONFIG_DIR, "discord_bot.json"));

		if (R2ApiMain.DEBUG) {
			logger.info(discordBotConfig.toString());
		}

		return discordBotConfig;
	}

}
