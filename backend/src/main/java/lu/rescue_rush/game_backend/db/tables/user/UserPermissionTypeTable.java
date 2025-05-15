package lu.rescue_rush.game_backend.db.tables.user;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import lu.rescue_rush.game_backend.db.data.user.UserPermissionTypeData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;

//@formatter:off
@DB_Table(name = "user_permission_type", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "name", type = "varchar(35)"),
		@Column(name = "key", type = "varchar(35)"),
		@Column(name = "description", type = "text", notNull = false)
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_user_permission_type_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_user_permission_type_key", columns = "key")
})
//@formatter:on
@Service
public class UserPermissionTypeTable extends R2DBTable<UserPermissionTypeData> {

	private static final Logger LOGGER = Logger.getLogger(UserPermissionTypeTable.class.getName());

	public UserPermissionTypeTable(DataBase dbTest) {
		super(dbTest);
	}

	@PostConstruct
	public void init() {
		super.init();

		//@formatter:off
		NextTask.collector(
				safeInsert(UserPermissionTypeData.KEY_SUPERUSER, "Superuser", "Contains all other permissions."),
				safeInsert(UserPermissionTypeData.KEY_USER_BAN, "Ban", "Ban users."),
				safeInsert(UserPermissionTypeData.KEY_USER_HIDE, "Hide", "Hide users."),
				safeInsert(UserPermissionTypeData.KEY_NEWSLETTER_EMAILS_QUERY, "Newsletter email query", "Query the newsletter emailing list.")
		).thenConsume((s) -> LOGGER.info("Set up " + s.size() + " default user permission types types."))
		.catch_(PCUtils::throw_)
		.run();
		//@formatter:on
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user-permission-types.ids", key = "#id")
	public UserPermissionTypeData byId(int id) {
		return super.query(UserPermissionTypeData.byId(id)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
	}

	@Cacheable(value = "user-permission-types.keys", key = "#key")
	public UserPermissionTypeData byKey(String key) {
		return super.query(UserPermissionTypeData.byKey(key)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + key, e)).run();
	}

	public List<UserPermissionTypeData> all() {
		return super.query(UserPermissionTypeData.all()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data (all) ", e)).run();
	}

	/*
	 * ========== UPDATE ==========
	 */

	/**
	 * You're not supposed to do this
	 */
	//@formatter:off
	@Caching(put={
			@CachePut(value="user-permission-types.ids", key="#userPermissionTypeData.id"),
			@CachePut(value="user-permission-types.keys", key="#userPermissionTypeData.key")
	})
	//@formatter:on
	@Deprecated
	public UserPermissionTypeData updateUserPermissionTypeData(UserPermissionTypeData userPermissionTypeData) {
		return super.update(userPermissionTypeData).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + userPermissionTypeData, e)).run();
	}

	/*
	 * ========== UTILS ==========
	 */

	public NextTask<Void, UserPermissionTypeData> safeInsert(String key, String name, String msg) {
		return TableHelper.insertOrLoad(this, new UserPermissionTypeData(name, key, msg), () -> UserPermissionTypeData.byKey(key));
	}

}
