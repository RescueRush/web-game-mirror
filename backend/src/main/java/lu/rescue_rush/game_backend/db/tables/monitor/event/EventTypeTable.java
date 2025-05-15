package lu.rescue_rush.game_backend.db.tables.monitor.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.async.NextTask;
import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.TableHelper;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.db.data.monitor.event.EventTypeEntryData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.monitoring.EventMonitor;

//@formatter:off
@DB_Table(name = "event_type", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "key", type = "varchar(50)"),
		@Column(name = "name", type = "varchar(100)"),
		@Column(name = "desc", type = "text", notNull = false, default_ = "NULL")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_event_type_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_event_type_key", columns = "key")
})
//@formatter:on
@Service
public class EventTypeTable extends R2DBTable<EventTypeEntryData> {

	private static final Logger LOGGER = Logger.getLogger(EventTypeTable.class.getName());

	public EventTypeTable(DataBase dbTest) {
		super(dbTest);
	}

	@PostConstruct
	public void init() {
		super.init();

		//@formatter:off
		NextTask.collector(
				safeInsert(EventMonitor.R2API_START, "API startup", null),
				safeInsert(EventMonitor.R2API_STOP, "API shutdown", null),
				safeInsert(EventMonitor.R2API_PERFTRACKER_START, "Executiontracker start", null),
				safeInsert(EventMonitor.R2API_PERFTRACKER_STOP, "Executiontracker stop", null),
				safeInsert(EventMonitor.R2API_PERFTRACKER_SAVE, "Executiontracker periodic save", null),
				safeInsert(EventMonitor.R2API_PERFTRACKER_FORCE_SAVE, "Executiontracker force save", null)
		).thenConsume((s) -> LOGGER.info("Set up " + s.size() + " default event types."))
		.catch_(PCUtils::throw_)
		.run();
		//@formatter:on
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user-tracking-type.ids", key = "#id")
	public EventTypeEntryData byId(int id) {
		return super.load(new EventTypeEntryData(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while loading data: " + id, e)).run();
	}

	@Cacheable(value = "user-tracking-type.keys", key = "#key")
	public int loadOrInsertByKey(String key) {
		return TableHelper.insertOrLoad(this, new EventTypeEntryData(key, "gen-" + key, null), () -> EventTypeEntryData.byKey(key))
				.catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: " + key, e)).run().getId();
	}

	/*
	 * ========== UTILS ==========
	 */

	public NextTask<Void, EventTypeEntryData> safeInsert(String key, String name, String desc) {
		return TableHelper.insertOrLoad(this, new EventTypeEntryData(key, name, desc), () -> EventTypeEntryData.byKey(key))
				.catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: (" + key + ", " + name + ", " + desc + ")", e));
	}

}
