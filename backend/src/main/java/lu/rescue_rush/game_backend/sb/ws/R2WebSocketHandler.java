package lu.rescue_rush.game_backend.sb.ws;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.sb.ws.WSMappingRegistry.WSHandlerMethod;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@Component
@Scope("prototype")
public class R2WebSocketHandler extends TextWebSocketHandler {

	private static final long TIMEOUT = 60_000, // 60 seconds
			PERIODIC_CHECK_DELAY = 10_000;

	private final Logger LOGGER;

	private final String beanPath;
	private final R2WSHandler bean;
	private final Map<String, WSHandlerMethod> methods;

	private final Map<Integer, WebSocketSession> wsSessions = new ConcurrentHashMap<>();
	private final Map<Integer, WebSocketSessionData> userSessions = new ConcurrentHashMap<>();

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	private final Map<String, List<ScheduledTaskData<?>>> scheduledTasks = new ConcurrentHashMap<>();

	public R2WebSocketHandler(String path, R2WSHandler bean, Map<String, WSHandlerMethod> methods) {
		this.beanPath = path;
		this.bean = bean;
		this.methods = methods.entrySet().stream().collect(Collectors.toMap(k -> WSMappingScanner.normalizeURI(k.getKey()), v -> v.getValue()));
		this.LOGGER = Logger.getLogger("WebSocketHandler " + path);

		executorService.scheduleAtFixedRate(() -> {
			userSessions.entrySet().removeIf(entry -> {
				WebSocketSessionData sessionData = entry.getValue();
				if (!sessionData.isAlive()) {
					try {
						if (R2ApiMain.DEBUG) {
							LOGGER.info("Removed timed out session (" + sessionData.getSession().getAttributes().get("user") + ")");
						}
						sessionData.getSession().close(CloseStatus.NORMAL);
					} catch (IOException e) {
						LOGGER.warning("Failed to close session: " + e.getMessage());
					}
					return true;
				}
				return false;
			});

			scheduledTasks.entrySet().forEach(e -> e.getValue().removeIf(t -> t.isCancelled() || t.isDone()));
			scheduledTasks.entrySet().removeIf(e -> e.getValue().isEmpty());
		}, PERIODIC_CHECK_DELAY, PERIODIC_CHECK_DELAY, TimeUnit.MILLISECONDS);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		final UserData user = (UserData) session.getAttributes().get("user");
		// TODO: @UnKabaraQuiDev add Anonymous ?
		if (user == null) {
			LOGGER.warning("User not authenticated.");
			return;
		}

		wsSessions.put(user.getId(), session);
		userSessions.put(user.getId(), new WebSocketSessionData((Authentication) session.getAttributes().get("auth"), user, this.beanPath));
		SpringUtils.setContextUser(user);

		SpringUtils.setRequestSource((String) session.getAttributes().get("httpRequest.source"));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		final JsonNode json = new ObjectMapper().readTree(message.getPayload());
		SpringUtils.badRequest(!json.has("destination"), "Invalid packet.");
		String requestPath = json.get("destination").asText();
		final String packetId = json.has("packetId") ? json.get("packetId").asText() : null;
		final JsonNode payload = json.get("payload");

		final Authentication auth = (Authentication) session.getAttributes().get("auth");
		final UserData ud = SpringUtils.setContextAuthentication(auth);

		final WSHandlerMethod handlerMethod = resolveMethod(requestPath);
		SpringUtils.badRequest(handlerMethod == null, "No method found for destination: " + requestPath);
		final Method method = handlerMethod.method();
		SpringUtils.badRequest(handlerMethod == null, "No method attached for destination: " + requestPath);
		requestPath = handlerMethod.inPath();
		final String responsePath = handlerMethod.outPath();

		final WebSocketSessionData userSession = userSessions.get(ud.getId());
		userSession.setRequestPath(requestPath);
		userSession.setPacketId(packetId);
		userSession.lastPing = System.currentTimeMillis();

		SpringUtils.setRequestSource((String) session.getAttributes().get("httpRequest.source"));

		SpringUtils.setContextLocale((Locale) session.getAttributes().get("locale"));

		Exception err = null;
		try {
			Object returnValue = null;

			if (method.getParameterCount() == 2) {
				final Class<?> parameterType = method.getParameterTypes()[1];
				SpringUtils.badRequest(payload == null, "Payload expected for destination: " + requestPath);
				final Object param = new ObjectMapper().readValue(payload.toString(), parameterType);

				returnValue = method.invoke(bean, userSession, param);
			} else if (method.getParameterCount() == 1) {
				returnValue = method.invoke(bean, userSession);
			} else {
				LOGGER.warning("Method " + method.getName() + " has an invalid number of parameters: " + method.getParameterCount());
				return;
			}

			final ObjectMapper objectMapper = new ObjectMapper();
			final ObjectNode root = objectMapper.createObjectNode();
			root.set("payload", objectMapper.valueToTree(returnValue));
			root.put("destination", responsePath);
			if (packetId != null) {
				root.put("packetId", packetId);
			}
			final String jsonResponse = objectMapper.writeValueAsString(root);

			session.sendMessage(new TextMessage(jsonResponse));
		} catch (Exception e) {
			err = e;
		} finally {
			SpringUtils.clearContextUser();
			SpringUtils.clearRequestSource();
			SpringUtils.clearContextLocale();
		}

		if (err != null) {
			throw err;
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		if (session.getAttributes().get("user") == null) {
			LOGGER.warning("User not auth. Invalid session: " + session);
			return;
		}
		wsSessions.remove(((UserData) session.getAttributes().get("user")).getId());
		userSessions.remove(((UserData) session.getAttributes().get("user")).getId());
	}

	public boolean cancelScheduledTasks(WebSocketSession session) {
		final String sessionId = session.getId();
		if (!scheduledTasks.containsKey(sessionId)) {
			return false;
		}
		scheduledTasks.get(sessionId).forEach(ScheduledTaskData::cancel);
		return !scheduledTasks.get(sessionId).isEmpty();
	}

	public boolean cancelScheduledTasks(WebSocketSessionData sessionData) {
		return cancelScheduledTasks(sessionData.getSession());
	}

	public boolean clearScheduledTasks(WebSocketSessionData sessionData, String matchingId) {
		Objects.requireNonNull(matchingId);
		return clearScheduledTasks(sessionData.getSession(), matchingId);
	}

	public boolean clearScheduledTasks(WebSocketSession session, String matchingId) {
		Objects.requireNonNull(matchingId);
		return clearScheduledTasks(session, (std) -> matchingId.equals(std.id));
	}

	public boolean clearScheduledTasks(WebSocketSession session, Predicate<ScheduledTaskData<?>> pred) {
		Objects.requireNonNull(session);
		Objects.requireNonNull(pred);
		if (!scheduledTasks.containsKey(session.getId())) {
			return false;
		}
		scheduledTasks.get(session.getId()).stream().filter(pred).forEach(t -> t.cancel());
		final boolean removed = scheduledTasks.get(session.getId()).removeIf(pred);
		return removed;
	}

	public boolean clearScheduledTasks(WebSocketSessionData sessionData, Predicate<ScheduledTaskData<?>> pred) {
		return clearScheduledTasks(sessionData.getSession(), pred);
	}

	public Collection<ScheduledTaskData<?>> getScheduledTasks(WebSocketSession session) {
		return scheduledTasks.get(session.getId());
	}

	public Collection<ScheduledTaskData<?>> getScheduledTasks(WebSocketSessionData sessionData) {
		return getScheduledTasks(sessionData.getSession());
	}

	public <T> void scheduleTask(WebSocketSessionData sessionData, Runnable run, String id, long delay, TimeUnit unit) {
		Objects.requireNonNull(sessionData);
		scheduleTask(sessionData.getSession(), run, id, delay, unit);
	}

	public <T> void scheduleTask(WebSocketSession session, Runnable run, String id, long delay, TimeUnit unit) {
		scheduleTask(session, (Callable<Void>) () -> {
			run.run();
			return null;
		}, id, delay, unit);
	}

	public <T> void scheduleTask(WebSocketSessionData sessionData, Callable<T> run, String id, long delay, TimeUnit unit) {
		Objects.requireNonNull(sessionData);
		scheduleTask(sessionData.getSession(), run, id, delay, unit);
	}

	public <T> void scheduleTask(WebSocketSession session, Callable<T> run, String id, long delay, TimeUnit unit) {
		Objects.requireNonNull(session);

		final ScheduledFuture<T> newTask = executorService.schedule(run, delay, unit);

		scheduledTasks.computeIfAbsent(session.getId(), (k) -> Collections.synchronizedList(new ArrayList<ScheduledTaskData<?>>()));
		scheduledTasks.get(session.getId()).add(new ScheduledTaskData<T>(id, newTask));
	}

	public void send(WebSocketSession session, String destination, String packetId, Object payload) {
		Objects.requireNonNull(session);
		Objects.requireNonNull(destination);

		if (session.isOpen()) {
			try {
				final ObjectMapper objectMapper = new ObjectMapper();
				final ObjectNode root = objectMapper.createObjectNode();
				root.set("payload", objectMapper.valueToTree(payload));
				root.put("destination", destination);
				if (packetId != null) {
					root.put("packetId", packetId);
				}
				final String json = objectMapper.writeValueAsString(root);

				session.sendMessage(new TextMessage(json));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			LOGGER.warning("Session is closed: " + session);
		}
	}

	public void send(WebSocketSessionData sessionData, String destination, String packetId, Object payload) {
		Objects.requireNonNull(sessionData);
		send(sessionData.getSession(), destination, packetId, payload);
	}

	public void send(WebSocketSessionData sessionData, String destination, Object payload) {
		Objects.requireNonNull(sessionData);
		send(sessionData.getSession(), destination, null, payload);
	}

	public void send(WebSocketSession session, String destination, Object payload) {
		send(session, destination, null, payload);
	}

	public void send(UserData user, String destination, String packetId, Object payload) {
		WebSocketSession session = userSessions.get(user.getId()).getSession();
		send(session, destination, packetId, payload);
	}

	public void send(UserData user, String destination, Object payload) {
		send(user, destination, null, payload);
	}

	public WSHandlerMethod resolveMethod(String requestPath) {
		requestPath = WSMappingScanner.normalizeURI(requestPath);
		final List<String> matchingPatterns = new ArrayList<>();
		for (String pattern : methods.keySet()) {
			if (pathMatcher.match(pattern, requestPath)) {
				matchingPatterns.add(pattern);
			}
		}
		matchingPatterns.sort(pathMatcher.getPatternComparator(requestPath));
		String bestPattern = matchingPatterns.get(0);
		WSHandlerMethod bestMatch = methods.get(bestPattern);

		return bestMatch;
	}

	public record ScheduledTaskData<T>(String id, ScheduledFuture<T> future) {

		public String getId() {
			return id;
		}

		public boolean isDone() {
			return future.isDone();
		}

		public boolean isCancelled() {
			return future.isCancelled();
		}

		public T get() throws InterruptedException, ExecutionException {
			return future.get();
		}

		public boolean cancel() {
			return future.cancel(false);
		}

		public boolean cancelForce() {
			return future.cancel(true);
		}

	}

	public class WebSocketSessionData {

		private Authentication auth;
		private UserData user;
		private long lastPing = System.currentTimeMillis();
		private String wsPath, requestPath, packetId;

		public WebSocketSessionData(Authentication auth, UserData user, String wsPath) {
			this.auth = auth;
			this.user = user;
			this.wsPath = wsPath;
		}

		public WebSocketSession getSession() {
			return wsSessions.get(user.getId());
		}

		public Authentication getAuth() {
			return auth;
		}

		public UserData getUser() {
			return user;
		}

		public long getLastPing() {
			return lastPing;
		}

		public boolean isAlive() {
			return System.currentTimeMillis() - lastPing < TIMEOUT;
		}

		public String getWsPath() {
			return wsPath;
		}

		public String getRequestPath() {
			return requestPath;
		}

		public void setRequestPath(String requestPath) {
			this.requestPath = requestPath;
		}

		public String getPacketId() {
			return packetId;
		}

		public void setPacketId(String packetId) {
			this.packetId = packetId;
		}

		public boolean hasPacketId() {
			return packetId != null;
		}

		public void clearContext() {
			this.requestPath = null;
			this.packetId = null;
		}

	}

}
