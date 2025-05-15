package lu.rescue_rush.game_backend.db.views.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.OrderBy;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioGameProgressData;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "game_progress_top_user_by_streak", tables = {
		@ViewTable(typeName = GameProgress_View.class, columns = {
				@ViewColumn(name = "user_id"),
				@ViewColumn(func = "MAX(max_multiplier)", asName = "max_multiplier"),
				@ViewColumn(func = "SUM(total_points)", asName = "total_points"),
				@ViewColumn(func = "SUM(correct_questions)", asName = "correct_questions")
		})
}, groupBy = { "user_id" },
orderBy = { @OrderBy(column = "max_multiplier", type = OrderBy.Type.DESC) })
//@formatter:on
@Service
public class GameProgress_TopUsersByStreak_View extends R2DBView<ScenarioGameProgressData> {

	@Autowired
	private GameProgress_View gameProgress;

	public GameProgress_TopUsersByStreak_View(DataBase dbTest) {
		super(dbTest);
	}

}
