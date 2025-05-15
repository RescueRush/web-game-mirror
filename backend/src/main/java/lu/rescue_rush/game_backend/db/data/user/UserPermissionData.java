package lu.rescue_rush.game_backend.db.data.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.db.TableProxy;

@GeneratedKey("id")
public class UserPermissionData implements SafeSQLEntry {

	private int id, userId, typeId, authorId;
	private String description;
	private Timestamp issueDate;

	private UserPermissionTypeData typeData;

	public UserPermissionData() {
	}

	public UserPermissionData(int user_id, int type_id, String description, int author_id) {
		this.userId = user_id;
		this.typeId = type_id;
		this.description = description;
		this.authorId = author_id;
		loadTypeData();
	}

	public UserPermissionData(int user_id, UserPermissionTypeData type, String description, int author_id) {
		this.userId = user_id;
		this.typeId = type.getId();
		this.description = description;
		this.authorId = author_id;
		typeData = type;
	}

	public UserPermissionData(UserData user, UserPermissionTypeData type, UserData author, String description) {
		this.userId = user.getId();
		this.typeId = type.getId();
		this.description = description;
		this.authorId = author.getId();
		typeData = type;
	}

	@GeneratedKeyUpdate
	public void updateKeys(ResultSet rs) throws SQLException {
		id = rs.getInt(1);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		userId = rs.getInt("user_id");
		typeId = rs.getInt("type_id");
		authorId = rs.getInt("author_id");
		description = rs.getString("description");
		issueDate = rs.getTimestamp("issue_date");
		loadTypeData();
	}

	public void push() {
		TableProxy.USER_PERMISSION.updateUserPermissionData(this);
	}

	/**
	 * You're not supposed to do this
	 */
	@Deprecated
	public UserPermissionData pushType() {
		this.typeData.push();
		return this;
	}

	public UserPermissionTypeData loadTypeData() {
		return typeData = TableProxy.USER_PERMISSION_TYPE.byId(typeId);
	}

	public UserPermissionTypeData getTypeData() {
		return typeData;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getTypeId() {
		return typeId;
	}

	public int getAuthorId() {
		return authorId;
	}

	public String getDescription() {
		return description;
	}

	public Timestamp getIssueDate() {
		return issueDate;
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "type_id", "description", "author_id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "user_id", "type_id", "description" }, new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedDeleteSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeDelete(table, new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return SQLBuilder.safeSelect(table, new String[] { "id" });
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
		stmt.setInt(2, typeId);
		stmt.setString(3, description);
		stmt.setInt(4, authorId);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
		stmt.setInt(2, typeId);
		stmt.setString(3, description);
		stmt.setInt(4, id);
	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public UserPermissionData clone() {
		return new UserPermissionData();
	}

	@Override
	public String toString() {
		return "UserSanctionData {id=" + id + ", userId=" + userId + ", typeId=" + typeId + ", authorId=" + authorId + ", description=" + description + ", issueDate=" + issueDate + ", typeData="
				+ typeData + "}";
	}

	public static SQLQuery<UserPermissionData> byId(int id) {
		return new SQLQuery.SafeSQLQuery<UserPermissionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public UserPermissionData clone() {
				return new UserPermissionData();
			}

		};
	}

	public static SQLQuery<UserPermissionData> byUserId(int userId) {
		return new SQLQuery.SafeSQLQuery<UserPermissionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "user_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
			}

			@Override
			public UserPermissionData clone() {
				return new UserPermissionData();
			}

		};
	}

	public static SQLQuery<UserPermissionData> byAuthorId(int authorId) {
		return new SQLQuery.SafeSQLQuery<UserPermissionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "author_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, authorId);
			}

			@Override
			public UserPermissionData clone() {
				return new UserPermissionData();
			}

		};
	}

	public static SQLQuery<UserPermissionData> byUserToken(String token) {
		return new SQLQuery.SafeSQLQuery<UserPermissionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPermissionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `user_id`=(SELECT `id` FROM " + TableProxy.USER.getQualifiedName() + " WHERE `token`=?);";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, token);
			}

			@Override
			public UserPermissionData clone() {
				return new UserPermissionData();
			}

		};
	}
}
