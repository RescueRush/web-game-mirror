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
public class UserSanctionData implements SafeSQLEntry {

	private int id, userId, reasonId, authorId, cancellerId = -1;
	private String description;
	private Timestamp issueDate, cancelDate;

	private UserSanctionReasonData reasonData;

	public UserSanctionData() {
	}

	public UserSanctionData(int user_id, int reason_id, String description, int author_id, int canceller_id) {
		this.userId = user_id;
		this.reasonId = reason_id;
		this.description = description;
		this.authorId = author_id;
		this.cancellerId = canceller_id;
		loadReasonData();
	}

	public UserSanctionData(int user_id, UserSanctionReasonData reason, String description, int author_id, int canceller_id) {
		this.userId = user_id;
		this.reasonId = reason.getId();
		this.description = description;
		this.authorId = author_id;
		this.reasonData = reason;
		this.cancellerId = canceller_id;
	}

	public UserSanctionData(UserData user, UserSanctionReasonData reason, UserData author, String description) {
		this.userId = user.getId();
		this.reasonId = reason.getId();
		this.description = description;
		this.authorId = author.getId();
		this.reasonData = reason;
	}

	@GeneratedKeyUpdate
	public void updateKeys(ResultSet rs) throws SQLException {
		id = rs.getInt(1);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		userId = rs.getInt("user_id");
		reasonId = rs.getInt("reason_id");
		authorId = rs.getInt("author_id");
		cancellerId = rs.getInt("canceller_id");
		cancellerId = rs.wasNull() ? -1 : cancellerId;
		description = rs.getString("description");
		issueDate = rs.getTimestamp("issue_date");
		cancelDate = rs.getTimestamp("cancel_date");

		loadReasonData();
	}

	public void push() {
		TableProxy.USER_SANCTION.updateUserSanctionData(this);
	}

	/**
	 * You're not supposed to do this
	 */
	@Deprecated
	public UserSanctionData pushReason() {
		this.reasonData.push();
		return this;
	}

	public UserSanctionReasonData loadReasonData() {
		return reasonData = TableProxy.USER_SANCTION_REASON.byId(reasonId);
	}

	public UserSanctionReasonData getReasonData() {
		if (reasonData == null) {
			return loadReasonData();
		}
		return reasonData;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getReasonId() {
		return reasonId;
	}

	public int getAuthorId() {
		return authorId;
	}

	public int getCancellerId() {
		return cancellerId;
	}

	public String getDescription() {
		return description;
	}

	public Timestamp getIssueDate() {
		return issueDate;
	}

	public Timestamp getCancelDate() {
		return cancelDate;
	}

	public boolean isCancelled() {
		return cancelDate != null;
	}

	public void setCancellerId(int cancellerId) {
		this.cancellerId = cancellerId;
	}

	public void setCancelDate(Timestamp cancelDate) {
		this.cancelDate = cancelDate;
	}

	public void setCanceller(UserData user) {
		if (user == null)
			this.cancellerId = -1;
		else
			this.cancellerId = user.getId();
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "reason_id", "description", "author_id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "user_id", "reason_id", "description", "canceller_id", "cancel_date" }, new String[] { "id" });
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
		stmt.setInt(2, reasonId);
		stmt.setString(3, description);
		stmt.setInt(4, authorId);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
		stmt.setInt(2, reasonId);
		stmt.setString(3, description);
		if (cancellerId == -1) {
			stmt.setNull(4, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(4, cancellerId);
		}
		stmt.setTimestamp(5, cancelDate);
		stmt.setInt(6, id);
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
	public UserSanctionData clone() {
		return new UserSanctionData();
	}

	@Override
	public String toString() {
		return "UserSanctionData [id=" + id + ", userId=" + userId + ", reasonId=" + reasonId + ", authorId=" + authorId + ", cancellerId=" + cancellerId + ", description=" + description
				+ ", issueDate=" + issueDate + ", cancelDate=" + cancelDate + ", reasonData=" + reasonData + "]";
	}

	public static SQLQuery<UserSanctionData> byId(int id) {
		return new SQLQuery.SafeSQLQuery<UserSanctionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public UserSanctionData clone() {
				return new UserSanctionData();
			}

		};
	}

	public static SQLQuery<UserSanctionData> byUserId(int userId) {
		return new SQLQuery.SafeSQLQuery<UserSanctionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "user_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, userId);
			}

			@Override
			public UserSanctionData clone() {
				return new UserSanctionData();
			}

		};
	}

	public static SQLQuery<UserSanctionData> byAuthorId(int authorId) {
		return new SQLQuery.SafeSQLQuery<UserSanctionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "author_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, authorId);
			}

			@Override
			public UserSanctionData clone() {
				return new UserSanctionData();
			}

		};
	}

	public static SQLQuery<UserSanctionData> byUserToken(String token) {
		return new SQLQuery.SafeSQLQuery<UserSanctionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserSanctionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `user_id`=(SELECT `id` FROM " + TableProxy.USER.getQualifiedName() + " WHERE `token`=?);";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, token);
			}

			@Override
			public UserSanctionData clone() {
				return new UserSanctionData();
			}

		};
	}
}
