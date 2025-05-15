package lu.rescue_rush.game_backend.db.views.monitor.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.view.DB_View;
import lu.pcy113.pclib.db.annotations.view.ViewColumn;
import lu.pcy113.pclib.db.annotations.view.ViewTable;

import lu.rescue_rush.game_backend.db.data.monitor.execution.ExecutionMonitorData;
import lu.rescue_rush.game_backend.db.tables.monitor.execution.ExecutionMonitorTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;

//@formatter:off
@DB_View(name = "execution_monitor_summary_per_key", tables = {
		@ViewTable(typeName = ExecutionMonitorTable.class, columns = {
				@ViewColumn(name = "key"),
				@ViewColumn(func = "SUM(entry_count)", asName = "total_entry_count"),
				@ViewColumn(func = "SUM(sum)", asName = "total_sum"),
				@ViewColumn(func = "AVG(sum)", asName = "avg_sum"),
				@ViewColumn(func = "AVG(avg)", asName = "avg_avg"),
				@ViewColumn(func = "AVG(median)", asName = "avg_median"),
				@ViewColumn(func = "AVG(dev)", asName = "avg_dev"),
		})
}, groupBy = "key")
//@formatter:on
@Service
public class ExecutionMonitor_SummaryPerKey_View extends R2DBView<ExecutionMonitorData> {

	@Autowired
	private ExecutionMonitorTable perfs;

	public ExecutionMonitor_SummaryPerKey_View(DataBase dbTest) {
		super(dbTest);
	}

}
