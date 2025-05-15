package lu.rescue_rush.game_backend.sb.component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.sb.ws.R2WSHandler;
import lu.rescue_rush.game_backend.sb.ws.WSMappingRegistry;

@Component
public class HandlerMethodResolver implements ApplicationContextAware {

	public static final String ATTR_HANDLER = "handlerMethod";

	private List<RequestMappingHandlerMapping> mappings;

	@Autowired
	private WSMappingRegistry wsMappingRegistry;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.mappings = new ArrayList<>(applicationContext.getBeansOfType(RequestMappingHandlerMapping.class).values());
	}

	public AbstractRequestHandler<?> resolve(HttpServletRequest request) {
		if (request.getAttribute(ATTR_HANDLER) != null) {
			return (AbstractRequestHandler<?>) request.getAttribute(ATTR_HANDLER);
		}

		if (wsMappingRegistry.hasBean(request.getRequestURI())) {
			final WSHandlerBean reqHandler = new WSHandlerBean(wsMappingRegistry.getBean(request.getRequestURI()));
			request.setAttribute(ATTR_HANDLER, reqHandler);
			return reqHandler;
		}

		for (RequestMappingHandlerMapping mapping : mappings) {
			try {
				HandlerExecutionChain handler = mapping.getHandler(request);
				if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {
					final HTTPHandlerMethod reqHandler = new HTTPHandlerMethod(handlerMethod);
					request.setAttribute(ATTR_HANDLER, reqHandler);
					return reqHandler;
				}
			} catch (Exception ignored) {
				ignored.printStackTrace();
			}
		}

		return null;
	}

	public sealed abstract class AbstractRequestHandler<T> permits HTTPHandlerMethod, WSHandlerBean {

		private T obj;

		public AbstractRequestHandler(T obj) {
			this.obj = obj;
		}

		public abstract boolean hasAnnotation(Class<? extends Annotation> annotation);

		public abstract boolean isWSHandler();

		public abstract boolean isHTTPHandler();

		public T get() {
			return obj;
		}

	}

	public final class HTTPHandlerMethod extends AbstractRequestHandler<HandlerMethod> {

		public HTTPHandlerMethod(HandlerMethod obj) {
			super(obj);
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return super.obj.hasMethodAnnotation(annotation);
		}

		@Override
		public boolean isWSHandler() {
			return false;
		}

		@Override
		public boolean isHTTPHandler() {
			return true;
		}

	}

	public final class WSHandlerBean extends AbstractRequestHandler<R2WSHandler> {

		public WSHandlerBean(R2WSHandler obj) {
			super(obj);
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return super.obj.getClass().isAnnotationPresent(annotation);
		}

		@Override
		public boolean isWSHandler() {
			return true;
		}

		@Override
		public boolean isHTTPHandler() {
			return false;
		}

	}

}
