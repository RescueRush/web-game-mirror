package lu.rescue_rush.game_backend.configs.discord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eduardomcb.discord.webhook.WebhookClient;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.DiscordWebhookConfig;

@Configuration
public class WebhookClientProvider {

	@Autowired
	private DiscordWebhookConfig config;

	@Bean
	public WebhookClient webhookClient() throws FileNotFoundException, IOException {
		final Logger logger = Logger.getLogger(WebhookClientProvider.class.getName());

		WebhookClient client = new WebhookClient(config.url) {
			protected Logger logger = Logger.getLogger(WebhookClient.class.getName());

			@Override
			protected void onSuccess(String message) {
				logger.info(message);
			}

			@Override
			protected void onFailure(Exception e) {
				logger.log(Level.WARNING, "Error while sending webhook packet: ", e);
			}
		};

		if (R2ApiMain.DEBUG) {
			logger.info(client.toString());
		}

		return client;
	}

}
