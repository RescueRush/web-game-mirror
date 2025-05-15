package lu.rescue_rush.game_backend.db.data.game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.ReadOnlySQLEntry.SafeReadOnlySQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

public class LeaderboardData implements SafeReadOnlySQLEntry {

	private int userId, points, position;
	private String username;

	public LeaderboardData() {
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.userId = rs.getInt("user_id");
		this.points = rs.getInt("total_points");
		this.position = rs.getInt("row_index");
		this.username = rs.getString("user_name");
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return SQLBuilder.safeSelect(table, new String[] { "user_id" });
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
	}

	public int getUserId() {
		return userId;
	}

	public int getPoints() {
		return points;
	}

	public int getPosition() {
		return position;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public LeaderboardData clone() {
		return new LeaderboardData();
	}

	public static SQLQuery<LeaderboardData> getUserLeaderboard(int id) {
		return new SafeSQLQuery<LeaderboardData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<LeaderboardData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "user_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public LeaderboardData clone() {
				return new LeaderboardData();
			}

		};
	}

	public static SQLQuery<LeaderboardData> getLeaderboardPage(int limit, int offset) {
		return new SQLQuery.UnsafeSQLQuery<LeaderboardData>() {

			@Override
			public String getQuerySQL(SQLQueryable<LeaderboardData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " ORDER BY `total_points` DESC LIMIT " + limit + " OFFSET " + offset + ";";
			}

			@Override
			public LeaderboardData clone() {
				return new LeaderboardData();
			}

		};
	}

}
