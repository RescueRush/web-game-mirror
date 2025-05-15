package lu.rescue_rush.game_backend.db.views.game.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.game.GameProgressData;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizGameProgressTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "quiz_game_stats", tables = {
	@ViewTable(typeName = QuizGameProgressTable.class, columns = {
		@ViewColumn(name = "user_id"),
		@ViewColumn(func = "SUM(`started_games`)", asName = "started_games"),
		@ViewColumn(func = "SUM(`total_points`)", asName = "total_points"),
		@ViewColumn(func = "SUM(`total_questions`)", asName = "total_questions"),
		@ViewColumn(func = "SUM(`correct_questions`)", asName = "correct_questions"),
		@ViewColumn(func = "MAX(`highest_streak`)", asName = "highest_streak"),
		@ViewColumn(func = "MAX(`max_multiplier`)", asName = "max_multiplier")
	})
}, groupBy = { "user_id" })
//@formatter:on
@Service
public class QuizGameProgress_Stats_View extends R2DBView<GameProgressData> {

	@Autowired
	private QuizGameProgressTable guessGameProgresses;

	public QuizGameProgress_Stats_View(DataBase dbTest) {
		super(dbTest);
	}

}
