package lu.rescue_rush.game_backend.configs.web;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@Profile("!debug")
public class CorsConfigurationSourceConfiguration {

	private static final Logger LOGGER = Logger.getLogger(CorsConfigurationSourceConfiguration.class.getName());

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		LOGGER.info("Creating CORS configuration source");

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("https://admin.rescue-rush.lu", "https://neo.rescue-rush.lu", "https://rescue-rush.lu"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Set-Cookie"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

}
