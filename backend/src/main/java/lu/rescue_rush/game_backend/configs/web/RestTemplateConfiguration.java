package lu.rescue_rush.game_backend.configs.web;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lu.rescue_rush.game_backend.R2ApiMain;

@Configuration
public class RestTemplateConfiguration {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.defaultHeader("User-Agent", R2ApiMain.NAME + " " + R2ApiMain.VERSION + " (Spring Boot)").build();
	}

}
