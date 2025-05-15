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
@DB_View(name = "traffic_monitor_top_referer_per_day", tables = {
		@ViewTable(typeName = TrafficMonitorTable.class, columns = {
				@ViewColumn(func = "DATE_FORMAT(`datetime`, '%Y-%m-%d 00:00:00')", asName = "time"),
				@ViewColumn(name = "referer"),
				@ViewColumn(func = "COUNT(*)", asName = "count")
		})
}, condition = "referer IS NOT NULL",
groupBy = { "time", "referer" },
orderBy = { @OrderBy(column = "time", type = OrderBy.Type.DESC), @OrderBy(column = "count", type = OrderBy.Type.DESC) })
//@formatter:on
@Service
public class TrafficMonitor_TopRefererPerDay_View extends R2DBView<AggregateTrafficData> {

	@Autowired
	private TrafficMonitorTable trafficMonitoring;

	public TrafficMonitor_TopRefererPerDay_View(DataBase dbTest) {
		super(dbTest);
	}

}
