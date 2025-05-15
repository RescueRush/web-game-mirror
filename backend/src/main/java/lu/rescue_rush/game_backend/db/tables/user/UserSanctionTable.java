package lu.rescue_rush.game_backend.db.tables.user;

import java.util.List;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.async.NextTask;
import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;

//@formatter:off
@DB_Table(name = "user_sanction", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int"),
		@Column(name = "author_id", type = "int", notNull = false),
		@Column(name = "canceller_id", type = "int", notNull = false),
		@Column(name = "reason_id", type = "int"),
		@Column(name = "description", type = "text", notNull = false),
		@Column(name = "issue_date", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "cancel_date", type = "timestamp", default_ = "NULL", notNull = false),
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_user_sanctions_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_sanctions_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.CASCADE),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_sanctions_author_id", columns = "author_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_sanctions_canceller_id", columns = "canceller_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_sanctions_reason_id", columns = "reason_id", referenceTableType = UserSanctionReasonTable.class, referenceColumn = "id")
})
//@formatter:on
@Service
public class UserSanctionTable extends R2DBTable<UserSanctionData> {

	@Autowired
	private UserTable users;
	@Autowired
	private UserSanctionReasonTable reasons;

	public UserSanctionTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user-sanction.user-tokens", key = "#token")
	public List<UserSanctionData> byUserToken(String token) {
		List<UserSanctionData> listUsd = super.query(UserSanctionData.byUserToken(token)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + token, e)).run();
		return listUsd;
	}

	@Cacheable(value = "user-sanction.user-ids", key = "#id")
	public List<UserSanctionData> byUserId(int id) {
		List<UserSanctionData> listUsd = super.query(UserSanctionData.byUserId(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
		return listUsd;
	}

	@Cacheable(value = "user-sanction.author-ids", key = "#id")
	public List<UserSanctionData> byAuthorId(int id) {
		List<UserSanctionData> listUsd = super.query(UserSanctionData.byAuthorId(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
		return listUsd;
	}

	@Cacheable(value = "user-sanction.user-ids", key = "#user.id")
	public List<UserSanctionData> byUser(UserData user) {
		return byUserId(user.getId());
	}

	@Cacheable(value = "user-sanction.author-ids", key = "#author.id")
	public List<UserSanctionData> byAuthor(UserData author) {
		return byAuthorId(author.getId());
	}
	/*
	 * public List<UserSanctionData> byOffset(int offset, int limit){ return
	 * super.query(UserSanctionData.byOffset(offset, limit)).catch_(e ->
	 * LOGGER.log(Level.SEVERE, "Error while querying data! ", e)).run(); }
	 */
	/*
	 * ========== UPDATE ==========
	 */

	//@formatter:off
	@Caching(put={
			@CachePut(value="user-sanction.user-ids", key="#userSanctionData.userId"),
			@CachePut(value="user-sanction.author-ids", key="#userSanctionData.authorId")
	})
	//@formatter:on
	public UserSanctionData updateUserSanctionData(UserSanctionData userSanctionData) {
		return super.update(userSanctionData).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + userSanctionData, e)).run();
	}

	/*
	 * ========== DELETE ==========
	 */

	public NextTask<Void, Integer> deleteByUserId(int userId) {
		return super.query(UserSanctionData.byUserId(userId)).thenApply(l -> {
			for (UserSanctionData upd : l) {
				super.delete(upd).run();
			}
			return l.size();
		});
	}

}
