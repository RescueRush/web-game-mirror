package lu.rescue_rush.game_backend.types;

import java.sql.Timestamp;
import java.util.List;

import lu.rescue_rush.game_backend.db.data.monitor.traffic.TrafficData;

public class LogTypes {

	public static class LogRequest {

		public List<LogEntryRequest> list;

		public LogRequest() {
		}

		public LogRequest(List<LogEntryRequest> list) {
			this.list = list;
		}

		public List<LogEntryRequest> getList() {
			return list;
		}

		@Override
		public String toString() {
			return "LogRequest [list=" + list + "]";
		}

		public static class LogEntryRequest {

			public Timestamp datetime;
			public String source;
			public String referer, userAgent, domain, path, syntheticReferer;

			public LogEntryRequest() {
			}

			public LogEntryRequest(Timestamp datetime, String source, String referer, String userAgent, String domain, String path, String syntheticReferer) {
				this.datetime = datetime;
				this.source = source;
				this.referer = referer;
				this.userAgent = userAgent;
				this.domain = domain;
				this.path = path;
				this.syntheticReferer = syntheticReferer;
			}

			public TrafficData toTrafficData() {
				return new TrafficData(datetime, source, referer, userAgent, domain, path, syntheticReferer);
			}

			public void loadTrafficData(TrafficData tf) {
				this.datetime = tf.getDatetime();
				this.source = tf.getSource();
				this.referer = tf.getReferer();
				this.userAgent = tf.getUserAgent();
				this.domain = tf.getDomain();
				this.path = tf.getPath();
				this.syntheticReferer = tf.getSyntheticReferer();
			}

			public static LogEntryRequest fromTrafficData(TrafficData tf) {
				return new LogEntryRequest(tf.getDatetime(), tf.getSource(), tf.getReferer(), tf.getUserAgent(), tf.getDomain(), tf.getPath(), tf.getSyntheticReferer());
			}

			@Override
			public String toString() {
				return "LogEntryRequest [datetime=" + datetime + ", source=" + source + ", referer=" + referer + ", userAgent=" + userAgent + ", domain=" + domain + ", path=" + path
						+ ", syntheticReferer=" + syntheticReferer + "]";
			}

		}

	}

}
