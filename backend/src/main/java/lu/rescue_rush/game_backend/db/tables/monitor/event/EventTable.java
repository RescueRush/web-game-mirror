package lu.rescue_rush.game_backend.db.tables.monitor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.monitor.event.EventEntryData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;

//@formatter:off
@DB_Table(name = "event", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "type", type = "int"),
		@Column(name = "time", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "description", type = "text", notNull = false, default_ = "NULL")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_event_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_event_type", columns = "type", referenceTableType = EventTypeTable.class, referenceColumn = "id"),
		@Constraint(type = Constraint.Type.INDEX, name = "idx_event_type", columns = "type"),
		@Constraint(type = Constraint.Type.INDEX, name = "idx_event_time", columns = "time")
})
//@formatter:on
@Service
public class EventTable extends R2DBTable<EventEntryData> {

	@Autowired
	private EventTypeTable eventTypes;

	public EventTable(DataBase dbTest) {
		super(dbTest);
	}

}
