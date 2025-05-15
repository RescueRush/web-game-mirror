package lu.rescue_rush.game_backend.integrations.discord.messages;

public class MessageHello implements DiscordMessage {

	private String userMention;

	public MessageHello(String userMention) {
		this.userMention = userMention;
	}

	@Override
	public String body() {
		return "Hello hello, " + userMention + ", running in here !!";
	}

}
