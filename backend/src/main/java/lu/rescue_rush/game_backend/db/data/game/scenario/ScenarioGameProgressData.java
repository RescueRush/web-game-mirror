package lu.rescue_rush.game_backend.db.data.game.scenario;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.annotations.entry.UniqueKey;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.db.data.user.UserData;

@GeneratedKey("id")
public class ScenarioGameProgressData implements SafeSQLEntry {

	private int id, userId;
	private Date date;
	private int totalPoints, correctQuestionCount, totalQuestionCount, highestStreak, maxMultiplier, startedGameCount;
	private Timestamp lastUpdate;

	public ScenarioGameProgressData() {
	}

	public ScenarioGameProgressData(int id, int userId, Date date, int totalPoints, int correctQuestionCount, int totalQuestionCount, int highestStreak, int maxMultiplier, int startedGameCount,
			Timestamp lastUpdate) {
		this.id = id;
		this.userId = userId;
		this.date = date;
		this.totalPoints = totalPoints;
		this.correctQuestionCount = correctQuestionCount;
		this.totalQuestionCount = totalQuestionCount;
		this.highestStreak = highestStreak;
		this.maxMultiplier = maxMultiplier;
		this.startedGameCount = startedGameCount;
		this.lastUpdate = lastUpdate;
	}

	public ScenarioGameProgressData(UserData ud) {
		this(ud.getId());
	}

	public ScenarioGameProgressData(int userId) {
		this.userId = userId;
	}

	public ScenarioGameProgressData startGame(ScenarioGameData game) {
		this.startedGameCount++;

		return this;
	}

	public ScenarioGameProgressData updateQuestion(ScenarioGameData game, int pointsAdd, boolean success) {
		this.correctQuestionCount += success ? 1 : 0;
		this.totalPoints += pointsAdd;
		this.totalQuestionCount++;
		this.highestStreak = Math.max(this.highestStreak, game.getStreakCount());
		this.maxMultiplier = Math.max(this.maxMultiplier, game.getCurrentMultiplier());

		return this;
	}

	public void generatedKeyUpdate(ResultSet rs) throws SQLException {
		this.id = rs.getInt(1);
		this.date = rs.getDate(2);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.userId = rs.getInt("user_id");
		this.date = rs.getDate("date");
		this.totalPoints = rs.getInt("total_points");
		this.correctQuestionCount = rs.getInt("correct_questions");
		this.totalQuestionCount = rs.getInt("total_questions");
		this.highestStreak = rs.getInt("highest_streak");
		this.maxMultiplier = rs.getInt("max_multiplier");
		this.startedGameCount = rs.getInt("started_games");
		this.lastUpdate = rs.getTimestamp("last_update");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "total_points", "correct_questions", "total_questions", "highest_streak", "max_multiplier", "started_games" }, new String[] { "id" });
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
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, totalPoints);
		stmt.setInt(2, correctQuestionCount);
		stmt.setInt(3, totalQuestionCount);
		stmt.setInt(4, highestStreak);
		stmt.setInt(5, maxMultiplier);
		stmt.setInt(6, startedGameCount);

		stmt.setInt(7, id);
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
	public ScenarioGameProgressData clone() {
		return new ScenarioGameProgressData();
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}

	public int getCorrectQuestionCount() {
		return correctQuestionCount;
	}

	public void setCorrectQuestionCount(int correctQuestionCount) {
		this.correctQuestionCount = correctQuestionCount;
	}

	public int getTotalQuestionCount() {
		return totalQuestionCount;
	}

	public void setTotalQuestionCount(int totalQuestionCount) {
		this.totalQuestionCount = totalQuestionCount;
	}

	public int getHighestStreak() {
		return highestStreak;
	}

	public void setHighestStreak(int highestStreak) {
		this.highestStreak = highestStreak;
	}

	public int getStartedGameCount() {
		return startedGameCount;
	}

	public void setStartedGameCount(int startedGameCount) {
		this.startedGameCount = startedGameCount;
	}

	public int getId() {
		return id;
	}

	@UniqueKey("user_id")
	public int getUserId() {
		return userId;
	}

	@UniqueKey("date")
	public Date getDate() {
		return date;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	@Override
	public String toString() {
		return "QuizGameProgressData [id=" + id + ", userId=" + userId + ", date=" + date + ", totalPoints=" + totalPoints + ", correctQuestionCount=" + correctQuestionCount + ", totalQuestionCount="
				+ totalQuestionCount + ", highestStreak=" + highestStreak + ", maxMultiplier=" + maxMultiplier + ", startedGameCount=" + startedGameCount + ", lastUpdate=" + lastUpdate + "]";
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byUserId(UserData ud) {
		return byUserId(ud.getId());
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byUserId(int userId) {
		return new SQLQuery.SafeSQLQuery<ScenarioGameProgressData>() {
			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
			}

			@Override
			public String getPreparedQuerySQL(SQLQueryable<ScenarioGameProgressData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE user_id = ?;";
			}

			@Override
			public ScenarioGameProgressData clone() {
				return new ScenarioGameProgressData();
			}
		};
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byDate(Date date) {
		return new SQLQuery.SafeSQLQuery<ScenarioGameProgressData>() {
			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setDate(1, date);
			}

			@Override
			public String getPreparedQuerySQL(SQLQueryable<ScenarioGameProgressData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE date = ?;";
			}

			@Override
			public ScenarioGameProgressData clone() {
				return new ScenarioGameProgressData();
			}
		};
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byUserIdAndDate(UserData ud, Date date) {
		return byUserIdAndDate(ud.getId(), date);
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byUserIdAndDate(int userId, Date date) {
		return new SQLQuery.SafeSQLQuery<ScenarioGameProgressData>() {
			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
				stmt.setDate(2, date);
			}

			@Override
			public String getPreparedQuerySQL(SQLQueryable<ScenarioGameProgressData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE user_id = ? AND date = ?;";
			}

			@Override
			public ScenarioGameProgressData clone() {
				return new ScenarioGameProgressData();
			}
		};
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byUserIdAndToday(UserData ud) {
		return byUserIdAndToday(ud.getId());
	}

	public static SQLQuery.SafeSQLQuery<ScenarioGameProgressData> byUserIdAndToday(int userId) {
		return new SQLQuery.SafeSQLQuery<ScenarioGameProgressData>() {
			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
			}

			@Override
			public String getPreparedQuerySQL(SQLQueryable<ScenarioGameProgressData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE user_id = ? AND date = CURRENT_DATE();";
			}

			@Override
			public ScenarioGameProgressData clone() {
				return new ScenarioGameProgressData();
			}
		};
	}

}
