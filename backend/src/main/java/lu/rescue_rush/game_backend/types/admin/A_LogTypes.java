package lu.rescue_rush.game_backend.types.admin;

import java.sql.Timestamp;
import java.util.List;

import lu.rescue_rush.game_backend.db.data.monitor.traffic.AggregateTrafficData;

public final class A_LogTypes {

	public static class TrafficRequest {

		public Timestamp from;
		public Timestamp to;
		public String filter;

		public TrafficRequest() {
		}

		public TrafficRequest(Timestamp from, Timestamp to, String filter) {
			this.from = from;
			this.to = to;
			this.filter = filter;
		}

		public TrafficRequest(Timestamp from, Timestamp to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public String toString() {
			return "EventRequest [from=" + from + ", to=" + to + ", filter=" + filter + "]";
		}

	}

	public static class TrafficResponse {

		public List<TrafficEntryReponse> entries;

		public TrafficResponse() {
		}

		public TrafficResponse(List<TrafficEntryReponse> entries) {
			this.entries = entries;
		}

		public static class TrafficEntryReponse {

			public Timestamp timestamp;
			public String domain;
			public int count;

			public TrafficEntryReponse() {
			}

			public TrafficEntryReponse(Timestamp timestamp, String domain, int count) {
				this.timestamp = timestamp;
				this.domain = domain;
				this.count = count;
			}

			public TrafficEntryReponse(AggregateTrafficData data) {
				this.timestamp = data.getTime();
				this.domain = data.getDomainPath();
				this.count = data.getCount();
			}

			@Override
			public String toString() {
				return "EventEntryIntResponse [timestamp=" + timestamp + ", domain=" + domain + ", count=" + count + "]";
			}

		}

	}

}
