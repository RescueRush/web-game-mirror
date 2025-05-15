package lu.rescue_rush.game_backend.db.views.game.quiz;

import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.game.LeaderboardData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;
import lu.rescue_rush.game_backend.db.views.game.LeaderboardView;
import lu.rescue_rush.game_backend.types.UserTypes.LeaderboardEntry;

//@formatter:off
@DB_View(name = "quiz_game_stats_leaderboard", tables = {
		@ViewTable(typeName = QuizGameProgress_Stats_View.class, columns = {
				@ViewColumn(name = "*"),
				@ViewColumn(func = "ROW_NUMBER() OVER (ORDER BY total_points DESC)", asName = "row_index")
		}, asName = "gs"),
		@ViewTable(typeName = UserTable.class, join = ViewTable.Type.LEFT, columns = {
				@ViewColumn(name = "name", asName = "user_name")
		}, on = "`u`.`id` = `gs`.`user_id`", asName = "u")
})
//@formatter:on
@Service
public class QuizGameStats_Leaderboard_View extends R2DBView<LeaderboardData> implements LeaderboardView {

	public static final int ENTRIES_PER_PAGE = 15;

	@Autowired
	private QuizGameProgress_Stats_View quizGameStats;
	@Autowired
	private UserTable users;

	public QuizGameStats_Leaderboard_View(DataBase dbTest) {
		super(dbTest);
	}

	@NonNull
	@Override
	public LeaderboardEntry leaderboard(UserData ud) {
		if (ud == null) {
			return new LeaderboardEntry(-1, 0, null, null);
		}

		List<LeaderboardData> list = super.query(LeaderboardData.getUserLeaderboard(ud.getId())).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + ud, e)).run();

		if (list.isEmpty()) { // return default value
			return new LeaderboardEntry(-1, 0, ud.getName(), ud.getJoinDate());
		}

		return new LeaderboardEntry(list.get(0), ud.getJoinDate());
	}

	@Override
	public List<LeaderboardEntry> leaderboard(int page) {
		List<LeaderboardData> entries = super.query(LeaderboardData.getLeaderboardPage(ENTRIES_PER_PAGE, page * ENTRIES_PER_PAGE)).run();

		return entries.stream().map(LeaderboardEntry::new).toList();
	}

}
