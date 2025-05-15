package lu.rescue_rush.game_backend.configs.web.ws;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

	private static final Logger LOGGER = Logger.getLogger(AuthHandshakeInterceptor.class.getName());

	@Autowired
	private LocaleResolver localeResolver;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		final ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
		final HttpServletRequest httpRequest = servletRequest.getServletRequest();

		if (httpRequest.getAttribute("user") == null) {
			LOGGER.warning("WS Session User is: " + httpRequest.getAttribute("user"));
			return false;
		}

		attributes.put("user", httpRequest.getAttribute("user"));
		attributes.put("auth", httpRequest.getAttribute("auth"));

		attributes.put("httpRequest.source", httpRequest.getRemoteAddr());
		SpringUtils.setRequestSource(httpRequest.getRemoteAddr());

		attributes.put("locale", localeResolver.resolveLocale(httpRequest));

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
	}

}
