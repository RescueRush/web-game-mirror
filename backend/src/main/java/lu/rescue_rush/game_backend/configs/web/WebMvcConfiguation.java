package lu.rescue_rush.game_backend.configs.web;

import java.util.logging.Logger;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguation implements WebMvcConfigurer {

	private static final Logger LOGGER = Logger.getLogger(WebMvcConfiguation.class.getName());

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// LOGGER.info("Registered Interceptors");
	}

}
