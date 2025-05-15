package lu.rescue_rush.game_backend.integrations.discord;

import java.awt.Color;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.async.NextTask;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordButtonMessage;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbed;
import lu.rescue_rush.game_backend.integrations.discord.listeners.ButtonInteractionListener;
import lu.rescue_rush.game_backend.integrations.discord.listeners.ModalInteractionListener;
import lu.rescue_rush.game_backend.integrations.discord.listeners.SlashCommandListener;
import lu.rescue_rush.game_backend.integrations.discord.messages.DiscordMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Service
public class DiscordSender {

	private static final Logger LOGGER = Logger.getLogger(DiscordSender.class.getName());

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordBotConfig config;

	@Autowired
	private ButtonInteractionListener buttonInteractionListener;
	@Autowired
	private SlashCommandListener slashCommandListener;
	@Autowired
	private ModalInteractionListener modalInteractionListener;

	private MessageChannel channelLogs;

	@PostConstruct
	public void init() {
		this.channelLogs = jda.getTextChannelById(config.channelLogId);
	}

	public void send(String message) {
		channelLogs.sendMessage(message).complete();
	}

	public void send(MessageCreateData message) {
		channelLogs.sendMessage(message).complete();
	}

	public void send(MessageEmbed embed) {
		channelLogs.sendMessageEmbeds(embed).complete();
	}

	public void sendEmbed(DiscordEmbed embed) {
		try {
			if (embed instanceof DiscordButtonMessage) {
				channelLogs.sendMessageEmbeds(embed.build()).setActionRow(((DiscordButtonMessage) embed).buttons()).complete();
			} else {
				channelLogs.sendMessageEmbeds(embed.build()).complete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when sending embed: " + embed.getClass().getName(), e);
		}
	}

	public void sendMessage(DiscordMessage message) {
		try {
			if (message instanceof DiscordButtonMessage) {
				channelLogs.sendMessage(message.body()).setActionRow(((DiscordButtonMessage) message).buttons()).complete();
			} else {
				channelLogs.sendMessage(message.body()).complete();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when sending embed: " + message.getClass().getName(), e);
		}
	}

	public void sendEmbed(String title, String contentTitle, String content, Color color) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setColor(color);
		builder.addField(new Field(contentTitle, content, true));
		send(builder.build());
	}

	public void sendEmbed(String title, Color color, Field... fields) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setColor(color);
		for (Field f : fields)
			builder.addField(f);
		send(builder.build());
	}

	public NextTask<DiscordEmbed, Void> prepareSendEmbed() {
		return NextTask.<DiscordEmbed>withArg(this::sendEmbed);
	}

	public NextTask<DiscordMessage, Void> prepareSendMessage() {
		return NextTask.<DiscordMessage>withArg(this::sendMessage);
	}

	public void shutdown() throws InterruptedException {
		if (R2ApiMain.TEST) {
			LOGGER.info("Started as TEST, ignoring unsent messages (" + jda.cancelRequests() + ")");
			jda.shutdownNow();
		} else {
			LOGGER.info("JDA shutdown requested.");
			jda.awaitShutdown();
		}
	}

}
