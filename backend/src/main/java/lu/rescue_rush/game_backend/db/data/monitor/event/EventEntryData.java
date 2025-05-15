package lu.rescue_rush.game_backend.db.data.monitor.event;

import java.math.BigInteger;
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
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

@GeneratedKey("id")
public class EventEntryData implements SafeSQLEntry {

	private int id, type;
	private Timestamp time;
	private String description;

	public EventEntryData() {
	}

	public EventEntryData(int type, String description) {
		this.type = type;
		this.description = description;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateKeys(BigInteger id) throws SQLException {
		this.id = id.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.type = rs.getInt("type");
		this.time = rs.getTimestamp("time");
		this.description = rs.getString("description");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "type", "description" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return null;
	}

	@Override
	public <T extends SQLEntry> String getPreparedDeleteSQL(DataBaseTable<T> table) {
		return null;
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return SQLBuilder.safeSelect(table, new String[] { "id" });
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, type);
		stmt.setString(2, description);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {

	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {

	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	@Override
	public EventEntryData clone() {
		return new EventEntryData();
	}

	@Override
	public String toString() {
		return "EventEntryData [id=" + id + ", type=" + type + ", time=" + time + ", description=" + description + "]";
	}

	public static SQLQuery<EventEntryData> byTimestamp(Timestamp from, Timestamp to) {
		return new SQLQuery.SafeSQLQuery<EventEntryData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<EventEntryData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `time` BETWEEN ? AND ?;";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setTimestamp(1, from);
				stmt.setTimestamp(2, to);
			}

			@Override
			public EventEntryData clone() {
				return new EventEntryData();
			}
		};
	}

	public static SQLQuery<EventEntryData> byEventKeyPatternAndTimestamp(Timestamp from, Timestamp to, String filter) {
		return new SQLQuery.SafeSQLQuery<EventEntryData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<EventEntryData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `time` BETWEEN ? AND ? AND `type` = (SELECT `id` WHERE `event_type`.`key` LIKE ?);";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setTimestamp(1, from);
				stmt.setTimestamp(2, to);
				stmt.setString(3, filter.replace("*", "%"));
			}

			@Override
			public EventEntryData clone() {
				return new EventEntryData();
			}
		};
	}

}
