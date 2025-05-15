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
@DB_View(name = "game_progress_summary_per_user_per_day", tables = {
		@ViewTable(typeName = GameProgress_View.class, columns = {
				@ViewColumn(name = "user_id"),
				@ViewColumn(func = "DATE(date)", asName = "date"),
				@ViewColumn(func = "SUM(total_points)", asName = "total_points"),
				@ViewColumn(func = "SUM(correct_questions)", asName = "correct_questions"),
				@ViewColumn(func = "SUM(total_questions)", asName = "total_questions"),
				@ViewColumn(func = "MAX(highest_streak)", asName = "highest_streak"),
				@ViewColumn(func = "SUM(started_games)", asName = "total_started_games")
		})
}, groupBy = { "user_id", "date" },
orderBy = { @OrderBy(column = "date"), @OrderBy(column = "user_id") })
//@formatter:on
@Service
public class GameProgress_SummaryPerUserPerDay_View extends R2DBView<ScenarioGameProgressData> {

	@Autowired
	private GameProgress_View gameProgress;

	public GameProgress_SummaryPerUserPerDay_View(DataBase dbTest) {
		super(dbTest);
	}

}
