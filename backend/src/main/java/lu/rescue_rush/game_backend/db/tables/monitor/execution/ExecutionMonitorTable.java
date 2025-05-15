package lu.rescue_rush.game_backend.db.tables.monitor.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.monitor.execution.ExecutionMonitorData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.views.monitor.execution.ExecutionMonitor_SummaryPerKeyPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.execution.ExecutionMonitor_SummaryPerKey_View;

//@formatter:off
@DB_Table(name = "execution_monitor", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "key", type = "varchar(100)"),
		@Column(name = "time_start", type = "timestamp"),
		@Column(name = "time_end", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "entry_count", type = "int"),
		@Column(name = "sum", type = "double"),
		@Column(name = "avg", type = "double"),
		@Column(name = "median", type = "double"),
		@Column(name = "dev", type = "double")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_execution_id", columns = "id")
})
//@formatter:on
@Service
public class ExecutionMonitorTable extends R2DBTable<ExecutionMonitorData> {

	@Autowired
	@Lazy
	public ExecutionMonitor_SummaryPerKey_View SUMMARY_PER_KEY;
	@Autowired
	@Lazy
	public ExecutionMonitor_SummaryPerKeyPerDay_View SUMMARY_PER_KEY_PER_DAY;

	public ExecutionMonitorTable(DataBase dbTest) {
		super(dbTest);
	}

}
