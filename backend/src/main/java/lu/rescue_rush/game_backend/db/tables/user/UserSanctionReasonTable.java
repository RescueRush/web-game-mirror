package lu.rescue_rush.game_backend.db.tables.user;

import java.util.List;
import java.util.logging.Level;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.async.NextTask;
import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.TableHelper;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionReasonData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;

//@formatter:off
@DB_Table(name = "user_sanction_reason", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "name", type = "varchar(35)"),
		@Column(name = "key", type = "varchar(35)"),
		@Column(name = "description", type = "text", notNull = false)
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_user_sanction_reasons_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_user_sanction_reasons_key", columns = "key")
})
//@formatter:on
@Service
public class UserSanctionReasonTable extends R2DBTable<UserSanctionReasonData> {

	public UserSanctionReasonTable(DataBase dbTest) {
		super(dbTest);
	}

	@PostConstruct
	public void init() {
		super.init();

		//@formatter:off
		NextTask.collector(
				safeInsert("Warn", UserSanctionReasonData.KEY_WARN, "You got warned."),
				safeInsert("Ban", UserSanctionReasonData.KEY_BAN, "You got banned."),
				safeInsert("Kick", UserSanctionReasonData.KEY_KICK, "You got kicked."),
				safeInsert("Mute", UserSanctionReasonData.KEY_MUTE, "You got muted."),
				safeInsert("Suspect", UserSanctionReasonData.KEY_SUSPECT, "You are suspected."),
				safeInsert("Hidden", UserSanctionReasonData.KEY_HIDDEN, "You are hidden.")
		).thenConsume((s) -> LOGGER.info("Set up " + s.size() + " default user sanction reasons."))
		.catch_(PCUtils::throw_)
		.run();
		//@formatter:on
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user-sanction-reason.ids", key = "#id")
	public UserSanctionReasonData byId(int id) {
		UserSanctionReasonData usrd = super.query(UserSanctionReasonData.byId(id)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
		return usrd;
	}

	@Cacheable(value = "user-sanction-reason.keys", key = "#key")
	public UserSanctionReasonData byKey(String key) {
		UserSanctionReasonData usrd = super.query(UserSanctionReasonData.byKey(key)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + key, e)).run();
		return usrd;
	}

	public List<UserSanctionReasonData> all() {
		return super.query(UserSanctionReasonData.all()).run();
	}

	/*
	 * ========== UPDATE ==========
	 */

	/**
	 * You're not supposed to do this
	 */
	//@formatter:off
	@Caching(put={
			@CachePut(value="user-sanction-reason.ids", key="#userSanctionReasonData.id"),
			@CachePut(value="user-sanction-reason.keys", key="#userSanctionReasonData.key")
	})
	//@formatter:on
	@Deprecated
	public UserSanctionReasonData updateUserSanctionReasonData(UserSanctionReasonData userSanctionReasonData) {
		return super.update(userSanctionReasonData).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + userSanctionReasonData, e)).run();
	}

	/*
	 * ========== UTILS ==========
	 */

	public NextTask<Void, UserSanctionReasonData> safeInsert(String name, String key, String msg) {
		return TableHelper.insertOrLoad(this, new UserSanctionReasonData(name, key, msg), () -> UserSanctionReasonData.byKey(key));
	}

}
