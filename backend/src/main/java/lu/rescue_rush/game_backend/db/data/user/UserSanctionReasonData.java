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
public class UserSanctionReasonData implements SafeSQLEntry {

	//@formatter:off
	public static final String
			KEY_WARN = "warn",
			KEY_MUTE = "mute",
			KEY_KICK = "kick",
			KEY_BAN = "ban",
			KEY_SUSPECT = "suspect",
			KEY_HIDDEN = "hidden",
			KEY_VERIFY = "verify";
	//@formatter:on

	private int id;
	private String name, key, description;

	public UserSanctionReasonData() {
	}

	public UserSanctionReasonData(int id) {
		this.id = id;
	}

	public UserSanctionReasonData(String name, String key, String description) {
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
		TableProxy.USER_SANCTION_REASON.updateUserSanctionReasonData(this);
	}

	public boolean matches(UserSanctionData e) {
		return e.getReasonId() == this.id;
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
	public UserSanctionReasonData clone() {
		return new UserSanctionReasonData();
	}

	@Override
	public String toString() {
		return "UserSanctionReasonData {id=" + id + ", name=" + name + ", key=" + key + ", description=" + description + "}";
	}

	public static SQLQuery<UserSanctionReasonData> byId(int id) {
		return new SafeSQLQuery<UserSanctionReasonData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionReasonData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public UserSanctionReasonData clone() {
				return new UserSanctionReasonData();
			}

		};
	}

	public static SQLQuery<UserSanctionReasonData> byKey(String key) {
		return new SafeSQLQuery<UserSanctionReasonData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionReasonData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "key" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, key);
			}

			@Override
			public UserSanctionReasonData clone() {
				return new UserSanctionReasonData();
			}

		};
	}

	public static SafeSQLQuery<UserSanctionReasonData> all() {
		return new SafeSQLQuery<UserSanctionReasonData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionReasonData> table) {
				return SQLBuilder.safeSelect(table, null);
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
			}

			@Override
			public UserSanctionReasonData clone() {
				return new UserSanctionReasonData();
			}

		};
	}

}
