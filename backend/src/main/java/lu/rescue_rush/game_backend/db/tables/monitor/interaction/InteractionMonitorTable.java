package lu.rescue_rush.game_backend.db.tables.monitor.interaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.monitor.interaction.InteractionMonitoringEntryData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_ActivityPerUserPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_ActivityPerUser_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_CountPerSource_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_CountPerType_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_Newest_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_SummaryOutcome_View;
import lu.rescue_rush.game_backend.utils.SpringUtils;

//@formatter:off
@DB_Table(name = "interaction_monitor", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int", notNull = false),
		@Column(name = "type", type = "int"),
		@Column(name = "outcome", type = "bit", notNull = false, default_ = "1"),
		@Column(name = "time", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "source", type = "varbinary(16)"),
		@Column(name = "description", type = "text", notNull = false, default_ = "NULL")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_interaction_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_interaction_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_interaction_type_id", columns = "type", referenceTableType = InteractionMonitorTypeTable.class, referenceColumn = "id")
})
//@formatter:on
@Service
public class InteractionMonitorTable extends R2DBTable<InteractionMonitoringEntryData> {

	@Autowired
	@Lazy
	public InteractionMonitor_ActivityPerUser_View ACTIVITY_PER_USER;
	@Autowired
	@Lazy
	public InteractionMonitor_ActivityPerUserPerDay_View ACTIVITY_PER_USER_PER_DAY;
	@Autowired
	@Lazy
	public InteractionMonitor_CountPerSource_View COUNT_PER_SOURCE;
	@Autowired
	@Lazy
	public InteractionMonitor_CountPerType_View COUNT_PER_TYPE;
	@Autowired
	@Lazy
	public InteractionMonitor_Newest_View NEWEST;
	@Autowired
	@Lazy
	public InteractionMonitor_SummaryOutcome_View SUMMARY_OUTCOME;

	@Autowired
	private UserTable users;
	@Autowired
	private InteractionMonitorTypeTable types;

	public InteractionMonitorTable(DataBase dbTest) {
		super(dbTest);
	}

	public InteractionMonitoringEntryData byId(int id) {
		return super.query(InteractionMonitoringEntryData.byId(id)).thenApply(SpringUtils.first()).run();
	}
}
