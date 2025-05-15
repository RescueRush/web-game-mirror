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
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.sb.component.HandlerMethodResolver;
import lu.rescue_rush.game_backend.sb.component.HandlerMethodResolver.AbstractRequestHandler;
import lu.rescue_rush.game_backend.sb.ws.WSMappingRegistry;

@Component
public class HandlerMethodResolverFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = Logger.getLogger(HandlerMethodResolverFilter.class.getName());

	@Autowired
	private HandlerMethodResolver handlerMethodResolver;

	@Autowired
	private WSMappingRegistry wsMappingRegistry;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		final String requestURI = request.getRequestURI();
		final AbstractRequestHandler<?> handler = handlerMethodResolver.resolve(request);

		if (handler == null) {
			if (R2ApiMain.DEBUG) {
				LOGGER.warning("Unknown endpoint: " + requestURI);
			}
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown endpoint: '" + requestURI + "'.");
			return;
		}

		filterChain.doFilter(request, response);
	}

}
