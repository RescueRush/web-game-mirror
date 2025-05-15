package lu.rescue_rush.game_backend.db.tables.game.scenario;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.data.MaterialCard;
import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioQuestionData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

//@formatter:off
@DB_Table(name = "scenario_question", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "desc_LU", type = "text"),
		@Column(name = "answer_LU", type = "text"),
		@Column(name = "desc_EN", type = "text"),
		@Column(name = "answer_EN", type = "text"),
		@Column(name = "desc_FR", type = "text"),
		@Column(name = "answer_FR", type = "text"),
		@Column(name = "desc_DE", type = "text"),
		@Column(name = "answer_DE", type = "text"),
		@Column(name = "answers", type = "int"),
		@Column(name = "points", type = "int")
}, constraints = {
		@Constraint(name = "pk_scenario_question_id", type = Constraint.Type.PRIMARY_KEY, columns = "id")
})
//@formatter:on
@Service
public class ScenarioQuestionTable extends R2DBTable<ScenarioQuestionData> {

	public ScenarioQuestionTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== REQUEST SAFE ==========
	 */

	@Deprecated
	@RequestSafe
	public ScenarioQuestionData requestSafe_getRandom() {
		ScenarioQuestionData qd = random();
		SpringUtils.internalServerError(qd == null, "Question not found.");
		return qd;
	}

	@RequestSafe
	public ScenarioQuestionData requestSafe_getRandom(Queue<Integer> notIds) {
		ScenarioQuestionData qd = random(notIds);
		SpringUtils.internalServerError(qd == null, "Question not found.");
		return qd;
	}

	@RequestSafe
	@Cacheable(value = "question.ids", key = "#id")
	public ScenarioQuestionData requestSafe_byId(int id) {
		ScenarioQuestionData qd = byId(id);
		SpringUtils.internalServerError(qd == null, "Question not found.");
		return qd;
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Deprecated
	public ScenarioQuestionData random() {
		return super.query(ScenarioQuestionData.random()).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data", e)).run();
	}

	public ScenarioQuestionData random(Queue<Integer> notIds) {
		return super.query(ScenarioQuestionData.random(notIds)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data", e)).run();
	}

	@Cacheable(value = "question.ids", key = "#id")
	public ScenarioQuestionData byId(int id) {
		return super.load(new ScenarioQuestionData(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
	}

	public List<ScenarioQuestionData> byOffset(int offset, int limit) {
		return super.query(ScenarioQuestionData.byOffset(offset, limit)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	/*
	 * ========== UTILS ==========
	 */

	@SuppressWarnings("deprecation")
	@RequestSafe
	public ScenarioQuestionData safeInsert(String qLu, String qEn, String qFr, String qDe, String aLu, String aEn, String aFr, String aDe, MaterialCard[] answers, int points) {
		final List<ScenarioQuestionData> matches = this.query(ScenarioQuestionData.byAnyLocale(qLu, qEn, qFr, qDe))
				.catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: (" + qLu + ", " + qEn + ", " + qFr + ", " + qDe + ", " + Arrays.toString(answers) + ", " + points + ")", e)).run();

		SpringUtils.internalServerError(matches == null, "Error while querying data: (" + qLu + ", " + qEn + ", " + qFr + ", " + qDe + ", " + Arrays.toString(answers) + ", " + points + ")");

		if (matches.size() > 0) {
			return matches.get(0);
		} else {
			return this.insertAndReload(new ScenarioQuestionData(qLu, aLu, qEn, aEn, qFr, aFr, qDe, aDe, answers, points))
					.catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: (" + qLu + ", " + qEn + ", " + qFr + ", " + Arrays.toString(answers) + ", " + points + ")", e)).run();
		}
	}

	/*
	 * ========== UPDATE ==========
	 */

	@CachePut(value = "question.ids", key = "#qd.id")
	public ScenarioQuestionData updateQuestionData(ScenarioQuestionData qd) {
		return super.update(qd).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + qd, e)).run();
	}

}
