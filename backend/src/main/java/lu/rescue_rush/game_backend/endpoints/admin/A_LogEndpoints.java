package lu.rescue_rush.game_backend.endpoints.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lu.pcy113.pclib.db.impl.SQLQuery;

import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.db.data.monitor.traffic.AggregateTrafficData;
import lu.rescue_rush.game_backend.db.data.user.UserPermissionTypeData;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPer10Minutes_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerHour_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerMinute_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopRefererPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopRefererPerHour_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopSyntheticRefererPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopSyntheticRefererPerHour_View;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.sb.component.annotation.NeedsPermission;
import lu.rescue_rush.game_backend.types.admin.A_LogTypes.TrafficRequest;
import lu.rescue_rush.game_backend.types.admin.A_LogTypes.TrafficResponse;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@CrossOrigin
@RestController
@RequestMapping(value = "/admin/events")
public class A_LogEndpoints {

	@Autowired
	private TrafficMonitor_SummaryPerDay_View HOMEPAGE_REQUESTS_PER_DAY;

	@Autowired
	private TrafficMonitor_SummaryPerHour_View HOMEPAGE_REQUESTS_PER_HOUR;

	@Autowired
	private TrafficMonitor_SummaryPer10Minutes_View HOMEPAGE_REQUESTS_PER_10_MINUTES;

	@Autowired
	private TrafficMonitor_SummaryPerMinute_View HOMEPAGE_REQUESTS_PER_MINUTE;

	@Autowired
	private TrafficMonitor_TopRefererPerDay_View TOP_REFERER_PER_DAY;

	@Autowired
	private TrafficMonitor_TopSyntheticRefererPerDay_View TOP_SYNTHETIC_REFERER_PER_DAY;

	@Autowired
	private TrafficMonitor_TopRefererPerHour_View TOP_REFERER_PER_HOUR;

	@Autowired
	private TrafficMonitor_TopSyntheticRefererPerHour_View TOP_SYNTHETIC_REFERER_PER_HOUR;

	@ExecutionTrack
	@NeedsPermission(UserPermissionTypeData.KEY_TRAFFIC_LOG_QUERY)
	@PostMapping(value = "/logs/traffic", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> remoteLogs(@RequestBody TrafficRequest request, HttpServletRequest request2) {
		SpringUtils.badRequest(request.from == null || request.to == null || request.filter == null, "Parameters not valid.");

		final SQLQuery<AggregateTrafficData> query = AggregateTrafficData.byTimestamp(request.from, request.to);

		//@formatter:off
		final List<AggregateTrafficData> list = 
				(switch (request.filter) {
					case "day" -> HOMEPAGE_REQUESTS_PER_DAY;
					case "hour" -> HOMEPAGE_REQUESTS_PER_HOUR;
					case "minute" -> HOMEPAGE_REQUESTS_PER_MINUTE;
					case "10minutes" -> HOMEPAGE_REQUESTS_PER_10_MINUTES;
					case "referer_day" -> TOP_REFERER_PER_DAY;
					case "referer_hour" -> TOP_REFERER_PER_HOUR;
					case "referer_synthetic_day" -> TOP_SYNTHETIC_REFERER_PER_DAY;
					case "referer_synthetic_hour" -> TOP_SYNTHETIC_REFERER_PER_HOUR;
					default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected value: " + request.filter);
				})
				.query(query).run();
		//@formatter:on

		return ResponseEntity.ok(new TrafficResponse(list.stream().map(c -> new TrafficResponse.TrafficEntryReponse(c)).collect(Collectors.toList())));
	}
}
