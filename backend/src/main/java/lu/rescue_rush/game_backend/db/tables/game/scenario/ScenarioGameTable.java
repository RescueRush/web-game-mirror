package lu.rescue_rush.game_backend.db.tables.game.scenario;

import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioGameData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;

//@formatter:off
@DB_Table(name = "scenario_game", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int"),
		@Column(name = "question_offset", type = "int"),
		@Column(name = "current_question_id", type = "int", notNull = false),
		@Column(name = "current_multiplier", type = "int", default_ = "2"),
		@Column(name = "current_points", type = "int", default_ = "0"),
		@Column(name = "correct_questions", type = "int", default_ = "0"),
		@Column(name = "total_questions", type = "int", default_ = "0"),
		@Column(name = "streak_count", type = "int", default_ = "0"),
		@Column(name = "start_time", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "latest_question", type = "text"/*, default_ = "'[]'"*/)
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_scenario_game_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_scenario_game_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.CASCADE),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_scenario_game_question_id", columns = "current_question_id", referenceTableType = ScenarioQuestionTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_scenario_game_user_id", columns = "user_id")
})
//@formatter:on
@Service
public class ScenarioGameTable extends R2DBTable<ScenarioGameData> {

	@Autowired
	private UserTable users;
	@Autowired
	private ScenarioQuestionTable questions;

	public ScenarioGameTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "scenario-game.user.ids", key = "#ud.id")
	public ScenarioGameData create(UserData ud) {
		return super.insertAndReload(new ScenarioGameData(ud)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: (" + ud + ")", e)).run();
	}

	@Cacheable(value = "scenario-game.user.ids", key = "#ud.id")
	public ScenarioGameData byUserId(int id) {
		return super.query(ScenarioGameData.byUserId(id)).thenApply(SpringUtils.first()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/loading data: " + id, e)).run();
	}

	@Cacheable(value = "scenario-game.user.ids", key = "#ud.id")
	public ScenarioGameData byUser(UserData ud) {
		return byUserId(ud.getId());
	}

	@CacheEvict(value = "scenario-game.user.ids", key = "#ud.id")
	public void endGame(UserData ud) {
		ScenarioGameData qgz = this.byUserId(ud.getId());
		super.delete(qgz).run();
	}

	/*
	 * ========== UTILS ==========
	 */

	public boolean hasGame(UserData ud) {
		return super.exists(new ScenarioGameData(ud)).run();
	}

	/*
	 * ========== UPDATE ==========
	 */

	@CachePut(value = "scenario-game.user.ids", key = "#qd.userId")
	public ScenarioGameData updateScenarioGameData(ScenarioGameData qd) {
		return super.update(qd).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + qd, e)).run();
	}

}
