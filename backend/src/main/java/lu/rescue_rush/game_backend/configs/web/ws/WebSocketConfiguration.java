package lu.rescue_rush.game_backend.configs.web.ws;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lu.rescue_rush.game_backend.sb.ws.WSMappingRegistry;
import lu.rescue_rush.game_backend.sb.ws.WSMappingRegistry.WSHandlerData;

@Configuration
@EnableWebSocket
@Profile("!debug")
public class WebSocketConfiguration implements WebSocketConfigurer {

	private static final Logger LOGGER = Logger.getLogger(WebSocketConfiguration.class.getName());

	@Autowired
	private WSMappingRegistry registry;

	@Autowired
	private AuthHandshakeInterceptor authHandshakeInterceptor;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry handlerRegistry) {
		for (WSHandlerData handlerBean : registry.getBeans().values()) {
			//@formatter:off
			handlerRegistry
					.addHandler(new QuietExceptionWebSocketHandlerDecorator(handlerBean.handler()), handlerBean.path())
					.addInterceptors(authHandshakeInterceptor)
					.setAllowedOriginPatterns("https://rescue-rush.lu/", "https://rescue.lu/");
			//@formatter:off
		}

		LOGGER.info("Registered " + registry.getAllBeans().length + " WebSocket handlers. [" + registry.getBeans().values().stream().map(WSHandlerData::path).collect(Collectors.joining(", ")) + "]");
	}

}
