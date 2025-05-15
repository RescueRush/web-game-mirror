package lu.rescue_rush.game_backend.db.data.monitor.interaction;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.UnsafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.db.data.user.UserData;

@GeneratedKey("id")
public class InteractionMonitoringEntryData implements SafeSQLEntry {

	private int id, userId, type;
	private Timestamp time;
	private boolean outcome;
	private String sourceIp;
	private String description;

	public InteractionMonitoringEntryData() {
	}

	public InteractionMonitoringEntryData(int type, int userId, boolean outcome, String source, String description) {
		this.time = Timestamp.from(Instant.now());
		this.type = type;
		this.userId = userId;
		this.outcome = outcome;
		this.sourceIp = source;
		this.description = description;
	}

	public InteractionMonitoringEntryData(int type, UserData userData, boolean outcome, String source, String description) {
		this(type, userData == null ? -1 : userData.getId(), outcome, source, description);
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateKeys(BigInteger id) throws SQLException {
		this.id = id.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.userId = rs.getInt("user_id");
		if (rs.wasNull()) {
			this.userId = -1;
		}
		this.type = rs.getInt("type");
		this.time = rs.getTimestamp("time");
		this.outcome = rs.getBoolean("outcome");
		this.sourceIp = rs.getString("source");
		this.description = rs.getString("description");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return "INSERT INTO " + table.getQualifiedName() + " (time, type, user_id, outcome, description, source) VALUES (?, ?, ?, ?, ?, INET6_ATON(?))";
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
		return "SELECT *, INET6_NTOA(source) FROM " + table.getQualifiedName() + " WHERE id = ?";
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {
		stmt.setTimestamp(1, time);
		stmt.setInt(2, type);
		stmt.setInt(3, userId);
		if (userId == -1) {
			stmt.setNull(3, Types.INTEGER);
		}
		stmt.setBoolean(4, outcome);
		stmt.setString(5, description);
		stmt.setString(6, sourceIp);
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

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isOutcome() {
		return outcome;
	}

	public void setOutcome(boolean outcome) {
		this.outcome = outcome;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}

	@Override
	public InteractionMonitoringEntryData clone() {
		return new InteractionMonitoringEntryData();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		InteractionMonitoringEntryData other = (InteractionMonitoringEntryData) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public static SafeSQLQuery<InteractionMonitoringEntryData> byId(int id) {
		return new SafeSQLQuery<InteractionMonitoringEntryData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<InteractionMonitoringEntryData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public InteractionMonitoringEntryData clone() {
				return new InteractionMonitoringEntryData();
			}

		};

	}

	public static UnsafeSQLQuery<InteractionMonitoringEntryData> byUser(UserData ud, Timestamp from, Timestamp to, Boolean outcome) {
		return new UnsafeSQLQuery<InteractionMonitoringEntryData>() {

			@Override
			public InteractionMonitoringEntryData clone() {
				return new InteractionMonitoringEntryData();
			}

			@Override
			public String getQuerySQL(SQLQueryable<InteractionMonitoringEntryData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `user_id` = " + ud.getId() + "" + buildTimeClause(from, to, outcome) + ";";
			}
		};
	}

	public static UnsafeSQLQuery<InteractionMonitoringEntryData> byEvent(int id, Timestamp from, Timestamp to, Boolean outcome) {
		return new UnsafeSQLQuery<InteractionMonitoringEntryData>() {

			@Override
			public InteractionMonitoringEntryData clone() {
				return new InteractionMonitoringEntryData();
			}

			@Override
			public String getQuerySQL(SQLQueryable<InteractionMonitoringEntryData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `type` = " + id + buildTimeClause(from, to, outcome) + ";";
			}

		};
	}

	public static String buildTimeClause(Timestamp from, Timestamp to, Boolean outcome) {
		final ArrayList<String> conditions = new ArrayList<>();
		if (from != null) {
			conditions.add("`time` >= '" + from + "'");
		}
		if (to != null) {
			conditions.add("`time` < '" + to + "'");
		}
		if (outcome != null) {
			conditions.add("`outcome` = " + outcome.toString());
		}
		return conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions);
	}

}
