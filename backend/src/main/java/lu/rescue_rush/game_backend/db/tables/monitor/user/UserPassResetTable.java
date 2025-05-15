package lu.rescue_rush.game_backend.db.tables.monitor.user;

import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.monitor.interaction.UserPassResetData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;

//@formatter:off
@DB_Table(name = "user_pass_resets", columns = { 
		@Column(name = "id", type = "int", autoIncrement = true), 
		@Column(name = "user_id", type = "int"), 
		@Column(name = "token", type = "varchar(120)"),
		@Column(name = "expires_at", type = "timestamp"), 
		@Column(name = "token_used", type = "boolean", default_ = "1") 
}, constraints = {
		@Constraint(name = "pk_user_pass_resets_id", columns = "id", type = Constraint.Type.PRIMARY_KEY),
		@Constraint(name = "fk_user_pass_resets_user_id", columns = "user_id", type = Constraint.Type.FOREIGN_KEY, referenceTableType = UserTable.class, referenceColumn = "id"),
		@Constraint(name = "uq_user_pass_resets_token", columns = "token", type = Constraint.Type.UNIQUE)
})
//@formatter:on
@Service
public class UserPassResetTable extends R2DBTable<UserPassResetData> {

	public UserPassResetTable(DataBase dbTest) {
		super(dbTest);
	}

	public UserPassResetData byToken(String token) {
		return super.query(UserPassResetData.byToken(token)).thenApply(SpringUtils.first(() -> null)).run();
	}

	public UserPassResetData requestSafe_byToken(String token) {
		UserPassResetData uprd = byToken(token);
		SpringUtils.badRequest(uprd == null, "Data not found.");
		return uprd;
	}

}
