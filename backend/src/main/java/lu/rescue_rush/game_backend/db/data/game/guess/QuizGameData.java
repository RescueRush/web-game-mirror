package lu.rescue_rush.game_backend.db.data.game.guess;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Queue;
import java.util.logging.Logger;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONArray;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.datastructure.pair.Pair;
import lu.pcy113.pclib.datastructure.pair.Pairs;
import lu.pcy113.pclib.datastructure.triplet.Triplet;
import lu.pcy113.pclib.datastructure.triplet.Triplets;
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

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

@GeneratedKey("id")
public class QuizGameData implements SafeSQLEntry {

	private static final Logger LOGGER = Logger.getLogger(QuizGameData.class.getName());

	public static final int LATEST_QUESTIONS_COUNT = 15, QUESTION_ANSWER_TIME = 20_000; // ms

	private int id, userId;
	private int questionOffset, currentQuestionId = -1, currentMultiplier, currentPoints, correctQuestionCount, totalQuestionCount, streakCount;
	private long questionTimeoutTime;
	private Timestamp startTime;
	private Queue<Integer> latestQuestions;

	private UserData userData;
	private QuizQuestionData questionData;

	public QuizGameData() {
	}

	public QuizGameData(UserData user) {
		this(user.getId());
		this.userData = user;
	}

	public QuizGameData(int user_id) {
		this.userId = user_id;
		this.questionOffset = PCUtils.randomIntRange(-255, 255);
	}

	public QuizGameData(int id, int user_id, int questionOffset, int currentQuestionId, int currentMultiplier, int currentPoints, int questionCount, int streakCount, long questionTimeoutTime,
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
		this.questionTimeoutTime = questionTimeoutTime;
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
		this.startTime = rs.getTimestamp("start_time");
		this.questionTimeoutTime = rs.getLong("question_timeout_time");
		this.latestQuestions = new CircularFifoQueue<Integer>(LATEST_QUESTIONS_COUNT);
		new JSONArray(rs.getString("latest_question")).forEach(i -> latestQuestions.offer((int) i));
	}

	/**
	 * 0 = socces<br>
	 * 1 = alswer hash<br>
	 * 2 = points add<br>
	 */
	@RequestSafe
	public Triplet<Boolean, String, Integer> handleAnswer(String answerCode) {
		SpringUtils.internalServerError(currentQuestionId == -1, "No current question.");

		// compute the hash before resetting the current question state
		final String correctCode = loadQuestionData().getCorrectAnswerHash(this);

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Possible computed codes: " + loadQuestionData().getPossibleAnswersForContextLocale(this));
			LOGGER.info("Correct computed code: " + correctCode);
		}

		latestQuestions.offer(currentQuestionId);
		currentQuestionId = -1; // reset state, yk
		totalQuestionCount++;

		// timed out
		if (answerCode == null || isTimedOut()) {
			streakCount = 0;
			setCorrectMultiplier(1);

			return Triplets.readOnly(false, correctCode, 0);
		}

		if (correctCode.equals(answerCode)) {
			final int pointsAdd = (int) (questionData.getPoints() * getCorrectMultiplier());

			currentPoints += pointsAdd;
			correctQuestionCount++;
			streakCount++;

			// stepped math black magic hehe
			// from {@link ScenarioGameData#handleAnswer(int)}
			final int a = 2, d = 2;
			currentMultiplier = (int) (Math.floor((-2 * a + d + Math.sqrt(Math.pow(2 * a - d, 2) + 8 * streakCount)) / (2 * d))) + 2;

			return Triplets.readOnly(true, correctCode, pointsAdd);
		} else {
			streakCount = 0;
			setCorrectMultiplier(1);

			return Triplets.readOnly(false, correctCode, 0);
		}
	}

	/**
	 * @return Pair( client question id, question data )
	 */
	@RequestSafe
	public Pair<Integer, QuizQuestionData> nextQuestion() {
		SpringUtils.internalServerError(currentQuestionId != -1, "Current question unanswered.");

		QuizQuestionData qd = TableProxy.QUIZ_QUESTION.random(latestQuestions);
		SpringUtils.internalServerError(qd == null, "Couldn't fetch question.");

		currentQuestionId = qd.getId();
		questionData = qd;

		questionTimeoutTime = System.currentTimeMillis() + QUESTION_ANSWER_TIME;

		return Pairs.readOnly(currentQuestionId + questionOffset, qd);
	}

	/**
	 * @return Pair( client question id, question data )
	 */
	@RequestSafe
	public Pair<Integer, QuizQuestionData> currentQuestion() {
		SpringUtils.internalServerError(currentQuestionId == -1, "No current question.");

		QuizQuestionData qd = getQuestionData();
		SpringUtils.internalServerError(qd == null, "Couldn't fetch question.");

		return Pairs.readOnly(currentQuestionId + questionOffset, qd);
	}

	public UserData loadUserData() {
		return userData = TableProxy.USER.byId(userId);
	}

	public QuizQuestionData loadQuestionData() {
		if (currentQuestionId == -1) {
			return null;
		}
		if (questionData != null && questionData.getId() == currentQuestionId) {
			return questionData;
		}
		return questionData = TableProxy.QUIZ_QUESTION.byId(currentQuestionId);
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "question_offset", "current_question_id", "latest_question" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table,
				new String[] { "current_question_id", "current_multiplier", "current_points", "correct_questions", "total_questions", "streak_count", "question_timeout_time", "latest_question" },
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
		stmt.setLong(7, questionTimeoutTime);
		stmt.setString(8, latestQuestions.stream().collect(() -> new JSONArray(), JSONArray::put, JSONArray::putAll).toString());

		stmt.setInt(9, id);
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

	public Timestamp getStartTime() {
		return startTime;
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

	public QuizQuestionData getQuestionData() {
		return questionData;
	}

	public void setQuestionData(QuizQuestionData questionData) {
		this.questionData = questionData;
	}

	public float getCorrectMultiplier() {
		return (float) currentMultiplier / 2;
	}

	public void setCorrectMultiplier(float correctMultiplier) {
		this.currentMultiplier = (int) (correctMultiplier * 2);
	}

	public long getQuestionTimeoutTime() {
		return questionTimeoutTime;
	}

	public void setQuestionTimeoutTime(long questionTimeoutTime) {
		this.questionTimeoutTime = questionTimeoutTime;
	}

	public long getRemainingQuestionTime() {
		return this.questionTimeoutTime - System.currentTimeMillis();
	}

	public boolean isTimedOut() {
		return getRemainingQuestionTime() <= 0;
	}

	@Override
	public QuizGameData clone() {
		return new QuizGameData();
	}

	public static SafeSQLQuery<QuizGameData> byUserId(int userId) {
		return new SafeSQLQuery<QuizGameData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<QuizGameData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "user_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
			}

			@Override
			public QuizGameData clone() {
				return new QuizGameData();
			}

		};
	}

	public static SafeSQLQuery<QuizGameData> byUser(UserData ud) {
		return byUserId(ud.getId());
	}

}
