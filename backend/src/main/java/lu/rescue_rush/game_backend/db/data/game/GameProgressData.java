package lu.rescue_rush.game_backend.db.data.game;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.ReadOnlySQLEntry.SafeReadOnlySQLEntry;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

public class GameProgressData implements SafeReadOnlySQLEntry {

	private int userId;
	private Timestamp date;
	private int startedGames, totalPoints, totalQuestions, correctQuestions, highestStreak, maxMultiplier;

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		userId = rs.getInt("user_id");
		date = rs.getTimestamp("date");
		startedGames = rs.getInt("started_games");
		totalPoints = rs.getInt("total_points");
		totalQuestions = rs.getInt("total_questions");
		correctQuestions = rs.getInt("correct_questions");
		highestStreak = rs.getInt("highest_streak");
		maxMultiplier = rs.getInt("max_multiplier");
	}

	public int getUserId() {
		return userId;
	}

	public Timestamp getDate() {
		return date;
	}

	public int getStartedGames() {
		return startedGames;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getTotalQuestions() {
		return totalQuestions;
	}

	public int getCorrectQuestions() {
		return correctQuestions;
	}

	public int getHighestStreak() {
		return highestStreak;
	}

	public int getMaxMultiplier() {
		return maxMultiplier;
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return SQLBuilder.safeSelect(table, new String[] { "id" });
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
	}

	public GameProgressData clone() {
		return new GameProgressData();
	}

}
