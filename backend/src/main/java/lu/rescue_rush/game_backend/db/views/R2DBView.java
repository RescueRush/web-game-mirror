package lu.rescue_rush.game_backend.db.views;

import java.util.logging.Logger;

import com.mysql.cj.PreparedQuery;
import com.mysql.cj.jdbc.ClientPreparedStatement;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.DataBaseView;
import lu.pcy113.pclib.db.SQLRequestType;
import lu.pcy113.pclib.db.impl.SQLEntry;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.db.R2DBBase;
import lu.rescue_rush.game_backend.utils.SpringUtils;

public class R2DBView<T extends SQLEntry> extends DataBaseView<T> {

	protected Logger LOGGER;

	public R2DBView(DataBase dbTest) {
		super(dbTest);

		LOGGER = Logger.getLogger("VIEW # " + super.getName());
	}

	@PostConstruct
	public void init() {
		((R2DBBase) super.getDataBase()).create(this);
	}

	@Override
	public void requestHook(SQLRequestType type, Object query) {
		if (R2ApiMain.DEBUG) {
			LOGGER.info(query == null ? null
					: (query instanceof ClientPreparedStatement query1 ? ((PreparedQuery) query1.getQuery()).asSql() : query.toString()) + " from " + SpringUtils.getFilteredUtils());
		}
	}

}
