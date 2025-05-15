package lu.rescue_rush.game_backend.db.data.game.scenario;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONArray;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.datastructure.pair.Pair;
import lu.pcy113.pclib.datastructure.pair.Pairs;
import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.annotations.entry.UniqueKey;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

@GeneratedKey("id")
public class ScenarioGameData implements SafeSQLEntry {

	public static final int LATEST_QUESTIONS_COUNT = 15;

	private int id, userId;
	private int questionOffset, currentQuestionId = -1, currentMultiplier, currentPoints, correctQuestionCount, totalQuestionCount, streakCount;
	private Queue<Integer> latestQuestions = new CircularFifoQueue<Integer>(LATEST_QUESTIONS_COUNT);

	private UserData userData;
	private ScenarioQuestionData questionData;

	public ScenarioGameData() {
	}

	public ScenarioGameData(UserData user) {
		this(user.getId());
		this.userData = user;
	}

	public ScenarioGameData(int user_id) {
		this.userId = user_id;
		this.questionOffset = PCUtils.randomIntRange(-255, 255);
	}

	public ScenarioGameData(int id, int user_id, int questionOffset, int currentQuestionId, int currentMultiplier, int currentPoints, int questionCount, int streakCount,
			Queue<Integer> latestQuestions) {
		this.id = id;
		this.userId = user_id;
		this.questionOffset = questionOffset;
		this.currentQuestionId = currentQuestionId;
		this.currentMultiplier = currentMultiplier;
		this.currentPoints = currentPoints;
		this.correctQuestionCount = questionCount;
		this.streakCount = streakCount;
		this.latestQuestions = latestQuestions;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateKeys(BigInteger id) throws SQLException {
		this.id = id.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.userId = rs.getInt("user_id");
		this.questionOffset = rs.getInt("question_offset");
		this.currentQuestionId = rs.getInt("current_question_id");
		this.currentQuestionId = rs.wasNull() ? -1 : this.currentQuestionId;
		this.currentMultiplier = rs.getInt("current_multiplier");
		this.currentPoints = rs.getInt("current_points");
		this.correctQuestionCount = rs.getInt("correct_questions");
		this.totalQuestionCount = rs.getInt("total_questions");
		this.streakCount = rs.getInt("streak_count");
		this.latestQuestions = new CircularFifoQueue<Integer>(LATEST_QUESTIONS_COUNT);
		new JSONArray(rs.getString("latest_question")).forEach(i -> latestQuestions.offer((int) i));
	}

	/**
	 * Success: key=true<br>
	 * value = points add<br>
	 * <br>
	 * Failure: key=false<br>
	 * value = answer cards<br>
	 * <br>
	 * (deprecated) The caller should call nextQuestion()
	 */
	@RequestSafe
	public Pair<Boolean, Integer> handleAnswer(int answer) {
		SpringUtils.internalServerError(currentQuestionId == -1, "No current question.");

		loadQuestionData();

		final Pair<Boolean, Float> questionAnswer = questionData.handleAnswer(answer);
		final boolean success = questionAnswer.getKey();
		final float correctCardPercentage = questionAnswer.getValue();

		latestQuestions.offer(currentQuestionId);
		currentQuestionId = -1; // reset state for next question
		totalQuestionCount++;

		if (success) {
			final int pointsAdd = (int) (correctCardPercentage * questionData.getPoints() * getCorrectMultiplier());

			currentPoints += pointsAdd;
			correctQuestionCount++;
			streakCount++;

			// stepped math black magic hehe
			final int a = 2, d = 2;
			currentMultiplier = (int) (Math.floor((-2 * a + d + Math.sqrt(Math.pow(2 * a - d, 2) + 8 * streakCount)) / (2 * d))) + 2;

			return Pairs.readOnly(true, pointsAdd);
		} else {
			streakCount = 0;
			setCorrectMultiplier(1);

			return Pairs.readOnly(false, 0);
		}
	}

	/**
	 * @return Pair( client question id, question data )
	 */
	@RequestSafe
	public Pair<Integer, ScenarioQuestionData> nextQuestion() {
		SpringUtils.internalServerError(currentQuestionId != -1, "Current question unanswered.");

		ScenarioQuestionData qd = TableProxy.SCENARIO_QUESTION.random(latestQuestions);

		SpringUtils.internalServerError(qd == null, "Couldn't fetch question.");

		currentQuestionId = qd.getId();
		questionData = qd;

		return Pairs.readOnly(currentQuestionId + questionOffset, qd);
	}

	/**
	 * @return Pair( client question id, question data )
	 */
	@RequestSafe
	public Pair<Integer, ScenarioQuestionData> currentQuestion() {
		SpringUtils.internalServerError(currentQuestionId == -1, "No current question.");

		ScenarioQuestionData qd = getQuestionData();
		SpringUtils.internalServerError(qd == null, "Couldn't fetch question.");

		return Pairs.readOnly(currentQuestionId + questionOffset, qd);
	}

	public UserData loadUserData() {
		return userData = TableProxy.USER.byId(userId);
	}

	public ScenarioQuestionData loadQuestionData() {
		if (currentQuestionId == -1) {
			return null;
		}
		if (questionData != null && questionData.getId() == currentQuestionId) {
			return questionData;
		}
		return questionData = TableProxy.SCENARIO_QUESTION.byId(currentQuestionId);
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "question_offset", "current_question_id", "latest_question" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "current_question_id", "current_multiplier", "current_points", "correct_questions", "total_questions", "streak_count", "latest_question" },
				new String[] { "id" });
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
		stmt.setInt(2, questionOffset);
		if (currentQuestionId == -1) {
			stmt.setNull(3, Types.INTEGER);
		} else {
			stmt.setInt(3, currentQuestionId);
		}
		stmt.setString(4, "[]");
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		if (currentQuestionId == -1) {
			stmt.setNull(1, Types.INTEGER);
		} else {
			stmt.setInt(1, currentQuestionId);
		}
		stmt.setInt(2, currentMultiplier);
		stmt.setInt(3, currentPoints);
		stmt.setInt(4, correctQuestionCount);
		stmt.setInt(5, totalQuestionCount);
		stmt.setInt(6, streakCount);
		stmt.setString(7, latestQuestions.stream().collect(() -> new JSONArray(), JSONArray::put, JSONArray::putAll).toString());
		stmt.setInt(8, id);
	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
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

