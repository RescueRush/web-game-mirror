package lu.rescue_rush.game_backend.db.tables.support;

import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.support.SupportFormData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.support.SupportForm_CountPerEmail_View;
import lu.rescue_rush.game_backend.db.views.support.SupportForm_CountPerName_View;
import lu.rescue_rush.game_backend.db.views.support.SupportForm_CountPerUser_View;
import lu.rescue_rush.game_backend.db.views.support.SupportForm_DiscordNotConfirmed_View;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

//@formatter:off
@DB_Table(name = "support_form", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int", notNull = false),
		@Column(name = "name", type = "varchar(200)"),
		@Column(name = "email", type = "varchar(320)"),
		@Column(name = "message", type = "text"),
		@Column(name = "lang", type = "enum('LU', 'EN', 'FR', 'DE')"),
		@Column(name = "submit_date", type = "timestamp", notNull = true, default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "discord_confirmed", type = "bit", notNull = true, default_ = "0"),
		@Column(name = "email_confirmed", type = "bit", notNull = true, default_ = "0")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_email_forms_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_email_forms_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL)
})
//@formatter:on
@Service
public class SupportFormTable extends R2DBTable<SupportFormData> {

	@Autowired
	@Lazy
	public SupportForm_CountPerEmail_View COUNT_PER_EMAIL;
	@Autowired
	@Lazy
	public SupportForm_CountPerUser_View COUNT_PER_USER;
	@Autowired
	@Lazy
	public SupportForm_CountPerName_View COUNT_PER_NAME;
	@Autowired
	@Lazy
	public SupportForm_DiscordNotConfirmed_View DISCORD_NOT_CONFIRMED;

	@Autowired
	private UserTable users;

	public SupportFormTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== REQUEST SAFE ==========
	 */

	@RequestSafe
	public SupportFormData requestSafe_insertAndReload(SupportFormData sefd) {
		sefd.validateData();

		return super.insertAndReload(sefd).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + sefd, e)).run();
	}

	/*
	 * ========== DIRECT ==========
	 */

	public List<SupportFormData> byEmail(String email) {
		return super.query(SupportFormData.byEmail(email)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + email, e)).run();
	}

	/*
	 * ========== UTILS ==========
	 */

	public int countEntries() {
		return super.count().run();
	}

	public SupportFormData updateSupportEmailFormData(SupportFormData sefd) {
		return super.update(sefd).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + sefd, e)).run();
	}

}
