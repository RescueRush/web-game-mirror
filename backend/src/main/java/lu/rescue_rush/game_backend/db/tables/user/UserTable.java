package lu.rescue_rush.game_backend.db.tables.user;

import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.views.user.User_ActiveUsers_View;
import lu.rescue_rush.game_backend.db.views.user.User_NewestLogin_View;
import lu.rescue_rush.game_backend.db.views.user.User_NewestRegister_View;
import lu.rescue_rush.game_backend.db.views.user.User_NoToken_View;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

//@formatter:off
@DB_Table(name = "user", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "name", type = "varchar(36)"),
		@Column(name = "email", type = "varchar(320)"),
		@Column(name = "lang", type = "enum('LB', 'EN', 'FR', 'DE')", default_ = "'LB'"),
		@Column(name = "pass", type = "varchar(128)"),
		@Column(name = "token", type = "varchar(128)", notNull = false),
		@Column(name = "join_date", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "last_login", type = "timestamp", notNull = false, default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "last_request", type = "timestamp", notNull = false, default_ = "CURRENT_TIMESTAMP")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_user_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_user_email", columns = "email"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_user_token", columns = "token"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_user_name", columns = "name")
})
//@formatter:on
@Service
public class UserTable extends R2DBTable<UserData> {

	@Autowired
	@Lazy
	public User_ActiveUsers_View ACTIVE_USERS;
	@Autowired
	@Lazy
	public User_NewestLogin_View NEWEST_LOGIN;
	@Autowired
	@Lazy
	public User_NewestRegister_View NEWEST_REGISTER;
	@Autowired
	@Lazy
	public User_NoToken_View NO_TOKEN;

	public UserTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== REQUEST SAFE ==========
	 */

