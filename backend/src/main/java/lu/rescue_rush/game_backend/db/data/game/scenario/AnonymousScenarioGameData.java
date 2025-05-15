package lu.rescue_rush.game_backend.db.data.game.scenario;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLQueryable;

import lu.rescue_rush.game_backend.db.data.user.UserData;

public class AnonymousScenarioGameData extends ScenarioGameData {

	@Override
	public UserData loadUserData() {
		throw new UnsupportedOperationException("AnonymousQuizGameData does not support loading user data.");
	}

	@Override
	public UserData getUserData() {
		throw new UnsupportedOperationException("AnonymousQuizGameData does not support getting user data.");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		throw new UnsupportedOperationException("AnonymousQuizGameData does not support prepared insert SQL.");
	}

	@Override
	public <T extends SQLEntry> String getPreparedDeleteSQL(DataBaseTable<T> table) {
		throw new UnsupportedOperationException("AnonymousQuizGameData does not support prepared delete SQL.");
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		throw new UnsupportedOperationException("AnonymousQuizGameData does not support prepared select SQL.");
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		throw new UnsupportedOperationException("AnonymousQuizGameData does not support prepared update SQL.");
	}

}
