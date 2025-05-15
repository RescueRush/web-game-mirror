package lu.rescue_rush.game_backend.configs.security.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.sb.component.HandlerMethodResolver;
import lu.rescue_rush.game_backend.sb.component.UserAuthenticator;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.AllowAnonymous;

@Component
public class CookieAuthFilter extends OncePerRequestFilter {

	@Autowired
	private UserAuthenticator authenticator;

	@Autowired
	private LocaleResolver localeResolver;

	@Autowired
	private HandlerMethodResolver handlerMethodResolver;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		final boolean optionalFilter = handlerMethodResolver.resolve(request).hasAnnotation(AllowAnonymous.class);

		if (SpringUtils.isContextUser()) {
			request.setAttribute("user", SpringUtils.getContextUser());
			request.setAttribute("auth", SpringUtils.getContextAuthentication());
			localeResolver.setLocale(request, response, SpringUtils.getContextUser().getLocale());

			filterChain.doFilter(request, response);
			return;
		}

		final Cookie[] cookies = request.getCookies();
		final HandlerMethod handler = (HandlerMethod) request.getAttribute("handler");
		final String requestURI = request.getRequestURI();

		if (cookies != null) {
			final UserData user = authenticator.cookies(cookies);

			if (user != null) { // even if the filter is optional, we still auth the user and attach it to the
								// session
				SpringUtils.setContextUser(user);
				request.setAttribute("user", user);
				request.setAttribute("auth", SpringUtils.getContextAuthentication());

				localeResolver.setLocale(request, response, user.getLocale());
				filterChain.doFilter(request, response);
				return;

			} else if (optionalFilter) { // if the user isn't found BUT the filter is optional, we don't care LOOL

				filterChain.doFilter(request, response);
				return;
			}

			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
			return;

		} else if (optionalFilter) { // if the user isn't found (no cookies >_<) BUT the filter is optional, we don't
										// care LOOL
			filterChain.doFilter(request, response);
			return;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token.");
	}

}
