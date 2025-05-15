package lu.rescue_rush.game_backend.db.data.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.annotations.entry.UniqueKey;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.db.TableProxy;

@GeneratedKey("id")
public class UserPermissionTypeData implements SafeSQLEntry {

	//@formatter:off
	public static final String
			KEY_SUPERUSER = "sudo",
			KEY_USER_BAN = "ban",
			KEY_USER_HIDE = "hide",
			KEY_NEWSLETTER_EMAILS_QUERY = "newsletter_query",
			KEY_TRAFFIC_LOG_QUERY = "traffic_log_query",
			
			KEY_QUESTIONS_ALL = "questions_all",
			KEY_QUESTIONS_SELECT = "questions_select",
			KEY_QUESTIONS_INSERT = "questions_insert",
			KEY_QUESTIONS_UPDATE = "questions_update",
			KEY_QUESTIONS_DELETE = "questions_delete",
			
			KEY_USERS_ALL = "user_all",
			KEY_USERS_SELECT = "user_select",
			KEY_USERS_TRACKING = "user_tracking",
			KEY_USERS_SANCTIONS = "user_sanctions",
			KEY_USERS_PERMISSIONS = "user_permissions",
	
			KEY_USERS_PROFILE_ALL = "user_profile_all",
			KEY_USERS_PROFILE_NAME_EDIT = "user_profile_name_edit",
			KEY_USERS_PROFILE_PASS_RESET = "user_profile_pass_reset",
			KEY_USERS_PROFILE_EMAIL_EDIT = "user_profile_email_edit",
			KEY_USERS_PROFILE_POINT_EDIT = "user_profile_point_edit";
	//@formatter:on

	private int id;
	private String name, key, description;

	public UserPermissionTypeData() {
	}

	public UserPermissionTypeData(int id) {
		this.id = id;
	}

	public UserPermissionTypeData(String name, String key, String description) {
		this.name = name;
		this.key = key;
		this.description = description;
	}

	@GeneratedKeyUpdate
	public void updateKeys(ResultSet rs) throws SQLException {
		id = rs.getInt(1);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		key = rs.getString("key");
		description = rs.getString("description");
	}

	/**
	 * You're not supposed to do this
	 */
	@Deprecated
	public void push() {
		TableProxy.USER_PERMISSION_TYPE.updateUserPermissionTypeData(this);
	}

	public boolean matches(UserPermissionData e) {
		return e.getTypeId() == this.id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@UniqueKey("key")
	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "name", "key", "description" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "name", "key", "description" }, new String[] { "id" });
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
		stmt.setString(2, key);
		stmt.setString(3, description);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, name);
		stmt.setString(2, key);
		stmt.setString(3, description);
		stmt.setInt(4, id);
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
	public UserPermissionTypeData clone() {
		return new UserPermissionTypeData();
	}

	@Override
	public String toString() {
		return "UserSanctionReasonData {id=" + id + ", name=" + name + ", key=" + key + ", description=" + description + "}";
	}

	public static SQLQuery<UserPermissionTypeData> byId(int id) {
		return new SafeSQLQuery<UserPermissionTypeData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionTypeData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public UserPermissionTypeData clone() {
				return new UserPermissionTypeData();
			}

		};
	}

	public static SQLQuery<UserPermissionTypeData> byKey(String key) {
		return new SafeSQLQuery<UserPermissionTypeData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionTypeData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "key" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, key);
			}

			@Override
			public UserPermissionTypeData clone() {
				return new UserPermissionTypeData();
			}

		};
	}

	public static SQLQuery<UserPermissionTypeData> all() {
		return new SafeSQLQuery<UserPermissionTypeData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionTypeData> table) {
				return SQLBuilder.safeSelect(table, null);
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
			}

			@Override
			public UserPermissionTypeData clone() {
				return new UserPermissionTypeData();
			}

		};
	}

}
