package lu.rescue_rush.game_backend.db.data.monitor.event;

import java.math.BigInteger;
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

@GeneratedKey("id")
public class EventTypeEntryData implements SafeSQLEntry {

	private int id;
	private String key;
	private String name;
	private String desc;

	public EventTypeEntryData() {
	}

	public EventTypeEntryData(int id) {
		this.id = id;
	}

	public EventTypeEntryData(String key, String name, String desc) {
		this.key = key;
		this.name = name;
		this.desc = desc;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateKeys(BigInteger id) throws SQLException {
		this.id = id.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.key = rs.getString("key");
		this.name = rs.getString("name");
		this.desc = rs.getString("desc");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "key", "name", "desc" });
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
		stmt.setString(1, key);
		stmt.setString(2, name);
		stmt.setString(3, desc);
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

	@UniqueKey("key")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public EventTypeEntryData clone() {
		return new EventTypeEntryData();
	}

	public static SQLQuery<EventTypeEntryData> byKey(String key) {
		return new SafeSQLQuery<EventTypeEntryData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<EventTypeEntryData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "key" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, key);
			}

			@Override
			public EventTypeEntryData clone() {
				return new EventTypeEntryData();
			}

		};
	}

}
