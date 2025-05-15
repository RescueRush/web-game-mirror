package lu.rescue_rush.game_backend.configs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lu.pcy113.pclib.config.ConfigLoader;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.email.EmailSenderConfig;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@Configuration
public class EmailSenderProvider {

	@Bean
	public EmailSenderConfig emailSenderConfig() throws FileNotFoundException, IOException {
		final Logger logger = Logger.getLogger(EmailSenderProvider.class.getName());

		// -- Load config
		if (SpringUtils.extractFile("email_sender.json", R2ApiMain.CONFIG_DIR, "email_sender.json")) {
			logger.info("Extracted default email_sender.json to " + R2ApiMain.CONFIG_DIR);
		} else {
			logger.info("Config file email_sender.json already existed found. Using default values.");
		}

		EmailSenderConfig dbConfig = ConfigLoader.loadFromJSONFile(new EmailSenderConfig(), new File(R2ApiMain.CONFIG_DIR, "email_sender.json"));

		if (R2ApiMain.DEBUG) {
			logger.info(dbConfig.toString());
		}

		return dbConfig;
	}

}