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
import lu.rescue_rush.game_backend.db.data.user.UserPermissionData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;

//@formatter:off
@DB_Table(name = "user_permission", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "user_id", type = "int"),
		@Column(name = "type_id", type = "int"),
		@Column(name = "author_id", type = "int", notNull = false),
		@Column(name = "description", type = "text", notNull = false),
		@Column(name = "issue_date", type = "timestamp", default_ = "CURRENT_TIMESTAMP")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_user_permissions_id", columns = "id"),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_permissions_user_id", columns = "user_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.CASCADE),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_permissions_author_id", columns = "author_id", referenceTableType = UserTable.class, referenceColumn = "id", onDelete = Constraint.OnEvent.SET_NULL),
		@Constraint(type = Constraint.Type.FOREIGN_KEY, name = "fk_user_permissions_type_id", columns = "type_id", referenceTableType = UserPermissionTypeTable.class, referenceColumn = "id")
})
//@formatter:on
@Service
public class UserPermissionTable extends R2DBTable<UserPermissionData> {

	@Autowired
	private UserTable users;
	@Autowired
	private UserPermissionTypeTable types;

	public UserPermissionTable(DataBase dbTest) {
		super(dbTest);
	}

	/*
	 * ========== DIRECT ==========
	 */

	@Cacheable(value = "user-permission.user-tokens", key = "#token")
	public List<UserPermissionData> byUserToken(String token) {
		List<UserPermissionData> listUsd = super.query(UserPermissionData.byUserToken(token)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + token, e)).run();
		return listUsd;
	}

	@Cacheable(value = "user-permission.user-ids", key = "#id")
	public List<UserPermissionData> byUserId(int id) {
		List<UserPermissionData> listUsd = super.query(UserPermissionData.byUserId(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
		return listUsd;
	}

	@Cacheable(value = "user-permission.author-ids", key = "#id")
	public List<UserPermissionData> byAuthorId(int id) {
		List<UserPermissionData> listUsd = super.query(UserPermissionData.byAuthorId(id)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data: " + id, e)).run();
		return listUsd;
	}

	@Cacheable(value = "user-permission.user-ids", key = "#user.id")
	public List<UserPermissionData> byUser(UserData user) {
		return byUserId(user.getId());
	}

	@Cacheable(value = "user-permission.author-ids", key = "#author.id")
	public List<UserPermissionData> byAuthor(UserData author) {
		return byAuthorId(author.getId());
	}

	/*
	 * ========== UPDATE ==========
	 */

	//@formatter:off
	@Caching(put={
			@CachePut(value="user-permission.user-ids", key="#userData.userId"),
			@CachePut(value="user-permission.author-ids", key="#userData.authorId")
	})
	//@formatter:on
	public UserPermissionData updateUserPermissionData(UserPermissionData userPermissionData) {
		return super.update(userPermissionData).catch_(e -> LOGGER.log(Level.SEVERE, "Error while update data: " + userPermissionData, e)).run();
	}

	/*
	 * ========== DELETE ==========
	 */

	public UserPermissionData deleteById(UserPermissionData upd) {
		return super.delete(upd).catch_(e -> LOGGER.log(Level.SEVERE, "Error while deleting data: ", e)).run();
	}

	public NextTask<Void, Integer> deleteByUserId(int userId) {
		return super.query(UserPermissionData.byUserId(userId)).thenApply(l -> {
			for (UserPermissionData upd : l) {
				super.delete(upd).run();
			}
			return l.size();
		});
	}

}
