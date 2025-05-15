package lu.rescue_rush.game_backend.db.views.monitor.interaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.OrderBy;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.monitor.interaction.InteractionMonitoringEntryData;
import lu.rescue_rush.game_backend.db.tables.monitor.interaction.InteractionMonitorTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "interaction_monitor_newest", tables = {
		@ViewTable(typeName = InteractionMonitorTable.class, columns = {
				@ViewColumn(name = "id"),
				@ViewColumn(name = "user_id"),
				@ViewColumn(name = "type"),
				@ViewColumn(name = "source"),
				@ViewColumn(name = "outcome"),
				@ViewColumn(func = "DATE(time)", asName = "event_date")
		})
}, condition = "time > NOW() - INTERVAL 7 DAY",
orderBy = { @OrderBy(column = "event_date", type = OrderBy.Type.DESC) })
//@formatter:on
@Service
public class InteractionMonitor_Newest_View extends R2DBView<InteractionMonitoringEntryData> {

	@Autowired
	private InteractionMonitorTable userTracking;

	public InteractionMonitor_Newest_View(DataBase dbTest) {
		super(dbTest);
	}

}