	@UniqueKey("user_id")
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getQuestionOffset() {
		return questionOffset;
	}

	public void setQuestionOffset(int questionOffset) {
		this.questionOffset = questionOffset;
	}

	public int getCurrentQuestionId() {
		return currentQuestionId;
	}

	public void setCurrentQuestionId(int currentQuestion) {
		this.currentQuestionId = currentQuestion;
	}

	public int getCurrentMultiplier() {
		return currentMultiplier;
	}

	public void setCurrentMultiplier(int currentMultiplier) {
		this.currentMultiplier = currentMultiplier;
	}

	public int getCurrentPoints() {
		return currentPoints;
	}

	public void setCurrentPoints(int currentPoints) {
		this.currentPoints = currentPoints;
	}

	public int getQuestionCount() {
		return correctQuestionCount;
	}

	public void setQuestionCount(int questionCount) {
		this.correctQuestionCount = questionCount;
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

	public int getStreakCount() {
		return streakCount;
	}

	public void setStreakCount(int streakCount) {
		this.streakCount = streakCount;
	}

	public Queue<Integer> getLatestQuestions() {
		return latestQuestions;
	}

	public void setLatestQuestions(Queue<Integer> latestQuestions) {
		this.latestQuestions = latestQuestions;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

	public ScenarioQuestionData getQuestionData() {
		return questionData;
	}

	public void setQuestionData(ScenarioQuestionData questionData) {
		this.questionData = questionData;
	}

	public float getCorrectMultiplier() {
		return (float) currentMultiplier / 2;
	}

	public void setCorrectMultiplier(float correctMultiplier) {
		this.currentMultiplier = (int) (correctMultiplier * 2);
	}

	@Override
	public ScenarioGameData clone() {
		return new ScenarioGameData();
	}

	public static SafeSQLQuery<ScenarioGameData> byUserId(int userId) {
		return new SafeSQLQuery<ScenarioGameData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<ScenarioGameData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "user_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
			}

			@Override
			public ScenarioGameData clone() {
				return new ScenarioGameData();
			}

		};
	}

	public static SafeSQLQuery<ScenarioGameData> byUser(UserData ud) {
		return byUserId(ud.getId());
	}

}
