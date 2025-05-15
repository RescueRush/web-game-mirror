package lu.rescue_rush.game_backend.db.data.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

@GeneratedKey("id")
public class UserProfileHistoryData implements SafeSQLEntry {

	private int id, userId, typeId;
	private Timestamp time;
	private String description, oldValue, newValue;

	public UserProfileHistoryData() {
	}

	public UserProfileHistoryData(int id) {
		this.id = id;
	}

	public UserProfileHistoryData(int userId, int type, String oldValue, String newValue) {
		this.userId = userId;
		this.typeId = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public UserProfileHistoryData(UserData ud, int type, String oldValue, String newValue) {
		this.userId = ud.getId();
		this.typeId = type;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public UserProfileHistoryData(int userId, int type, String description, String oldValue, String newValue) {
		this.userId = userId;
		this.typeId = type;
		this.description = description;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public UserProfileHistoryData(int id, int userId, int type, Timestamp time, String description, String oldValue, String newValue) {
		this.id = id;
		this.userId = userId;
		this.typeId = type;
		this.time = time;
		this.description = description;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@GeneratedKeyUpdate
	public void updateKeys(ResultSet rs) throws SQLException {
		id = rs.getInt(1);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		userId = rs.getInt("user_id");
		typeId = rs.getInt("type");
		time = rs.getTimestamp("time");
		description = rs.getString("description");
		oldValue = rs.getString("old_value");
		newValue = rs.getString("new_value");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "type_id", "description", "old_value", "new_value" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "type_id", "description", "old_value", "new_value" }, new String[] { "id" });
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
		stmt.setInt(1, userId);
		stmt.setInt(2, typeId);
		stmt.setString(3, description);
		stmt.setString(4, oldValue);
		stmt.setString(5, newValue);

	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, typeId);
		stmt.setString(2, description);
		stmt.setString(3, oldValue);
		stmt.setString(4, newValue);

		stmt.setInt(5, id);
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
	public UserProfileHistoryData clone() {
		return new UserProfileHistoryData();
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getType() {
		return typeId;
	}

	public void setType(int type) {
		this.typeId = type;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
