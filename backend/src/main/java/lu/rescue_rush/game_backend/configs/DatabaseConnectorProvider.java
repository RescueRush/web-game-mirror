package lu.rescue_rush.game_backend.configs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lu.pcy113.pclib.config.ConfigLoader;
import lu.pcy113.pclib.db.DataBaseConnector;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@Configuration
public class DatabaseConnectorProvider {

	@Bean
	public DataBaseConnector dataBaseConnector() throws FileNotFoundException, IOException {
		final Logger logger = Logger.getLogger(DatabaseConnectorProvider.class.getName());

		// -- Load config
		if (SpringUtils.extractFile("db_connector.json", R2ApiMain.CONFIG_DIR, "db_connector.json")) {
			logger.info("Extracted default db_connector.json to " + R2ApiMain.CONFIG_DIR);
		} else {
			logger.info("Config file db_connector.json already existed found. Using default values.");
		}

		DataBaseConnector dbConfig = ConfigLoader.loadFromJSONFile(new DataBaseConnector(), new File(R2ApiMain.CONFIG_DIR, "db_connector.json"));

		if (R2ApiMain.DEBUG) {
			logger.info(dbConfig.toString());
		}

		return dbConfig;
	}

}