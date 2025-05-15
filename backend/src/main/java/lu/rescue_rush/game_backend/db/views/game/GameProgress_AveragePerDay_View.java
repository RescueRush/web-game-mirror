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
@DB_View(name = "game_progress_average_per_day", tables = {
		@ViewTable(typeName = GameProgress_View.class, columns = {
				@ViewColumn(func = "DATE(date)", asName = "date"),
				@ViewColumn(func = "AVG(total_points)", asName = "avg_points"),
				@ViewColumn(func = "AVG(correct_questions)", asName = "avg_correct_questions"),
				@ViewColumn(func = "AVG(total_questions)", asName = "avg_question")
		})
}, groupBy = { "date" },
orderBy = { @OrderBy(column = "date") })
//@formatter:on
@Service
public class GameProgress_AveragePerDay_View extends R2DBView<ScenarioGameProgressData> {

	@Autowired
	private GameProgress_View gameProgress;

	public GameProgress_AveragePerDay_View(DataBase dbTest) {
		super(dbTest);
	}

}
