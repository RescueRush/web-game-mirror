package lu.rescue_rush.game_backend.types;

public class NewsletterTypes {

	public static class SubscribeRequest {

		public String email, lang, source = "api";

		public SubscribeRequest() {
		}

		public SubscribeRequest(String email) {
			this.email = email;
		}

		public SubscribeRequest(String email, String lang) {
			this.email = email;
			this.lang = lang;
		}

		public SubscribeRequest(String email, String lang, String source) {
			this.email = email;
			this.lang = lang;
			this.source = source;
		}

	}

	public static class UnsubscribeRequest {

		public String email;

		public UnsubscribeRequest() {
		}

		public UnsubscribeRequest(String email) {
			this.email = email;
		}

	}

}