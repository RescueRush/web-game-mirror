package lu.rescue_rush.game_backend.sb.ws;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.springframework.web.socket.WebSocketSession;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.sb.ws.R2WebSocketHandler.ScheduledTaskData;
import lu.rescue_rush.game_backend.sb.ws.R2WebSocketHandler.WebSocketSessionData;

public abstract class R2WSHandler {

	private R2WebSocketHandler webSocketHandler;

	public void send(WebSocketSessionData sessionData, String destination, String packetId, Object payload) {
		webSocketHandler.send(sessionData, destination, packetId, payload);
	}

	public void send(WebSocketSessionData sessionData, String destination, Object payload) {
		webSocketHandler.send(sessionData, destination, payload);
	}

	public void send(WebSocketSession session, String destination, String packetId, Object payload) {
		webSocketHandler.send(session, destination, packetId, payload);
	}

	public void send(WebSocketSession session, String destination, Object payload) {
		webSocketHandler.send(session, destination, payload);
	}

	public void send(UserData user, String destination, String packetId, Object payload) {
		webSocketHandler.send(user, destination, packetId, payload);
	}

	public void send(UserData user, String destination, Object payload) {
		webSocketHandler.send(user, destination, payload);
	}

	public void cancelScheduledTasks(WebSocketSession session) {
		webSocketHandler.cancelScheduledTasks(session);
	}

	public void cancelScheduledTasks(WebSocketSessionData sessionData) {
		webSocketHandler.cancelScheduledTasks(sessionData);
	}

	public void clearScheduledTasks(WebSocketSession session, String matchingId) {
		webSocketHandler.clearScheduledTasks(session, matchingId);
	}

	public void clearScheduledTasks(WebSocketSessionData sessionData, String matchingId) {
		webSocketHandler.clearScheduledTasks(sessionData, matchingId);
	}

	public void clearScheduledTasks(WebSocketSession session, Predicate<ScheduledTaskData<?>> pred) {
		webSocketHandler.clearScheduledTasks(session, pred);
	}

	public void clearScheduledTasks(WebSocketSessionData sessionData, Predicate<ScheduledTaskData<?>> pred) {
		webSocketHandler.clearScheduledTasks(sessionData, pred);
	}

	public Collection<ScheduledTaskData<?>> getScheduledTasks(WebSocketSession session) {
		return webSocketHandler.getScheduledTasks(session);
	}

	public Collection<ScheduledTaskData<?>> getScheduledTasks(WebSocketSessionData sessionData) {
		return webSocketHandler.getScheduledTasks(sessionData);
	}

	public <T> void scheduleTask(WebSocketSessionData sessionData, Runnable run, String id, long delay, TimeUnit unit) {
		webSocketHandler.scheduleTask(sessionData, run, id, delay, unit);
	}

	public <T> void scheduleTask(WebSocketSessionData sessionData, Callable<T> run, String id, long delay, TimeUnit unit) {
		webSocketHandler.scheduleTask(sessionData, run, id, delay, unit);
	}

	public <T> void scheduleTask(WebSocketSession session, Runnable run, String id, long delay, TimeUnit unit) {
		webSocketHandler.scheduleTask(session, run, id, delay, unit);
	}

	public <T> void scheduleTask(WebSocketSession session, Callable<T> run, String id, long delay, TimeUnit unit) {
		webSocketHandler.scheduleTask(session, run, id, delay, unit);
	}

	public R2WebSocketHandler getWebSocketHandler() {
		return webSocketHandler;
	}

	public void setWebSocketHandler(R2WebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

}
