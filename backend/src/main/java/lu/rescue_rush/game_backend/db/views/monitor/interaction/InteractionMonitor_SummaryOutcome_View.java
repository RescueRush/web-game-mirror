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
@DB_View(name = "interaction_monitor_summary_outcome", tables = {
		@ViewTable(typeName = InteractionMonitorTable.class, columns = {
				@ViewColumn(name = "outcome"),
				@ViewColumn(func = "COUNT(id)", asName = "event_count")
		})
}, groupBy = { "outcome" },
orderBy = { @OrderBy(column = "event_count", type = OrderBy.Type.DESC) })
//@formatter:on
@Service
public class InteractionMonitor_SummaryOutcome_View extends R2DBView<InteractionMonitoringEntryData> {

	@Autowired
	private InteractionMonitorTable userTracking;

	public InteractionMonitor_SummaryOutcome_View(DataBase dbTest) {
		super(dbTest);
	}

}
