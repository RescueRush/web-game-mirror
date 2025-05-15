package lu.rescue_rush.game_backend.db.data.monitor.traffic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLEntry.SafeSQLEntry;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

public class TrafficData implements SafeSQLEntry {

	private BigInteger id;
	private Timestamp datetime;
	private String source;
	private String referer, userAgent, domain, path, syntheticReferer;

	public TrafficData() {
	}

	public TrafficData(Timestamp datetime, String source, String referer, String userAgent, String domain, String path, String syntheticReferer) {
		this.datetime = datetime;
		this.source = source;
		this.referer = referer;
		this.userAgent = userAgent;
		this.domain = domain;
		this.path = path;
		this.syntheticReferer = syntheticReferer;
	}

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void setId(BigInteger id) {
		this.id = id;
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		this.id = rs.getBigDecimal("id").toBigInteger();
		this.datetime = rs.getTimestamp("datetime");
		this.source = rs.getString("source");
		this.referer = rs.getString("referer");
		this.userAgent = rs.getString("user_agent");
		this.domain = rs.getString("domain");
		this.path = rs.getString("path");
		this.syntheticReferer = rs.getString("synthetic_referer");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return "INSERT INTO " + table.getQualifiedName() + " (`datetime`, `source`, `referer`, `user_agent`, `domain`, `path`, `synthetic_referer`) VALUES (?, INET6_ATON(?), ?, ?, ?, ?, ?)";
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return null;
	}

	@Override
	public <T extends SQLEntry> String getPreparedDeleteSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeDelete(table, new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return "SELECT `datetime`, INET6_NTOA(`source`) AS `source`, `referer`, `user_agent`, `domain`, `path`, `synthetic_referer` FROM " + table.getQualifiedName() + " WHERE `id` = ?";
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {
		stmt.setTimestamp(1, datetime);
		stmt.setString(2, source);
		stmt.setString(3, referer);
		stmt.setString(4, userAgent);
		stmt.setString(5, domain);
		stmt.setString(6, path);
		stmt.setString(7, syntheticReferer);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {

	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setBigDecimal(1, new BigDecimal(id));
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setBigDecimal(1, new BigDecimal(id));
	}

	@Override
	public TrafficData clone() {
		return new TrafficData();
	}

	public BigInteger getId() {
		return id;
	}

	public Timestamp getDatetime() {
		return datetime;
	}

	public String getSource() {
		return source;
	}

	public String getReferer() {
		return referer;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getDomain() {
		return domain;
	}

	public String getPath() {
		return path;
	}

	public String getSyntheticReferer() {
		return syntheticReferer;
	}

	@Override
	public String toString() {
		return "TrafficData [id=" + id + ", datetime=" + datetime + ", source=" + source + ", referer=" + referer + ", userAgent=" + userAgent + ", domain=" + domain + ", path=" + path
				+ ", syntheticReferer=" + syntheticReferer + "]";
	}

}
