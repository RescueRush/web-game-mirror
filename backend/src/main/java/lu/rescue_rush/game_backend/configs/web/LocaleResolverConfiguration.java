package lu.rescue_rush.game_backend.configs.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import lu.rescue_rush.game_backend.data.Locales;

@Configuration
public class LocaleResolverConfiguration {

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver resolver = new SessionLocaleResolver();
		resolver.setDefaultLocale(Locales.LUXEMBOURISH);
		return resolver;
	}

}
