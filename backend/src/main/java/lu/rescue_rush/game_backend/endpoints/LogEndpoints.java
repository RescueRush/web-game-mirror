package lu.rescue_rush.game_backend.endpoints;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.db.data.monitor.traffic.TrafficData;
import lu.rescue_rush.game_backend.db.tables.monitor.traffic.TrafficMonitorTable;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.types.LogTypes.LogRequest;
import lu.rescue_rush.game_backend.types.LogTypes.LogRequest.LogEntryRequest;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@CrossOrigin
@RestController
@RequestMapping(value = "/logs")
public class LogEndpoints {

	private static final Logger LOGGER = Logger.getLogger(LogEndpoints.class.getName());

	@Autowired
	private TrafficMonitorTable HOMEPAGE_TRAFFIC;

	@Value("${remote.host}")
	private String remoteHost;

	private String allowedIp;

	@PostConstruct
	public void init() throws UnknownHostException {
		allowedIp = InetAddress.getByName(remoteHost).getHostAddress();
	}

	/**
	 * Saves the {@link LogRequest#LogEntryRequest} as {@link TrafficData} into
	 * {@link TrafficTable}.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#FORBIDDEN} Invalid remote ip.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED}
	 */

	@ExecutionTrack
	@PostMapping(value = "hp", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> hp(@RequestBody LogRequest data, HttpServletRequest request) {
		final String ip = request.getRemoteAddr();
		if (!ip.equals(allowedIp)) {
			LOGGER.warning("Invalid remote ip: '" + ip + "', expecting: '" + allowedIp + "'");
			InteractionMonitor.pushDeniedDesc("Invalid remote ip: '" + ip + "'");
			SpringUtils.forbidden(true, "Invalid remote ip.");
			return null; // never reached
		}

		InteractionMonitor.pushAcceptedDesc(Integer.toString(data.getList().size()));

		HOMEPAGE_TRAFFIC.bulkInsert(data.getList().parallelStream().map(LogEntryRequest::toTrafficData).collect(Collectors.toList()))
				.catch_(SpringUtils.catch_(LOGGER, e -> "Couldn't save traffic data: " + e.getMessage())).runAsync();

		return ResponseEntity.accepted().build();
	}

}
