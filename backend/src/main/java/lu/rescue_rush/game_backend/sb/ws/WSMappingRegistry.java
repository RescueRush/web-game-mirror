package lu.rescue_rush.game_backend.sb.ws;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class WSMappingRegistry {

	@Autowired
	private ApplicationContext context;

	private final Map<String, WSHandlerData> beanMap = new ConcurrentHashMap<>();

	public void register(String path, R2WSHandler bean, Map<String, WSHandlerMethod> methods) {
		R2WebSocketHandler attachedHandler = context.getBean(R2WebSocketHandler.class, path, bean, methods);
		bean.setWebSocketHandler(attachedHandler);
		beanMap.put(path, new WSHandlerData(path, bean, methods, attachedHandler));
	}

	public String[] getAllBeans() {
		return beanMap.keySet().toArray(new String[0]);
	}

	public Map<String, WSHandlerData> getBeans() {
		return beanMap;
	}

	public WSHandlerData getBeanData(String path) {
		return beanMap.get(path);
	}

	public R2WSHandler getBean(String path) {
		if (beanMap.containsKey(path)) {
			return beanMap.get(path).bean;
		}
		return null;
	}

	public record WSHandlerMethod(Method method, String inPath, String outPath) {
	}

	public record WSHandlerData(String path, R2WSHandler bean, Map<String, WSHandlerMethod> methods, R2WebSocketHandler handler) {

		public WSHandlerMethod getDestination(String dest) {
			return methods.get(dest);
		}

	}

	public boolean hasBean(String path) {
		return beanMap.containsKey(path);
	}

}
