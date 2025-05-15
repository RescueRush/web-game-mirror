package lu.rescue_rush.game_backend.db.tables.game.quiz;

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

import lu.rescue_rush.game_backend.db.data.game.guess.QuizGameProgressData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.game.GameProgress_View;

//@formatter:off
@DB_Table(name="quiz_game_progress", columns={
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
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_quiz_game_progress_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_quiz_game_progress_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.CASCADE),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_quiz_game_progress_user_date", columns = {"user_id", "date"})
})
//@formatter:on
@Service
public class QuizGameProgressTable extends R2DBTable<QuizGameProgressData> {

	@Autowired
	@Lazy
	public GameProgress_View STATS;

	@Autowired
	private UserTable users;

	public QuizGameProgressTable(DataBase dbTest) {
		super(dbTest);
	}

	@NonNull
	@Cacheable(value = "quiz-game-progress.ids", key = "#ud.id")
	public QuizGameProgressData loadOrInsertByUser(UserData ud) {
		return loadOrInsertByUser(ud.getId());
	}

	@NonNull
	@Cacheable(value = "quiz-game-progress.ids", key = "#userId")
	public QuizGameProgressData loadOrInsertByUser(int userId) {
		final List<QuizGameProgressData> data = super.query(QuizGameProgressData.byUserIdAndToday(userId)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + userId, e)).run();
		if (!data.isEmpty()) {
			return data.get(0);
		}

		return super.insertAndReload(new QuizGameProgressData(userId)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/reloading data: " + userId, e)).run();
	}

	@CachePut(value = "quiz-game-progress.ids", key = "#data.userId")
	public QuizGameProgressData updateQuizGameProgressData(QuizGameProgressData data) {
		return super.update(data).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + data, e)).run();
	}

}
