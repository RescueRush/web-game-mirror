package lu.rescue_rush.game_backend.db.data.monitor.interaction;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@GeneratedKey("id")
public class UserPassResetData implements SafeSQLEntry {

	private int id, userId;
	private String token;
	private Timestamp expiresAt;
	private boolean tokenUsed = true;

	private String clientToken;
	private UserData userData;

	public UserPassResetData() {
	}

	public UserPassResetData(int id, int userId, String token, Timestamp expiresAt, boolean token_used) {
		this.id = id;
		this.userId = userId;
		this.token = token;
		this.expiresAt = expiresAt;
		this.tokenUsed = token_used;
	}

	public UserPassResetData(int userId, String token, Timestamp expiresAt, boolean token_used) {
		this.userId = userId;
		this.token = token;
		this.expiresAt = expiresAt;
		this.tokenUsed = token_used;
	}

	public UserPassResetData(int id) {
		this.id = id;
	}

	public UserPassResetData(UserData ud) {
		userId = ud.getId();
		userData = ud;
	}

	public String genResetToken() {
		String clientToken = UserData.hashPass(userData.getName() + String.valueOf(System.nanoTime()));
		this.token = UserData.hashPass(clientToken);
		tokenUsed = false;
		expiresAt = new Timestamp(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES));
		return this.clientToken = clientToken;
	}

	public boolean isTokenValid(String clientToken) {
		if (tokenUsed || expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
			return false;
		}
		return UserData.hashPass(clientToken).equals(token);
	}

	public UserPassResetData load() {
		return TableProxy.USER_PASS_RESET.query(byId(id)).thenApply(SpringUtils.first(() -> null)).run();
	}

	public UserData loadUser() {
		return userData = TableProxy.USER.byId(userId);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		userId = rs.getInt("user_id");
		token = rs.getString("token");
		expiresAt = rs.getTimestamp("expires_at");
		tokenUsed = rs.getBoolean("token_used");
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void keyUpdate(BigInteger id) throws SQLException {
		this.id = id.intValue();
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "token", "expires_at", "token_used" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "user_id", "token", "expires_at", "token_used" }, new String[] { "id" });
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
		stmt.setString(2, token);
		stmt.setTimestamp(3, expiresAt);
		stmt.setBoolean(4, tokenUsed);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
		stmt.setString(2, token);
		stmt.setTimestamp(3, expiresAt);
		stmt.setBoolean(4, tokenUsed);

		stmt.setInt(5, id);
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
	public UserPassResetData clone() {
		return new UserPassResetData();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public UserData getUserData() {
		return userData;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Timestamp getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Timestamp expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isToken_used() {
		return tokenUsed;
	}

	public void setToken_used(boolean token_used) {
		this.tokenUsed = token_used;
	}

	public String getClientToken() {
		return clientToken;
	}

	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	@Override
	public String toString() {
		return "UserPassResetData [id=" + id + ", userId=" + userId + ", token=" + token + ", expiresAt=" + expiresAt + ", tokenUsed=" + tokenUsed + ", userdata=" + userData + ", clientToken="
				+ clientToken + "]";
	}

	public static SafeSQLQuery<UserPassResetData> byToken(String token) {
		return new SafeSQLQuery<UserPassResetData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPassResetData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "token" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				String hashed = UserData.hashPass(token);
				stmt.setString(1, hashed);
			}

			@Override
			public UserPassResetData clone() {
				return new UserPassResetData();
			}
		};
	}

	public static SafeSQLQuery<UserPassResetData> byUser(int user_id) {
		return new SafeSQLQuery<UserPassResetData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPassResetData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "user_id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, user_id);
			}

			@Override
			public UserPassResetData clone() {
				return new UserPassResetData();
			}
		};
	}

	public static SafeSQLQuery<UserPassResetData> byId(int id) {
		return new SafeSQLQuery<UserPassResetData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<UserPassResetData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public UserPassResetData clone() {
				return new UserPassResetData();
			}
		};
	}

	public static SafeSQLQuery<UserPassResetData> byUser(UserData ud) {
		return byUser(ud.getId());
	}
}
