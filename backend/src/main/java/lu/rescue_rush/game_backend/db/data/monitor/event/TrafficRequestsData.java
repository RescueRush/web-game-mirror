package lu.rescue_rush.game_backend.db.data.monitor.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;

public class TrafficRequestsData implements SafeSQLEntry {

	private Timestamp datetime;
	private String source;
	private int count;

	public TrafficRequestsData() {
	}

	public TrafficRequestsData(Timestamp datetime, int count) {
		this.datetime = datetime;
		this.count = count;
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.datetime = rs.getTimestamp("datetime");
		this.source = rs.getString("source");
		this.count = rs.getInt("total_count");
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

	public Timestamp getDatetime() {
		return datetime;
	}

	public String getSource() {
		return source;
	}

	public int getCount() {
		return count;
	}

	public TrafficRequestsData clone() {
		return new TrafficRequestsData();
	}

	public static SQLQuery<TrafficRequestsData> byTimestamp(Timestamp from, Timestamp to) {
		return new SQLQuery.SafeSQLQuery<TrafficRequestsData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<TrafficRequestsData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `datetime` BETWEEN ? AND ?;";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setTimestamp(1, from);
				stmt.setTimestamp(2, to);
			}

			@Override
			public TrafficRequestsData clone() {
				return new TrafficRequestsData();
			}
		};
	}

}
