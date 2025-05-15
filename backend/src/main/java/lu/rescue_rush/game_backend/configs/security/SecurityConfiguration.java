package lu.rescue_rush.game_backend.configs.security;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lu.rescue_rush.game_backend.configs.security.filters.AllowAnonymousFilter;
import lu.rescue_rush.game_backend.configs.security.filters.CookieAuthFilter;
import lu.rescue_rush.game_backend.configs.security.filters.DeprecatedFilter;
import lu.rescue_rush.game_backend.configs.security.filters.HandlerMethodResolverFilter;

@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfiguration {

	private static final Logger LOGGER = Logger.getLogger(SecurityConfiguration.class.getName());

	@Autowired
	private HandlerMethodResolverFilter handlerMethodResolverFilter;
	@Autowired
	private DeprecatedFilter deprecatedFilter;
	@Autowired
	private AllowAnonymousFilter allowAnonymousFilter;
	@Autowired
	private CookieAuthFilter cookieAuthFilter;

	@Autowired
	private SecurityContextRepository securityContextRepository;
	@Autowired
	private CorsConfigurationSource corsConfigurationSource;
	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		LOGGER.info("Registered SecurityFilterChain");

		//@formatter:off
		return http
			.csrf(csrf -> csrf.disable())
			.formLogin(formLogin -> formLogin.disable())
			.httpBasic(httpBasic -> httpBasic.disable())
			.anonymous(a -> a.disable())
			.logout(logout -> logout.disable())
			
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
			.securityContext(securityContext -> securityContext.securityContextRepository(securityContextRepository))
			.userDetailsService(userDetailsService)
			
			// .addFilterBefore(optionPreflightFilter, SecurityContextHolderFilter.class)
			// .addFilterBefore(corsFilter, SecurityContextHolderFilter.class)
			.addFilterAfter(handlerMethodResolverFilter, CorsFilter.class)
			.addFilterAfter(deprecatedFilter, HandlerMethodResolverFilter.class)
			.addFilterAfter(allowAnonymousFilter, DeprecatedFilter.class)
			.addFilterAfter(cookieAuthFilter, AllowAnonymousFilter.class)
			
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // actual auth control is in filters
			)
			
			.build();
		//@formatter:on
	}
}
