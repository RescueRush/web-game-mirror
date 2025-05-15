package lu.rescue_rush.game_backend.db.tables.monitor.traffic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.async.NextTask;
import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.monitor.traffic.TrafficData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPer10Minutes_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerHour_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerMinute_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopRefererPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopRefererPerHour_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopSyntheticRefererPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopSyntheticRefererPerHour_View;

//@formatter:off
@DB_Table(name = "traffic_monitor", columns = {
		@Column(name = "id", type = "bigint", autoIncrement = true),
		@Column(name = "datetime", type = "timestamp"),
		@Column(name = "source", type = "varbinary(16)"),
		@Column(name = "referer", type = "text", notNull = false),
		@Column(name = "user_agent", type = "text", notNull = false),
		@Column(name = "domain", type = "varchar(64)"),
		@Column(name = "path", type = "varchar(64)"),
		@Column(name = "synthetic_referer", type = "varchar(64)", notNull = false),
		@Column(name = "domain_path", type = "varchar(128)", generated = true, generatedType = Column.GeneratedType.STORED, generator = "CONCAT(domain, path)")	,
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_traffic_monitor_id", columns = "id"),
		@Constraint(type = Constraint.Type.INDEX, name = "idx_traffic_monitor_time", columns = "datetime")
})
//@formatter:on
@Service
public class TrafficMonitorTable extends R2DBTable<TrafficData> {

	@Autowired
	@Lazy
	public TrafficMonitor_SummaryPerDay_View SUMMARY_PER_DAY;
	@Autowired
	@Lazy
	public TrafficMonitor_SummaryPerHour_View SUMMARY_PER_HOUR;
	@Autowired
	@Lazy
	public TrafficMonitor_SummaryPer10Minutes_View SUMMARY_PER_10_MINUTES;
	@Autowired
	@Lazy
	public TrafficMonitor_SummaryPerMinute_View SUMMARY_PER_MINUTE;
	@Autowired
	@Lazy
	public TrafficMonitor_TopRefererPerDay_View TOP_REFERER_PER_DAY;
	@Autowired
	@Lazy
	public TrafficMonitor_TopSyntheticRefererPerDay_View TOP_SYNTHETIC_REFERER_PER_DAY;
	@Autowired
	@Lazy
	public TrafficMonitor_TopRefererPerHour_View TOP_REFERER_PER_HOUR;
	@Autowired
	@Lazy
	public TrafficMonitor_TopSyntheticRefererPerHour_View TOP_SYNTHETIC_REFERER_PER_HOUR;

	public TrafficMonitorTable(DataBase dbTest) {
		super(dbTest);
	}

	public NextTask<Void, Integer> bulkInsert(List<TrafficData> td) {
		return NextTask.<Integer>create(() -> {
			Connection con = null;
			PreparedStatement pstmt = null;
			int result = -1;

			try {
				con = super.createConnection();
				con.setAutoCommit(false);

				if (td.size() == 0) {
					return 0;
				}

				final TrafficData builder = td.get(0);

				pstmt = con.prepareStatement(builder.getPreparedInsertSQL(super.getQueryable()));

				for (TrafficData t : td) {
					t.prepareInsertSQL(pstmt);
					pstmt.addBatch();
				}

				result = Arrays.stream(pstmt.executeBatch()).sum();

				if (td.size() != result) {
					throw new IllegalStateException("Inserted incorrect number of rows: " + result + " instead of expected " + td.size());
				}

				con.commit();
			} catch (Exception e) {
				throw e;
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			}

			return result;
		});
	}

}