	@RequestSafe
	@Cacheable(value = "user.logins", key = "#user + ':' + T(lu.rescue_rush.game_backend.db.data.user.UserData).hashPass(#pass)")
	public UserData requestSafe_byLogin(String user, String pass) {
		UserData ud = byLogin(user, pass);
		SpringUtils.notFound(ud == null, "User not found.");
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.logins", key = "#user + ':' + T(lu.rescue_rush.game_backend.db.data.user.UserData).hashPass(#pass)")
	public UserData requestSafe_byLogin(String user, String pass, Runnable else_) {
		UserData ud = byLogin(user, pass);
		SpringUtils.notFound(ud == null, "User not found.", else_);
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.tokens", key = "#token")
	public UserData requestSafe_byToken(String token) {
		UserData ud = byToken(token);
		SpringUtils.notFound(ud == null, "User not found.");
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.tokens", key = "#token")
	public UserData requestSafe_byToken(String token, Runnable else_) {
		UserData ud = byToken(token);
		SpringUtils.notFound(ud == null, "User not found.", else_);
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.ids", key = "#id")
	public UserData requestSafe_byId(int id) {
		UserData ud = byId(id);
		SpringUtils.notFound(ud == null, "User not found.");
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.ids", key = "#id")
	public UserData requestSafe_byId(int id, Runnable else_) {
		UserData ud = byId(id);
		SpringUtils.notFound(ud == null, "User not found.", else_);
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.emails", key = "#email")
	public UserData requestSafe_byEmail(String email) {
		UserData ud = byEmail(email);
		SpringUtils.notFound(ud == null, "User not found.");
		return ud;
	}

	@Cacheable(value = "user.emails", key = "#email")
	public UserData requestSafe_byEmail(String email, Runnable else_) {
		UserData ud = byEmail(email);
		SpringUtils.notFound(ud == null, "User not found.", else_);
		return ud;
	}

	@Cacheable(value = "user.names", key = "#name")
	public UserData requestSafe_byName(String name) {
		UserData ud = byName(name);
		SpringUtils.notFound(ud == null, "User not found.");
		return ud;
	}

	@RequestSafe
	@Cacheable(value = "user.names", key = "#name")
	public UserData requestSafe_byName(String name, Runnable else_) {
		UserData ud = byName(name);
		SpringUtils.notFound(ud == null, "User not found.", else_);
		return ud;
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user.logins", key = "#user + ':' + T(lu.rescue_rush.game_backend.db.data.user.UserData).hashPass(#pass)", unless = "#result == null")
	public UserData byLogin(String user, String pass) {
		UserData ud = super.query(UserData.login(user, pass)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: (" + user + ", " + pass + ")", e))
				.thenApply(SpringUtils.first(() -> null)).run();
		return ud;
	}

	@Cacheable(value = "user.tokens", key = "#token", unless = "#result == null")
	public UserData byToken(String token) {
		UserData ud = super.query(UserData.byToken(token)).thenApply(SpringUtils.first(() -> null)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + token, e)).run();
		return ud;
	}

	@Cacheable(value = "user.ids", key = "#id", unless = "#result == null")
	public UserData byId(int id) {
		UserData ud = super.query(UserData.byId(id)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
		return ud;
	}

	@Cacheable(value = "user.emails", key = "#email", unless = "#result == null")
	public UserData byEmail(String email) {
		return super.query(UserData.byEmail(email)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + email, e)).thenApply(SpringUtils.first()).run();
	}

	@Cacheable(value = "user.names", key = "#name", unless = "#result == null")
	public UserData byName(String name) {
		UserData ud = super.query(UserData.byName(name)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + name, e)).thenApply(SpringUtils.first()).run();
		return ud;
	}

	public List<UserData> byMatchingEmail(String email) {
		return super.query(UserData.byMatchingEmails(email)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying for datas for matching emails: " + email, e)).run();
	}

	public List<UserData> byMatchingEmail(String email, int limit) {
		return super.query(UserData.byMatchingEmails(email, limit)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying for datas for matching emails: " + email, e)).run();
	}

	public List<UserData> byMatchingName(String name) {
		return super.query(UserData.byMatchingNames(name)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying for datas for matching names: " + name, e)).run();
	}

	public List<UserData> byMatchingName(String name, int limit) {
		return super.query(UserData.byMatchingNames(name, limit)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying for datas for matching names: " + name, e)).run();
	}

	@Cacheable(value = "user_all", key = "'all'")
	public List<UserData> all() {
		return super.query(UserData.all()).run();
	}

	public List<UserData> byOffset(int offset, int limit) {
		return super.query(UserData.byOffset(offset, limit)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + limit + "/" + offset, e)).run();
	}

	/*
	 * ========== UTIL ==========
	 */

	public boolean nameExists(String name) {
		return super.query(UserData.byName(name)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + name, e)).run().size() > 0;
	}

	public boolean emailExists(String email) {
		return super.query(UserData.byEmail(email)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + email, e)).run().size() > 0;
	}

	public int countUsers() {
		return super.count().catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	/*
	 * ========== UPDATE ==========
	 */

	//@formatter:off
	@Caching(evict={
			@CacheEvict(value="user.logins", key="#userData.name + ':' + #userData.pass")
	})
	//@formatter:on
	public UserData forgetUserData(UserData ud) {
		return ud;
	}

	//@formatter:off
	@Caching(put={
	
			@CachePut(value="user.logins", key="#userData.name + ':' + #userData.pass"),
			@CachePut(value="user.tokens", key="#userData.token", condition = "#userData.token != null"),
			@CachePut(value="user.ids", key="#userData.id"),
			@CachePut(value="user.emails", key="#userData.email"),
			@CachePut(value="user.names", key="#userData.name"),
	})
	//@formatter:on
	public UserData updateUserData(UserData userData) {
		try {
			forgetUserData(userData);
			return super.update(userData).runThrow();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while updating data: " + userData, e);
			return super.load(userData).catch_(e2 -> LOGGER.log(Level.SEVERE, "Error while reloading data: " + userData, e2)).run();
		}
	}

}
