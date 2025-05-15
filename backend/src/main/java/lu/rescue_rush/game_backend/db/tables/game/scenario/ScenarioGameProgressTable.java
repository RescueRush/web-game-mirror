package lu.rescue_rush.game_backend.db.tables.game.scenario;

import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioGameProgressData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.game.GameProgress_View;
import lu.rescue_rush.game_backend.utils.SpringUtils;

//@formatter:off
@DB_Table(name="scenario_game_progress", columns={
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int"),
		@Column(name = "date", type = "DATE", default_ = "(CURRENT_DATE)"),
		@Column(name = "total_points", type = "int", default_ = "0"),
		@Column(name = "correct_questions", type = "int", default_ = "0"),
		@Column(name = "total_questions", type = "int", default_ = "0"),
		@Column(name = "highest_streak", type = "int", default_ = "0"),
		@Column(name = "max_multiplier", type = "int", default_ = "2"),
		@Column(name = "started_games", type = "int", default_ = "0"),
		@Column(name = "last_update", type = "TIMESTAMP", default_ = "CURRENT_TIMESTAMP", onUpdate = "CURRENT_TIMESTAMP")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_scenario_game_progress_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_scenario_game_progress_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.CASCADE),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_quiz_game_progress_user_date", columns = {"user_id", "date"})
})
//@formatter:on
@Service
public class ScenarioGameProgressTable extends R2DBTable<ScenarioGameProgressData> {

	@Autowired
	@Lazy
	public GameProgress_View STATS;

	@Autowired
	private UserTable users;
	@Autowired
	private ScenarioQuestionTable quizQuestions;

	public ScenarioGameProgressTable(DataBase dbTest) {
		super(dbTest);
	}

	@NonNull
	@Cacheable(value = "scenario-game-progress.ids", key = "#ud.id")
	public ScenarioGameProgressData loadOrInsertByUser(UserData ud) {
		return loadOrInsertByUser(ud.getId());
	}

	@NonNull
	@Cacheable(value = "scenario-game-progress.ids", key = "#userId")
	public ScenarioGameProgressData loadOrInsertByUser(int userId) {
		List<ScenarioGameProgressData> data = super.query(ScenarioGameProgressData.byUserIdAndToday(userId)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + userId, e)).run();

		SpringUtils.internalServerError(data == null, "Returned data is null for user: " + userId);

		if (!data.isEmpty()) {
			return data.get(0);
		}

		return super.insertAndReload(new ScenarioGameProgressData(userId)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/reloading data: " + userId, e)).run();
	}

	@CachePut(value = "scenario-game-progress.ids", key = "#quizGameProgressData.userId")
	public ScenarioGameProgressData updateScenarioGameProgressData(ScenarioGameProgressData quizGameProgressData) {
		return super.update(quizGameProgressData).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + quizGameProgressData, e)).run();
	}

}
