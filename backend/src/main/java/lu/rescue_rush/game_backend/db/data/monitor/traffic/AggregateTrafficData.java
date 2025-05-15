package lu.rescue_rush.game_backend.db.data.monitor.traffic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;

public class AggregateTrafficData implements SafeSQLEntry {

	private Timestamp time;
	private String domainPath;
	private int count;

	public AggregateTrafficData() {
	}

	public AggregateTrafficData(Timestamp time, String domainPath, int count) {
		this.time = time;
		this.domainPath = domainPath;
		this.count = count;
	}

	public static boolean doesColumnExist(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			if (metaData.getColumnName(i).equalsIgnoreCase(columnName)) {
				return true; // Column found
			}
		}
		return false; // Column not found
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.time = rs.getTimestamp("time");
		this.domainPath = doesColumnExist(rs, "referer") ? rs.getString("referer") : (doesColumnExist(rs, "synthetic_referer") ? rs.getString("synthetic_referer") : rs.getString("domain_path"));
		this.count = rs.getInt("count");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return null;
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
		return null;
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {

	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {

	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {

	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {

	}

	public Timestamp getTime() {
		return time;
	}

	public String getDomainPath() {
		return domainPath;
	}

	public int getCount() {
		return count;
	}

	@Override
	public AggregateTrafficData clone() {
		return new AggregateTrafficData();
	}

	public static SQLQuery<AggregateTrafficData> byTimestamp(Timestamp from, Timestamp to) {
		return new SQLQuery.SafeSQLQuery<AggregateTrafficData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<AggregateTrafficData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `time` >= ? AND `time` <= ?;";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setTimestamp(1, from);
				stmt.setTimestamp(2, to);
			}

			@Override
			public AggregateTrafficData clone() {
				return new AggregateTrafficData();
			}
		};
	}

}
