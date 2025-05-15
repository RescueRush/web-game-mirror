package lu.rescue_rush.game_backend.db.data.newsletter;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.annotations.entry.UniqueKey;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQuery.SafeSQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.data.Locales;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;

@GeneratedKey("id")
public class NewsletterSubscriptionData implements SafeSQLEntry {

	private int id;
	private String email, lang, hash;
	private Timestamp since, left;
	private String source;

	private UserData userData;

	public NewsletterSubscriptionData() {
	}

	public NewsletterSubscriptionData(String email, String source) {
		this.email = email;
		this.source = source;
		this.hash = hash(System.currentTimeMillis() + email);
	}

	public NewsletterSubscriptionData(String email, String source, String language) {
		this.email = email;
		this.source = source;
		this.lang = language.toLowerCase();
		this.hash = hash(System.currentTimeMillis() + email);
	}

	public void loadUserData() {
		if (TableProxy.USER.emailExists(email)) {
			this.userData = TableProxy.USER.byEmail(email);
		}
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateGeneratedKeys(BigInteger inte) throws SQLException {
		id = inte.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		email = rs.getString("email");
		lang = rs.getString("lang");
		hash = rs.getString("hash");
		since = rs.getTimestamp("since");
		source = rs.getString("source");
		left = rs.getTimestamp("left");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "email", "lang", "hash", "source", "left" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "email", "lang", "source", "left" }, new String[] { "id" });
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
		stmt.setString(1, email);
		stmt.setString(2, lang);
		stmt.setString(3, hash);
		stmt.setString(4, source);
		stmt.setTimestamp(5, left);
	}

	public static String hash(String email) {
		return PCUtils.hashString(email + System.currentTimeMillis(), "sha-256");
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, email);
		stmt.setString(2, lang);
		stmt.setString(3, source);
		stmt.setTimestamp(4, left);

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

	@UniqueKey("hash")
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getId() {
		return id;
	}

	public Timestamp getSince() {
		return since;
	}

	@UniqueKey("email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Timestamp getLeft() {
		return left;
	}

	public void setLeft(Timestamp left) {
		this.left = left;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String language) {
		this.lang = language.toLowerCase();
	}

	public Language getLanguage() {
		return Language.byCode(lang);
	}

	public void setLanguage(Language language) {
		this.lang = language.getCode();
	}

	public Locale getLocale() {
		return Locales.byCode(lang);
	}

	public void setLocale(Locale locale) {
		this.lang = locale.getLanguage();
	}

	public UserData getUserData() {
		return userData;
	}

	@Override
	public String toString() {
		return "NewsletterSubscriptionData [id=" + id + ", email=" + email + ", lang=" + lang + ", hash=" + hash + ", since=" + since + ", left=" + left + ", source=" + source + ", userData="
				+ userData + "]";
	}

	@Override
	public NewsletterSubscriptionData clone() {
		return new NewsletterSubscriptionData();
	}

	public static SQLQuery<NewsletterSubscriptionData> byEmailHash(String hash) {
		return new SafeSQLQuery<NewsletterSubscriptionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<NewsletterSubscriptionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "hash" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, hash);
			}

			@Override
			public NewsletterSubscriptionData clone() {
				return new NewsletterSubscriptionData();
			}

		};
	}

	public static SQLQuery<NewsletterSubscriptionData> byEmail(String email) {
		return new SafeSQLQuery<NewsletterSubscriptionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<NewsletterSubscriptionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "email" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, email);
			}

			@Override
			public NewsletterSubscriptionData clone() {
				return new NewsletterSubscriptionData();
			}

		};
	}

	public static SQLQuery<NewsletterSubscriptionData> byLang(String language) {
		return new SafeSQLQuery<NewsletterSubscriptionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<NewsletterSubscriptionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "lang" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, language.toLowerCase());
			}

			@Override
			public NewsletterSubscriptionData clone() {
				return new NewsletterSubscriptionData();
			}

		};
	}

	public static SQLQuery<NewsletterSubscriptionData> bySource(String source) {
		return new SQLQuery.SafeSQLQuery<NewsletterSubscriptionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<NewsletterSubscriptionData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "source" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, source);
			}

			@Override
			public NewsletterSubscriptionData clone() {
				return new NewsletterSubscriptionData();
			}

		};
	}

	public static SQLQuery<NewsletterSubscriptionData> bySourceExclude(String notSource) {
		return new SQLQuery.SafeSQLQuery<NewsletterSubscriptionData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<NewsletterSubscriptionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `source` != ?;";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, notSource);
			}

			@Override
			public NewsletterSubscriptionData clone() {
				return new NewsletterSubscriptionData();
			}

		};
	}

	public static SQLQuery<NewsletterSubscriptionData> all() {
		return new SQLQuery.UnsafeSQLQuery<NewsletterSubscriptionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<NewsletterSubscriptionData> table) {
				return SQLBuilder.safeSelect(table, null);
			}

			@Override
			public NewsletterSubscriptionData clone() {
				return new NewsletterSubscriptionData();
			}
		};
	}

}
