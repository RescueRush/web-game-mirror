package lu.rescue_rush.game_backend.configs.security.filters;

import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lu.rescue_rush.game_backend.sb.component.HandlerMethodResolver;
import lu.rescue_rush.game_backend.sb.component.HandlerMethodResolver.AbstractRequestHandler;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@Component
public class DeprecatedFilter extends OncePerRequestFilter {

	// public static final String AUTH_HEADER = "Authorization";
	private static final Logger LOGGER = Logger.getLogger(DeprecatedFilter.class.getName());

	@Autowired
	private HandlerMethodResolver handlerMethodResolver;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		final String requestURI = request.getRequestURI();

		final AbstractRequestHandler<?> handler = handlerMethodResolver.resolve(request);

		if (handler.hasAnnotation(Deprecated.class)) {
			InteractionMonitor.pushDesc(SpringUtils.getContextUser(), InteractionMonitor.R2API_DEPRECATED_ENDPOINT, false, request.getRemoteAddr(), "Deprecated endpoint: '" + requestURI + "'");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Deprecated endpoint.");
			return;
		}

		filterChain.doFilter(request, response);
	}

}
