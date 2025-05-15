package lu.rescue_rush.game_backend.types;

public final class SupportTypes {

	public static class EmailFormResponse {

		public int ticketId;

		public EmailFormResponse(int ticketId) {
			this.ticketId = ticketId;
		}

		public int getTicketId() {
			return ticketId;
		}

	}

	public static class EmailFormRequest {

		public String name, email, message, lang;

		public EmailFormRequest(String name, String email, String message, String lang) {
			this.name = name;
			this.email = email;
			this.message = message;
			this.lang = lang;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

	}

}
