package lu.rescue_rush.game_backend.configs;

import java.util.logging.Logger;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

	private static final Logger LOGGER = Logger.getLogger(CacheConfiguration.class.getName());

	@Bean
	public CacheManager cacheManager() {
		LOGGER.info("Registered cache manager");
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
		cacheManager.setAllowNullValues(false);
		return cacheManager;
	}
}