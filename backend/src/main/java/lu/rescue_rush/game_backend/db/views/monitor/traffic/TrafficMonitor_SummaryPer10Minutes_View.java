package lu.rescue_rush.game_backend.db.views.monitor.traffic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.OrderBy;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.monitor.traffic.AggregateTrafficData;
import lu.rescue_rush.game_backend.db.tables.monitor.traffic.TrafficMonitorTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "traffic_monitor_summary_per_10_minutes", tables = {
		@ViewTable(typeName = TrafficMonitorTable.class, columns = {
				@ViewColumn(func = "DATE_FORMAT(`datetime`, '%Y-%m-%d %H:%i:00') - INTERVAL MINUTE(`datetime`) % 10 MINUTE", asName = "time"),
				@ViewColumn(name = "domain_path"),
				@ViewColumn(func = "COUNT(*)", asName = "count")
		})
}, groupBy = { "time", "domain_path" },
orderBy = { @OrderBy(column = "time", type = OrderBy.Type.DESC) })
//@formatter:on
@Service
public class TrafficMonitor_SummaryPer10Minutes_View extends R2DBView<AggregateTrafficData> {

	@Autowired
	private TrafficMonitorTable trafficMonitoring;

	public TrafficMonitor_SummaryPer10Minutes_View(DataBase dbTest) {
		super(dbTest);
	}

}
