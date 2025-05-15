package lu.rescue_rush.game_backend.sb.ws;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.sb.ws.WSMappingRegistry.WSHandlerMethod;

@Component
public class WSMappingScanner implements ApplicationContextAware {

	private final Logger LOGGER = Logger.getLogger(WSMappingScanner.class.getName());

	@Autowired
	private WSMappingRegistry registry;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		for (Object bean : applicationContext.getBeansWithAnnotation(WSMapping.class).values()) {

			if (!(bean instanceof R2WSHandler)) {
				LOGGER.warning("Bean " + bean.getClass().getName() + " is not a WSHandler. Skipping.");
				continue;
			}

			Class<?> target = AopProxyUtils.ultimateTargetClass(bean);

			if (!target.isAnnotationPresent(WSMapping.class)) {
				continue;
			}

			final WSMapping beanMapping = target.getAnnotation(WSMapping.class);

			final Map<String, WSHandlerMethod> methods = new ConcurrentHashMap<>();

			for (Method proxyMethod : bean.getClass().getDeclaredMethods()) {
				Method targetMethod = null;
				try {
					targetMethod = target.getDeclaredMethod(proxyMethod.getName(), proxyMethod.getParameterTypes());
				} catch (NoSuchMethodException e) {
					// LOGGER.warning("Proxy method `" + proxyMethod.getName() + "` not found in
					// target class. Skipping.");
					continue;
				} catch (SecurityException se) {
					LOGGER.warning("Security exception while accessing method `" + proxyMethod.getName() + "` in target class. Skipping.");
					continue;
				}

				if (!targetMethod.isAnnotationPresent(WSMapping.class)) {
					continue;
				}

				final WSMapping mapping = targetMethod.getAnnotation(WSMapping.class);
				final WSResponseMapping responseMapping = targetMethod.getAnnotation(WSResponseMapping.class);

				final String inPath = normalizeURI(mapping.path());
				final String outPath = normalizeURI(responseMapping == null ? mapping.path() : responseMapping.path());

				methods.put(mapping.path(), new WSHandlerMethod(proxyMethod, inPath, outPath));
			}

			registry.register(beanMapping.path(), (R2WSHandler) bean, methods);
			// LOGGER.info("Discovered WebSocket handler for path: " + beanMapping.path());
		}

		// LOGGER.info("Discovered " + registry.getAllBeans().length + " WebSocket
		// handlers.");
	}

	public static String normalizeURI(String path) {
		if (path == null || path.isEmpty())
			return "/";
		String trimmed = path.replaceAll("^/+", "").replaceAll("/+$", "");
		return "/" + trimmed;
	}

}
