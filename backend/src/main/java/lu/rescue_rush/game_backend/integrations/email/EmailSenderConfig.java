package lu.rescue_rush.game_backend.integrations.email;

import lu.pcy113.pclib.config.ConfigLoader.ConfigContainer;
import lu.pcy113.pclib.config.ConfigLoader.ConfigProp;

public class EmailSenderConfig implements ConfigContainer {

	@ConfigProp("host")
	public String host;

	@ConfigProp("port")
	public int port;

	@ConfigProp("from")
	public String from;

	@ConfigProp("username")
	public String username;

	@ConfigProp("password")
	public String password;

	@ConfigProp("protocol")
	public String protocol;

	@ConfigProp("properties.mail.smtp.auth")
	public boolean properties_mail_smtp_auth;

	@ConfigProp("properties.mail.smtp.starttls.enable")
	public boolean properties_mail_smtp_starttls_enable;

	public EmailSenderConfig() {
	}

	public EmailSenderConfig(String host, int port, String from, String username, String password, String protocol, boolean properties_mail_smtp_auth, boolean properties_mail_smtp_starttls_enable) {
		this.host = host;
		this.port = port;
		this.from = from;
		this.username = username;
		this.password = password;
		this.protocol = protocol;
		this.properties_mail_smtp_auth = properties_mail_smtp_auth;
		this.properties_mail_smtp_starttls_enable = properties_mail_smtp_starttls_enable;
	}

	@Override
	public String toString() {
		return "EmailSenderConfig [host=" + host + ", port=" + port + ", from=" + from + ", username=" + username + ", password=" + password + ", protocol=" + protocol + ", properties_mail_smtp_auth="
				+ properties_mail_smtp_auth + ", properties_mail_smtp_starttls_enable=" + properties_mail_smtp_starttls_enable + "]";
	}

}
