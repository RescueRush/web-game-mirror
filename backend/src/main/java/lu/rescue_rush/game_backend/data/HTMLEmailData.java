package lu.rescue_rush.game_backend.data;

public class HTMLEmailData {

	private String receiver, subject, htmlDoc;

	public HTMLEmailData(String receiver, String subject, String htmlDoc) {
		this.receiver = receiver;
		this.subject = subject;
		this.htmlDoc = htmlDoc;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getHtmlDoc() {
		return htmlDoc;
	}

	public void setHtmlDoc(String htmlDoc) {
		this.htmlDoc = htmlDoc;
	}

}
