package lu.rescue_rush.game_backend.db.data.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.annotations.entry.UniqueKey;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.UnsafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.data.Locales;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;
import lu.rescue_rush.game_backend.utils.monitoring.TrackingEntity;

@GeneratedKey("id")
public class UserData implements SafeSQLEntry, TrackingEntity {

	// TODO remove sanctions / perms (if not already done)

	private int id = -1;
	private String email, name, lang = "lb", pass, token;
	private Timestamp joinDate, lastLoginDate, lastRequestDate;

	private String ip;

	private List<UserSanctionData> sanctions;
	private List<UserPermissionData> permissions;

	public UserData() {
	}

	public UserData(int id) {
		this.id = id;
	}

	public UserData(int id, String name, String email, String lang) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.lang = lang;
	}

	public UserData(int id, String email, String name, String lang, String pass, String token, Timestamp joinDate, Timestamp lastLoginDate, Timestamp lastRequestDate) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.lang = lang;
		this.pass = pass;
		this.token = token;
		this.joinDate = joinDate;
		this.lastLoginDate = lastLoginDate;
	}

	/**
	 * @param pass the pass we get from the client (hashed once)
	 */
	public UserData(String email, String name, String pass, Language lang) {
		this(name, pass, lang);
		this.email = email;
	}

	/**
	 * @param pass the pass we get from the client (hashed once)
	 */
	public UserData(String name, String pass, Language lang2) {
		this.name = name;
		this.pass = hashPass(pass);
		this.lang = lang2.getCode();
	}

	public UserData(String name2, String pass2) {
		this.name = name2;
		this.pass = pass2;
	}

	public UserData(String email, Language lang) {
		this.email = email;
		this.lang = lang.getCode();
	}

	@GeneratedKeyUpdate
	public void updateGeneratedKeys(ResultSet rs) throws SQLException {
		id = rs.getInt(1);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		email = rs.getString("email");
		lang = rs.getString("lang");
		pass = rs.getString("pass");
		token = rs.getString("token");
		joinDate = rs.getTimestamp("join_date");
		lastLoginDate = rs.getTimestamp("last_login");
		lastRequestDate = rs.getTimestamp("last_request");
	}

	public void updateName(String newName) {
		UserProfileHistoryData pd = new UserProfileHistoryData(id, TableProxy.INTERACTION_MONITOR_TYPE.loadOrInsertByKey(InteractionMonitor.R2API_USER_UPDATE_NAME), "Updated name.", name, newName);
		name = newName;
		TableProxy.USER_PROFILE_HISTORY.insert(pd).run();
	}

	public void updatePass(String newpass) {
		UserProfileHistoryData pd = new UserProfileHistoryData(id, TableProxy.INTERACTION_MONITOR_TYPE.loadOrInsertByKey(InteractionMonitor.R2API_USER_UPDATE_PASS), "Updated password.", null, null);
		pass = newpass;
		TableProxy.USER_PROFILE_HISTORY.insert(pd).run();
	}

	public void updateEmail(String newEmail) {
		UserProfileHistoryData pd = new UserProfileHistoryData(id, TableProxy.INTERACTION_MONITOR_TYPE.loadOrInsertByKey(InteractionMonitor.R2API_USER_UPDATE_EMAIL), "Updated email.", email,
				newEmail);
		email = newEmail;
		TableProxy.USER_PROFILE_HISTORY.insert(pd).run();
	}

	public UserData addSanction(String reasonType, UserData author, String description) {
		if (sanctions == null) {
			loadSanctions();
		}
		UserSanctionData usd = new UserSanctionData(this, TableProxy.USER_SANCTION_REASON.byKey(reasonType), author, description);
		usd = TableProxy.USER_SANCTION.insert(usd).run();
		sanctions.add(usd);
		updateSanctionStatus();
		return this;
	}

	public UserData addSanctionIfNotPresent(String reasonType, UserData author, String description) {
		if (sanctions == null) {
			loadSanctions();
		}
		if (!hasActiveSanction(reasonType)) {
			UserSanctionData usd = new UserSanctionData(this, TableProxy.USER_SANCTION_REASON.byKey(reasonType), author, description);
			usd = TableProxy.USER_SANCTION.insert(usd).run();
			sanctions.add(usd);
			updateSanctionStatus();
		}
		return this;
	}

	public UserData addSanction(@NonNull UserSanctionReasonData reason, UserData author, String description) {
		if (sanctions == null) {
			loadSanctions();
			loadSanctionReasonDatas();
		}
		if (!hasActiveSanction(reason)) {
			UserSanctionData usd = new UserSanctionData(this, reason, author, description);
			usd = TableProxy.USER_SANCTION.insertAndReload(usd).run();
			sanctions.add(usd);
			updateSanctionStatus();
		}
		return this;
	}

	public UserData addPermission(String permissionType, UserData author, String description) {
		if (permissions == null) {
			loadPermissionTypes();
		}
		if (!hasPermission(permissionType)) {
			UserPermissionData upd = new UserPermissionData(this, TableProxy.USER_PERMISSION_TYPE.byKey(permissionType), author, description);
			upd = TableProxy.USER_PERMISSION.insert(upd).run();
			permissions.add(upd);
		}
		return this;
	}

	public UserData addPermission(@NonNull UserPermissionTypeData permissionType, UserData author, String description) {
		if (permissions == null) {
			loadPermissions();
			loadPermissionTypes();
		}
		if (!hasPermission(permissionType.getKey())) {
			UserPermissionData upd = new UserPermissionData(this, permissionType, author, description);
			upd = TableProxy.USER_PERMISSION.insert(upd).run();
			permissions.add(upd);
		}
		return this;
	}

	public int removePermissions() {
		permissions.clear();
		return TableProxy.USER_PERMISSION.deleteByUserId(id).run();
	}

	public int removeSanctions() {
		sanctions.clear();
		return TableProxy.USER_SANCTION.deleteByUserId(id).run();
	}

	public int removeSanctions(String keyHidden) {
		final int startingCount = sanctions.size();
		loadSanctionReasonDatas();
		sanctions.stream().forEach(usrd -> TableProxy.USER_SANCTION.delete(usrd).run());
		sanctions.removeIf(sanction -> sanction.getReasonData().getKey().equals(keyHidden));
		return startingCount - sanctions.size();
	}

	public UserData updateSanctionStatus() {
		if (isBanned()) {
			this.token = null; // force logout all connections
		}

		return this;
	}

	public void push() {
		TableProxy.USER.updateUserData(this);
	}

	public UserData pushSanctions() {
		if (sanctions == null)
			return this;
		this.sanctions.forEach(UserSanctionData::push);
		return this;
	}

	public boolean hasActiveSanction(String key) {
		for (UserSanctionData data : sanctions) {
			if (data.getReasonData().getKey().equals(key) && !data.isCancelled()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasActiveSanction(UserSanctionReasonData data) {
		return hasActiveSanction(data.getKey());
	}

	public UserData pushPermissions() {
		if (permissions == null)
			return this;
		this.permissions.forEach(UserPermissionData::push);
		return this;
	}

	public void pushRecursive() {
		pushSanctions();
		pushPermissions();
		push();
	}

	public UserData updateLogin() {
		lastLoginDate = new Timestamp(System.currentTimeMillis());
		return this;
	}

	public UserData updateRequest() {
		lastRequestDate = new Timestamp(System.currentTimeMillis());
		return this;
	}

	public List<UserSanctionData> loadSanctions() {
		return sanctions = TableProxy.USER_SANCTION.byUser(this);
	}

	public List<UserSanctionData> loadSanctionReasonDatas() {
		if (sanctions == null) {
			loadSanctions();
		}
		sanctions.forEach(UserSanctionData::loadReasonData);
		return sanctions;
	}

	public List<UserSanctionData> getSanctions() {
		return sanctions;
	}

	public List<UserPermissionData> loadPermissions() {
		return permissions = TableProxy.USER_PERMISSION.byUser(this);
	}

	public List<UserPermissionData> loadPermissionTypes() {
		if (permissions == null) {
			loadPermissions();
		}
		permissions.forEach(UserPermissionData::loadTypeData);
		return permissions;
	}

	public List<UserPermissionData> getPermissions() {
		return permissions;
	}

	public List<GrantedAuthority> getAuthorities() {
		loadPermissionTypes();
		return permissions.stream().map(role -> new SimpleGrantedAuthority(role.getTypeData().getKey())).collect(Collectors.toList());
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "name", "email", "lang", "pass" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "name", "email", "lang", "pass", "token", "last_login", "last_request" }, new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedDeleteSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeDelete(table, new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return SQLBuilder.safeSelect(table, new String[] { "id" });
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, name);
		stmt.setString(2, email);
		stmt.setString(3, lang);
		stmt.setString(4, pass);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, name);
		stmt.setString(2, email);
		stmt.setString(3, lang);
		stmt.setString(4, pass);
		stmt.setString(5, token);
		stmt.setTimestamp(6, lastLoginDate);
		stmt.setTimestamp(7, lastRequestDate);

		stmt.setInt(8, id);
	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public UserData clone() {
		return new UserData();
	}

	@Override
	@UniqueKey("name")
	public String getName() {
		return name;
	}

	@Deprecated
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getId() {
		return id;
	}

	public Timestamp getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Timestamp lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Timestamp getJoinDate() {
		return joinDate;
	}

	@UniqueKey("token")
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Language getLanguage() {
		return Language.byCode(lang);
	}

	public void setLanguage(Language lang) {
		setLocale(lang.getLocale());
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Locale getLocale() {
		return PCUtils.defaultIfNull(Locales.byCode(lang), Locales.LUXEMBOURISH);
	}

	public void setLocale(Locale lang) {
		this.lang = lang.getLanguage().toUpperCase();
	}

	public String getPass() {
		return pass;
	}

	public Timestamp getLastRequestDate() {
		return lastRequestDate;
	}

	@UniqueKey("email")
	public String getEmail() {
		return email;
	}

	@Deprecated
	public void setEmail(String email) {
		this.email = email;
	}

	@Deprecated
	public void setPass(String pass) {
		this.pass = pass;
	}

	public boolean hasSanctions() {
		return sanctions.size() > 0;
	}

	public boolean isBanned() {
		return sanctionCount(UserSanctionReasonData.KEY_BAN) > 0;
	}

	public long sanctionCount(String key) {
		return sanctions.stream().filter(s -> s.getReasonData().getKey().equals(key)).count();
	}

	public long sanctionCount() {
		return sanctions.size();
	}

	public List<UserSanctionData> getSanctions(String key) {
		return sanctions.stream().filter(s -> s.getReasonData().getKey().equals(key)).collect(Collectors.toList());
	}

	public void clearSanctions() {
		sanctions.clear();
	}

	public void clearPermissions() {
		permissions.clear();
	}

	public boolean hasPermission() {
		return permissions.size() > 0;
	}

	public boolean hasPermission(String key) {
		return permissionCount(key) > 0;
	}

	public boolean isSuperuser() {
		return permissionCount(UserPermissionTypeData.KEY_SUPERUSER) > 0;
	}

	public long permissionCount(String key) {
		return permissions.stream().filter(s -> s.getTypeData().getKey().equals(key)).count();
	}

	public long permissionCount() {
		return permissions.size();
	}

	public List<UserPermissionData> getPermission(String key) {
		return permissions.stream().filter(s -> s.getTypeData().getKey().equals(key)).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return "UserData [id=" + id + ", email=" + email + ", name=" + name + ", lang=" + lang + ", pass=" + pass + ", token=" + token + ", joinDate=" + joinDate + ", lastLoginDate=" + lastLoginDate
				+ ", lastRequestDate=" + lastRequestDate + ", sanctions=" + sanctions + ", permissions=" + permissions + "]";
	}

	public static String hashPass(String rawPass) {
		return PCUtils.hashString(rawPass, "SHA-256");
	}

	public String genNewToken() {
		String newToken = UserData.hashPass(pass + System.currentTimeMillis());
		this.token = UserData.hashPass(newToken);
		return newToken;
	}

	public static SQLQuery<UserData> byToken(String token) {
		return new SafeSQLQuery<UserData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "token" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, UserData.hashPass(token));
			}

			@Override
			public UserData clone() {
				return new UserData();
			}
		};
	}

	public static SQLQuery<UserData> byRawToken(String token) {
		return new SafeSQLQuery<UserData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "token" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, token);
			}

			@Override
			public UserData clone() {
				return new UserData();
			}
		};
	}

	@Deprecated
	public static SQLQuery<UserData> loginRaw(String name, String unHashedPass) {
		return login(name, hashPass(unHashedPass));
	}

	/**
	 * get srv side pass: hashed by client only
	 */
	public static SQLQuery<UserData> login(String name, String pass) {
		return new SafeSQLQuery<UserData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "name", "pass" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, name);
				stmt.setString(2, hashPass(pass));
			}

			@Override
			public UserData clone() {
				return new UserData(name, pass);
			}

		};
	}

	public static SQLQuery<UserData> byId(int id) {
		return new SafeSQLQuery<UserData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}

	public static SQLQuery<UserData> byName(String user) {
		return new SafeSQLQuery<UserData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "name" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, user);
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}

	public static SQLQuery<UserData> byEmail(String email) {
		return new SafeSQLQuery<UserData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "email" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, email);
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}

	public static SQLQuery<UserData> all() {
		return new UnsafeSQLQuery<UserData>() {

			@Override
			public String getQuerySQL(SQLQueryable<UserData> table) {
				return SQLBuilder.safeSelect(table, null);
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}

	public static SQLQuery<UserData> byOffset(int offset, int limit) {
		return new SQLQuery.UnsafeSQLQuery<UserData>() {
			@Override
			public String getQuerySQL(SQLQueryable<UserData> table) {
				String query = SQLBuilder.safeSelect(table, null, limit);
				return new StringBuilder(query).insert(query.length() - 1, " OFFSET " + offset).toString();
			}

			@Override
			public UserData clone() {
				return new UserData();
			}
		};
	}

	public static SQLQuery<UserData> byMatchingEmails(String email) {
		return byMatchingEmails(email, -1);
	}

	public static SQLQuery<UserData> byMatchingEmails(String email, int limit) {
		return new SQLQuery.TransformativeSQLQuery.SafeTransformativeSQLQuery<UserData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return "SELECT `id`, `name`, `email`, `lang` FROM " + table.getQualifiedName() + " WHERE `email` LIKE ? LIMIT " + (limit <= 0 ? 100 : limit) + ";";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, email + "%");
			}

			@Override
			public List<UserData> transform(ResultSet rs) throws SQLException {
				List<UserData> list = new ArrayList<UserData>();

				while (rs.next()) {
					list.add(new UserData(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("lang")));
				}

				return list;
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}

	public static SQLQuery<UserData> byMatchingNames(String email) {
		return byMatchingNames(email, -1);
	}

	public static SQLQuery<UserData> byMatchingNames(String email, int limit) {
		return new SQLQuery.TransformativeSQLQuery.SafeTransformativeSQLQuery<UserData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserData> table) {
				return "SELECT `id`, `name`, `email`, `lang` FROM " + table.getQualifiedName() + " WHERE `name` LIKE ? LIMIT " + (limit <= 0 ? 100 : limit) + ";";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, "%" + email + "%");
			}

			@Override
			public List<UserData> transform(ResultSet rs) throws SQLException {
				List<UserData> list = new ArrayList<UserData>();

				while (rs.next()) {
					list.add(new UserData(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("lang")));
				}

				return list;
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}

}
