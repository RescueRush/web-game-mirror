package lu.rescue_rush.game_backend.db.views.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.game.GameStatsData;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "game_stats", tables = {
	@ViewTable(typeName = GameProgress_View.class, columns = {
		@ViewColumn(name = "user_id"),
		@ViewColumn(func = "SUM(`started_games`)", asName = "started_games"),
		@ViewColumn(func = "SUM(`total_points`)", asName = "total_points"),
		@ViewColumn(func = "SUM(`total_questions`)", asName = "total_questions"),
		@ViewColumn(func = "SUM(`correct_questions`)", asName = "correct_questions"),
		@ViewColumn(func = "MAX(`max_multiplier`)", asName = "max_multiplier")
	})
}, groupBy = "user_id")
//@formatter:on
@Service
public class GameProgress_Stats_View extends R2DBView<GameStatsData> {

	@Autowired
	private GameProgress_View gameProgress;

	@Autowired
	@Lazy
	public GameStats_Leaderboard_View LEADERBOARD;

	public GameProgress_Stats_View(DataBase dbTest) {
		super(dbTest);
	}

}
