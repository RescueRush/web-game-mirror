package lu.rescue_rush.game_backend.integrations.discord;

import lu.pcy113.pclib.config.ConfigLoader.ConfigContainer;
import lu.pcy113.pclib.config.ConfigLoader.ConfigProp;

public class DiscordWebhookConfig implements ConfigContainer {

	@ConfigProp("username")
	public String username;

	@ConfigProp("author/name")
	public String authorName;

	@ConfigProp("author/icon")
	public String authorIcon;

	@ConfigProp("author/url")
	public String authorUrl;

	@ConfigProp("url")
	public String url;

	public DiscordWebhookConfig() {
	}

	public DiscordWebhookConfig(String username, String authorName, String authorIcon, String authorUrl, String url) {
		this.username = username;
		this.authorName = authorName;
		this.authorIcon = authorIcon;
		this.authorUrl = authorUrl;
		this.url = url;
	}

	@Override
	public String toString() {
		return "DiscordSenderConfig [username=" + username + ", authorName=" + authorName + ", authorIcon=" + authorIcon + ", authorUrl=" + authorUrl + ", url=" + url + "]";
	}

}
