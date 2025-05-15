package lu.rescue_rush.game_backend.db.tables.monitor.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.user.UserProfileHistoryData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.monitor.interaction.InteractionMonitorTypeTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;

//@formatter:off
@DB_Table(name = "user_profile_history", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int", notNull = false),
		@Column(name = "type_id", type = "int"), @Column(name = "time", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "description", type = "text", notNull = false, default_ = "NULL"), @Column(name = "old_value", type = "varchar(320)", notNull = false),
		@Column(name = "new_value", type = "varchar(320)", notNull = false)
},constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_user_profile_history_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_profile_history_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_profile_history_type_id", columns = "type_id", referenceTableType = InteractionMonitorTypeTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.RESTRICT)
})
//@formatter:on
@Service
public class UserProfileHistoryTable extends R2DBTable<UserProfileHistoryData> {

	@Autowired
	private UserTable users;
	@Autowired
	private InteractionMonitorTypeTable types;

	public UserProfileHistoryTable(DataBase dbTest) {
		super(dbTest);
	}

}