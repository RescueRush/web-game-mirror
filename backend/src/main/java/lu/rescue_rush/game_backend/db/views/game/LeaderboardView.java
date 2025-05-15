package lu.rescue_rush.game_backend.db.views.game;

import java.util.List;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.types.UserTypes.LeaderboardEntry;

public interface LeaderboardView {

	LeaderboardEntry leaderboard(UserData ud);

	List<LeaderboardEntry> leaderboard(int page);

}
