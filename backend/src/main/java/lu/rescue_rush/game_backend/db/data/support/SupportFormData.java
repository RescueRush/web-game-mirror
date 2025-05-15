package lu.rescue_rush.game_backend.db.data.support;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.springframework.http.HttpStatus;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.data.Locales;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

@GeneratedKey("id")
public class SupportFormData implements SafeSQLEntry {

	private int id, userId;
	private String name, email, message, source, lang;
	private Timestamp submitDate;
	private boolean discordConfirmed = false, emailConfirmed = false;

	private UserData userData;

	public SupportFormData() {
	}

	public SupportFormData(String name, String email, String message, String lang) {
		this.userId = -1;
		this.name = name;
		this.email = email;
		this.message = message;
		this.lang = lang;
	}

	public SupportFormData(int userId, String name, String email, String message, String lang) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.message = message;
		this.lang = lang;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void setId(BigInteger id) {
		this.id = id.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.userId = rs.getInt("user_id");
		if (rs.wasNull()) {
			this.userId = -1;
		}
		this.name = rs.getString("name");
		this.email = rs.getString("email");
		this.message = rs.getString("message");
		this.lang = rs.getString("lang");
		this.submitDate = rs.getTimestamp("submit_date");
		this.discordConfirmed = rs.getBoolean("discord_confirmed");
		this.emailConfirmed = rs.getBoolean("email_confirmed");
	}

	/**
	 * @throws {@link HttpStatus#INTERNAL_SERVER_ERROR} Email is too long. (>320
	 *                chars)
	 * @throws {@link HttpStatus#INTERNAL_SERVER_ERROR} Name is too long. (>200
	 *                chars)
	 * @throws {@link HttpStatus#INTERNAL_SERVER_ERROR} Message is too long.
	 *                (>10_000 chars)
	 */
	@RequestSafe
	public SupportFormData validateData() {
		SpringUtils.internalServerError(!PCUtils.validEmail(email), "Email is not an email.");
		SpringUtils.internalServerError(email.length() > 320, "Email is too long.");
		SpringUtils.internalServerError(name.length() > 200, "Name is too long.");
		SpringUtils.internalServerError(message.length() > 10_000, "Message is too long.");
		SpringUtils.internalServerError(Locales.byCode(lang) == null, "Unsupported language");

		return this;
	}

	public void loadUserData() {
		if (userId != -1) {
			userData = TableProxy.USER.byId(userId);
		}
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "user_id", "name", "message", "email", "lang" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "user_id", "name", "message", "lang", "discord_confirmed", "email_confirmed" }, new String[] { "id" });
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
		if (userId == -1) {
			stmt.setNull(1, Types.INTEGER);
		}
		stmt.setString(2, name);
		stmt.setString(3, message);
		stmt.setString(4, email);
		stmt.setString(5, lang);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, userId);
		if (userId == -1) {
			stmt.setNull(1, Types.INTEGER);
		}
		stmt.setString(2, name);
		stmt.setString(3, message);
		stmt.setString(4, lang);
		stmt.setBoolean(5, discordConfirmed);
		stmt.setBoolean(6, emailConfirmed);

		stmt.setInt(7, id);
	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	public static SafeSQLQuery<SupportFormData> byId(int id) {
		return new SQLQuery.SafeSQLQuery<SupportFormData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<SupportFormData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "id" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
			}

			@Override
			public SupportFormData clone() {
				return new SupportFormData();
			}

		};
	}

	public static SafeSQLQuery<SupportFormData> byEmail(String email) {
		return new SQLQuery.SafeSQLQuery<SupportFormData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<SupportFormData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "email" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, email);
			}

			@Override
			public SupportFormData clone() {
				return new SupportFormData();
			}

		};
	}

	public SupportFormData clone() {
		return new SupportFormData();

	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Timestamp getSubmitDate() {
		return submitDate;
	}

	public int getId() {
		return id;
	}

	public UserData getUserData() {
		return userData;
	}

	public boolean isDiscordConfirmed() {
		return discordConfirmed;
	}

	public void setDiscordConfirmed(boolean discordConfirmed) {
		this.discordConfirmed = discordConfirmed;
	}

	public boolean isEmailConfirmed() {
		return emailConfirmed;
	}

	public void setEmailConfirmed(boolean emailConfirmed) {
		this.emailConfirmed = emailConfirmed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}
