package lu.rescue_rush.game_backend.db.views.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.UnionTable;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.game.GameProgressData;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizGameProgressTable;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioGameProgressTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "game_progress", tables = {
	@ViewTable(typeName = GameProgress_View.class, join = ViewTable.Type.MAIN_UNION_ALL, columns = {
		@ViewColumn(name = "user_id"),
		@ViewColumn(name = "date"),
		@ViewColumn(func = "SUM(`started_games`)", asName = "started_games"),
		@ViewColumn(func = "SUM(`total_points`)", asName = "total_points"),
		@ViewColumn(func = "SUM(`total_questions`)", asName = "total_questions"),
		@ViewColumn(func = "SUM(`correct_questions`)", asName = "correct_questions"),
		@ViewColumn(func = "MAX(`highest_streak`)", asName = "highest_streak"),
		@ViewColumn(func = "MAX(`max_multiplier`)", asName = "max_multiplier")
	}, asName = "gp")
}, unionTables = {
	@UnionTable(typeName = ScenarioGameProgressTable.class, columns = {
		@ViewColumn(name = "user_id"),
		@ViewColumn(name = "date"),
		@ViewColumn(name = "started_games"),
		@ViewColumn(name = "total_points"),
		@ViewColumn(name = "total_questions"),
		@ViewColumn(name = "correct_questions"),
		@ViewColumn(name = "highest_streak"),
		@ViewColumn(name = "max_multiplier")
	}),
	@UnionTable(typeName = QuizGameProgressTable.class, columns = {
		@ViewColumn(name = "user_id"),
		@ViewColumn(name = "date"),
		@ViewColumn(name = "started_games"),
		@ViewColumn(name = "total_points"),
		@ViewColumn(name = "total_questions"),
		@ViewColumn(name = "correct_questions"),
		@ViewColumn(name = "highest_streak"),
		@ViewColumn(name = "max_multiplier")
	}),
}, groupBy = { "user_id", "date" })
//@formatter:on
@Service
public class GameProgress_View extends R2DBView<GameProgressData> {

	@Autowired
	private ScenarioGameProgressTable quizGameProgresses;
	@Autowired
	private QuizGameProgressTable guessGameProgresses;

	@Autowired
	@Lazy
	public GameProgress_AveragePerDay_View AVERAGE_PER_DAY;
	@Autowired
	@Lazy
	public GameProgress_SummaryPerUser_View SUMMARY_PER_USER;
	@Autowired
	@Lazy
	public GameProgress_SummaryPerUserPerDay_View SUMMARY_PER_USER_PER_DAY;
	@Autowired
	@Lazy
	public GameProgress_TopUsersByGamesStarted_View TOP_USERS_BY_GAMES_STARTED;
	@Autowired
	@Lazy
	public GameProgress_TopUsersByStreak_View TOP_USERS_BY_STREAK;
	@Autowired
	@Lazy
	public GameProgress_Stats_View STATS;

	public GameProgress_View(DataBase dbTest) {
		super(dbTest);
	}

}
