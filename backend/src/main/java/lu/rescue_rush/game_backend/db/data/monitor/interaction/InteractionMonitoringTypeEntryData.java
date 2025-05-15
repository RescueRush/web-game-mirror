package lu.rescue_rush.game_backend.db.data.monitor.interaction;

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
import lu.pcy113.pclib.db.impl.SQLQuery.UnsafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.utils.monitoring.TrackingEntity;

@GeneratedKey("id")
public class InteractionMonitoringTypeEntryData implements SafeSQLEntry, TrackingEntity {

	private int id;
	private String key;
	private String name;
	private String desc;

	public InteractionMonitoringTypeEntryData() {
	}

	public InteractionMonitoringTypeEntryData(String key, String name, String desc) {
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

	@Override
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

	@Override
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
	public InteractionMonitoringTypeEntryData clone() {
		return new InteractionMonitoringTypeEntryData();
	}

	public static SQLQuery<InteractionMonitoringTypeEntryData> byKey(String key) {
		return new SafeSQLQuery<InteractionMonitoringTypeEntryData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<InteractionMonitoringTypeEntryData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "key" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, key);
			}

			@Override
			public InteractionMonitoringTypeEntryData clone() {
				return new InteractionMonitoringTypeEntryData();
			}
		};
	}

	public static SafeSQLQuery<InteractionMonitoringTypeEntryData> byId(int id) {
		return new SafeSQLQuery<InteractionMonitoringTypeEntryData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<InteractionMonitoringTypeEntryData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			public InteractionMonitoringTypeEntryData clone() {
				return new InteractionMonitoringTypeEntryData();
			}
		};
	}

	public static UnsafeSQLQuery<InteractionMonitoringTypeEntryData> loadAll() {
		return new UnsafeSQLQuery<InteractionMonitoringTypeEntryData>() {

			@Override
			public String getQuerySQL(SQLQueryable<InteractionMonitoringTypeEntryData> table) {
				return SQLBuilder.safeSelect(table, null);
			}

			public InteractionMonitoringTypeEntryData clone() {
				return new InteractionMonitoringTypeEntryData();
			}

		};
	}

}
