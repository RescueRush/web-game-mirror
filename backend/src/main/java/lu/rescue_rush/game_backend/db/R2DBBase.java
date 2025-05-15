package lu.rescue_rush.game_backend.db;

import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.mysql.cj.PreparedQuery;
import com.mysql.cj.jdbc.ClientPreparedStatement;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.DataBaseConnector;
import lu.pcy113.pclib.db.DataBaseView;
import lu.pcy113.pclib.db.SQLRequestType;
import lu.pcy113.pclib.db.annotations.base.DB_Base;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@DB_Base(name = "rescue_rush")
@Component
public class R2DBBase extends DataBase {

	protected final Logger LOGGER = Logger.getLogger(R2DBBase.class.getName());

	public static R2DBBase BASE;

	public R2DBBase(DataBaseConnector dbConfig) {
		super(dbConfig);

		BASE = this;
	}

	@PostConstruct
	public void init() {
		create(this);
	}

	@Override
	public void requestHook(SQLRequestType type, Object query) {
		if (R2ApiMain.DEBUG) {
			LOGGER.info(query == null ? null
					: (query instanceof ClientPreparedStatement query1 ? ((PreparedQuery) query1.getQuery()).asSql() : query.toString()) + " from " + SpringUtils.getFilteredUtils());
		}
	}

	public void create(R2DBTable<?> table) {
		final String name = table.getQualifiedName();

		//@formatter:off
		table.create()
				.catch_(SpringUtils.catch_(LOGGER, e -> "Error creating Table: " + name + " (" + table.getClass().getName() + ") (" + e.getMessage() + ")\n" + table.getCreateSQL()))
				.thenConsume((e) -> LOGGER.info(!e.existed() ? "Table: " + name + " created" : "Table: " + name + " already exists"))
		.run();
		//@formatter:on
	}

	public void create(DataBaseView<?> view) {
		final String name = view.getQualifiedName();

		//@formatter:off
		view.create()
				.catch_(SpringUtils.catch_(LOGGER, e -> "Error creating View: " + name + " (" + view.getClass().getName() + ") (" + e.getMessage() + ")\n" + view.getCreateSQL()))
				.thenConsume((e) -> LOGGER.info(!e.existed() ? "View: " + name + " created" : "View: " + name + " already exists"))
		.run();
		//@formatter:on
	}

	public void create(DataBase base) {
		final String name = "`" + base.getDataBaseName() + "`";

		//@formatter:off
		base.create()
				.catch_(SpringUtils.catch_(LOGGER, e -> "Error creating Base: " + name + " (" + base.getClass().getName() + ") (" + e.getMessage() + ")\n" + base.getCreateSQL()))
				.thenConsume((e) -> LOGGER.info(!e.existed() ? "Base: " + name + " created" : "Base: " + name + " already exists"))
				.thenConsume((e) -> base.updateDataBaseConnector())
				.catch_(SpringUtils.catch_(LOGGER, "Error updating DataBaseConnector"))
		.run();
		//@formatter:on
	}

}
