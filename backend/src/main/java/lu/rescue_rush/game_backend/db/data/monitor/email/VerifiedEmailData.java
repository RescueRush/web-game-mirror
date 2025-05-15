package lu.rescue_rush.game_backend.db.data.monitor.email;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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

import lu.rescue_rush.game_backend.utils.SpringUtils;

@GeneratedKey("id")
public class VerifiedEmailData implements SafeSQLEntry {

	private int id;
	private String hash;
	private boolean verified = false;
	private Timestamp firstTime;

	public VerifiedEmailData() {
	}

	public VerifiedEmailData(int id, String email, boolean verified, Timestamp firstTime) {
		this.id = id;
		this.hash = email;
		this.verified = verified;
		this.firstTime = firstTime;
	}

	public VerifiedEmailData(String email, boolean verified) {
		this.hash = SpringUtils.hash(email);
		this.verified = verified;
	}

	public VerifiedEmailData(String email) {
		this.hash = SpringUtils.hash(email);
	}

	public static VerifiedEmailData ofHash(String hash) {
		VerifiedEmailData ved = new VerifiedEmailData();
		ved.setHash(hash);
		return ved;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void setId(BigInteger id) {
		this.id = id.intValue();
	}

	@Reload
	public void load(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.hash = rs.getString("hash");
		this.verified = rs.getBoolean("verified");
		this.firstTime = rs.getTimestamp("first_time");

	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "hash", "verified" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "hash", "verified" }, new String[] { "id" });
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
		stmt.setString(1, hash);
		stmt.setBoolean(2, verified);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, hash);
		stmt.setBoolean(2, verified);

		stmt.setInt(3, id);
	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	public int getId() {
		return id;
	}

	@UniqueKey("hash")
	public String getHash() {
		return hash;
	}

	public boolean isVerified() {
		return verified;
	}

	public Timestamp getFirstTime() {
		return firstTime;
	}

	public void setHash(String email) {
		this.hash = email;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	@Override
	public VerifiedEmailData clone() {
		return new VerifiedEmailData();
	}

	public static SQLQuery<VerifiedEmailData> byEmail(String email) {
		return new SafeSQLQuery<VerifiedEmailData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<VerifiedEmailData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "hash" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, SpringUtils.hash(email));
			}

			@Override
			public VerifiedEmailData clone() {
				return new VerifiedEmailData();
			}

		};
	}

	public static SQLQuery<VerifiedEmailData> byHash(String hash) {
		return new SafeSQLQuery<VerifiedEmailData>() {

			@Override
			public String getPreparedQuerySQL(SQLQueryable<VerifiedEmailData> table) {
				return SQLBuilder.safeSelect(table, new String[] { "hash" });
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, hash);
			}

			@Override
			public VerifiedEmailData clone() {
				return new VerifiedEmailData();
			}

		};
	}

}
