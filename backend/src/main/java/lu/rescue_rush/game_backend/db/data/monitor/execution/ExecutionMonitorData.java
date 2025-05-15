package lu.rescue_rush.game_backend.db.data.monitor.execution;

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
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

@GeneratedKey("id")
public class ExecutionMonitorData implements SafeSQLEntry {

	private int id;

	private String key;
	private Timestamp timeStart, timeEnd;
	private int entryCount;
	private double sum;
	private double avg;
	private double median;
	private double deviation;

	public ExecutionMonitorData() {
	}

	public ExecutionMonitorData(String key, Timestamp timeStart, int entryCount, double sum, double avg, double median, double deviation) {
		this.key = key;
		this.timeStart = timeStart;
		this.entryCount = entryCount;
		this.sum = sum;
		this.avg = avg;
		this.median = median;
		this.deviation = deviation;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateGeneratedKeys(BigInteger id) throws SQLException {
		this.id = id.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.key = rs.getString("key");
		this.timeStart = rs.getTimestamp("time_start");
		this.timeEnd = rs.getTimestamp("time_end");
		this.entryCount = rs.getInt("entry_count");
		this.sum = rs.getDouble("sum");
		this.avg = rs.getDouble("avg");
		this.median = rs.getDouble("median");
		this.deviation = rs.getDouble("dev");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "key", "entry_count", "sum", "avg", "median", "dev", "time_start" });
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
		stmt.setInt(2, entryCount);
		stmt.setDouble(3, sum);
		stmt.setDouble(4, avg);
		stmt.setDouble(5, median);
		stmt.setDouble(6, deviation);
		stmt.setTimestamp(7, timeStart);
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
	public ExecutionMonitorData clone() {
		return new ExecutionMonitorData();
	}

}
