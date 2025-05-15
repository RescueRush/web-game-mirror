package lu.rescue_rush.game_backend.db.tables.monitor.interaction;

import java.util.List;
import java.util.logging.Level;

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
import lu.rescue_rush.game_backend.db.data.monitor.interaction.InteractionMonitoringTypeEntryData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

//@formatter:off
@DB_Table(name = "interaction_monitor_type", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "key", type = "varchar(50)"),
		@Column(name = "name", type = "varchar(100)"),
		@Column(name = "desc", type = "text", notNull = false, default_ = "NULL")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_interaction_monitor_type_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_interaction_monitor_type_key", columns = "key")
})
//@formatter:on
@Service
public class InteractionMonitorTypeTable extends R2DBTable<InteractionMonitoringTypeEntryData> {

	public InteractionMonitorTypeTable(DataBase dbTest) {
		super(dbTest);
	}

	@PostConstruct
	public void init() {
		super.init();

		//@formatter:off
		NextTask.collector(
				safeInsert(InteractionMonitor.R2API_USER_REGISTER, "New user registered", null),
				safeInsert(InteractionMonitor.R2API_USER_LOGIN, "User logged in", null),
				safeInsert(InteractionMonitor.R2API_USER_LOGOUT, "User logged out", null),
				safeInsert(InteractionMonitor.R2API_USER_LOGOUT_ALL, "User logged out from all devices", "Logged out and deleted token."),
				safeInsert(InteractionMonitor.R2API_USER_DELETE, "User deleted", null),
				safeInsert(InteractionMonitor.R2API_USER_TOKEN_VALID, "Token valid", "Checks if the given token is valid and extends its validity period."),
				safeInsert(InteractionMonitor.R2API_USER_LEADERBOARD, "Fetch leaderboard", null),
				safeInsert(InteractionMonitor.R2API_USER_UPDATE_NAME, "Username updated", null),
				safeInsert(InteractionMonitor.R2API_USER_UPDATE_PASS, "Password updated", null),
				safeInsert(InteractionMonitor.R2API_USER_UPDATE_EMAIL, "Email updated", null),
				safeInsert(InteractionMonitor.R2API_GAME_CREATED, "Game created", null),
				safeInsert(InteractionMonitor.R2API_GAME_END, "Game ended", null),
				safeInsert(InteractionMonitor.R2API_GAME_ANSWER, "Game answer", null),
				safeInsert(InteractionMonitor.R2API_GAME_NEXT_QUESTION, "Game next question", null),
				safeInsert(InteractionMonitor.R2API_GAME_STATE, "Game state", null),
				safeInsert(InteractionMonitor.R2API_ADMIN_EVENTS, "Admin events", "Admin requested a list of events"),
				safeInsert(InteractionMonitor.R2API_SUPPORT_FORM_EMAIL, "Support form email submitted", null),
				safeInsert(InteractionMonitor.R2API_NEWSLETTER_SUBSCRIBE, "Subscribed email to newsletter", null),
				safeInsert(InteractionMonitor.R2API_NEWSLETTER_UNSUBSCRIBE, "Unsubscribed email to newsletter", null),
				safeInsert(InteractionMonitor.R2API_LOGS_HOMEPAGE, "Traffic request count log", null),
				safeInsert(InteractionMonitor.R2API_INVALID_ENDPOINT, "Invalid endpoint requested", null)
		).thenConsume((s) -> LOGGER.info("Set up " + s.size() + " default user tracking types."))
		.catch_(PCUtils::throw_)
		.run();
		//@formatter:on
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user-tracking-type.keys", key = "#key")
	public int loadOrInsertByKey(String key) {
		return TableHelper.insertOrLoad(this, new InteractionMonitoringTypeEntryData(key, "gen-" + key, null), () -> InteractionMonitoringTypeEntryData.byKey(key))
				.catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: " + key, e)).run().getId();
	}

	/*
	 * ========== UTILS ==========
	 */

	public NextTask<Void, InteractionMonitoringTypeEntryData> safeInsert(String key, String name, String desc) {
		return TableHelper.insertOrLoad(this, new InteractionMonitoringTypeEntryData(key, name, desc), () -> InteractionMonitoringTypeEntryData.byKey(key))
				.catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: (" + key + ", " + name + ", " + desc + ")", e));
	}

	@Cacheable(value = "user-tracking-type.id", key = "#id")
	public InteractionMonitoringTypeEntryData byId(int id) {
		return super.query(InteractionMonitoringTypeEntryData.byId(id)).thenApply(SpringUtils.first()).run();
	}

	public List<InteractionMonitoringTypeEntryData> loadEvents() {
		return super.query(InteractionMonitoringTypeEntryData.loadAll()).run();
	}

}
