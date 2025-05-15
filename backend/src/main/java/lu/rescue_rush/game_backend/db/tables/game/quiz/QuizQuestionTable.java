package lu.rescue_rush.game_backend.db.tables.game.quiz;

import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import org.json.JSONObject;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;
import lu.pcy113.pclib.db.impl.SQLQuery.UnsafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;

import lu.rescue_rush.game_backend.db.data.game.guess.QuizQuestionData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

//@formatter:off
@DB_Table(name = "quiz_question", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "desc_LU", type = "text"),
		@Column(name = "answer_LU", type = "json"),
		@Column(name = "desc_EN", type = "text"),
		@Column(name = "answer_EN", type = "json"),
		@Column(name = "desc_FR", type = "text"),
		@Column(name = "answer_FR", type = "json"),
		@Column(name = "desc_DE", type = "text"),
		@Column(name = "answer_DE", type = "json"),
		@Column(name = "answer", type = "int"),
		@Column(name = "points", type = "int"),
		@Column(name = "level", type = "int")
}, constraints = {
		@Constraint(name = "pk_quiz_question_id", type = Constraint.Type.PRIMARY_KEY, columns = "id")
})
//@formatter:on
@Service
public class QuizQuestionTable extends R2DBTable<QuizQuestionData> {

	public QuizQuestionTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== REQUEST SAFE ==========
	 */

	@Deprecated
	@RequestSafe
	public QuizQuestionData requestSafe_getRandom() {
		QuizQuestionData qd = random();
		SpringUtils.internalServerError(qd == null, "Question not found.");
		return qd;
	}

	@RequestSafe
	public QuizQuestionData requestSafe_getRandom(Queue<Integer> notIds) {
		QuizQuestionData qd = random(notIds);
		SpringUtils.internalServerError(qd == null, "Question not found.");
		return qd;
	}

	@RequestSafe
	@Cacheable(value = "question.ids", key = "#id")
	public QuizQuestionData requestSafe_byId(int id) {
		QuizQuestionData qd = byId(id);
		SpringUtils.internalServerError(qd == null, "Question not found.");
		return qd;
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Deprecated
	public QuizQuestionData random() {
		return super.query(QuizQuestionData.random()).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data", e)).run();
	}

	public QuizQuestionData random(Queue<Integer> notIds) {
		return super.query(QuizQuestionData.random(notIds)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data", e)).run();
	}

	@Cacheable(value = "question.ids", key = "#id")
	public QuizQuestionData byId(int id) {
		return super.load(new QuizQuestionData(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
	}

	public List<QuizQuestionData> byOffset(int offset, int limit) {
		return super.query(QuizQuestionData.byOffset(offset, limit)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	/*
	 * ========== UTILS ==========
	 */

	@SuppressWarnings("deprecation")
	public QuizQuestionData safeInsert(String qLu, String qEn, String qFr, String qDe, JSONObject aLu, JSONObject aEn, JSONObject aFr, JSONObject aDe, int answer, int level, int points) {
		final List<QuizQuestionData> matches = this.query(QuizQuestionData.byAnyLocale(qLu, qEn, qFr, qDe))
				.catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: (" + qLu + ", " + qEn + ", " + qFr + ", " + qDe + ", " + answer + ", " + points + ")", e)).run();

		if (matches == null)
			return null;

		if (matches.size() > 0) {
			return matches.get(0);
		} else {
			return this.insertAndReload(new QuizQuestionData(qLu, qEn, qFr, qDe, aLu, aEn, aFr, aDe, answer, level, points))
					.catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: (" + qLu + ", " + qEn + ", " + qFr + ", " + qDe + ", " + answer + ", " + points + ")", e)).run();
		}
	}

	/*
	 * ========== UPDATE ==========
	 */

	@CachePut(value = "question.ids", key = "#qd.id")
	public QuizQuestionData updateQuestionData(QuizQuestionData qd) {
		return super.update(qd).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + qd, e)).run();
	}

	public void changeTable() {
		UnsafeSQLQuery<UserData> query = new UnsafeSQLQuery<>() {

			@Override
			public String getQuerySQL(SQLQueryable<UserData> table) {
				return "alter table users add answer_LU text, add answer_EN text, add answer_FR text;";
			}

			@Override
			public UserData clone() {
				return new UserData();
			}

		};
	}
}
