package lu.rescue_rush.game_backend.utils.monitoring;

import org.springframework.lang.Nullable;

import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.monitor.event.EventEntryData;

public class EventMonitor {

	//@formatter:off
	public static final String
			R2API_START = "r2api.start",
			R2API_STOP = "r2api.end",
			R2API_PERFTRACKER_SAVE = "r2api.perftracker.save",
			R2API_PERFTRACKER_FORCE_SAVE = "r2api.perftracker.force_save",
			R2API_PERFTRACKER_START = "r2api.perftracker.start",
			R2API_PERFTRACKER_STOP = "r2api.perftracker.stop",
			R2API_SHUTDOWN_CONFIRMED = "r2api.shutdown.confirmed";
	//@formatter:on

	public static void push(String key, @Nullable String desc) {
		TableProxy.EVENTS.insert(new EventEntryData(TableProxy.EVENT_TYPES.loadOrInsertByKey(key), desc)).runAsync();
	}

	public static void push(String key) {
		push(key, null);
	}

}
