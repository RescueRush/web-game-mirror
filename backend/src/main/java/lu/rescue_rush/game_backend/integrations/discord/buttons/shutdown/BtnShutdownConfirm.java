package lu.rescue_rush.game_backend.integrations.discord.buttons.shutdown;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lu.pcy113.pclib.async.NextTask;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.buttons.ButtonInteractionExecutor;
import lu.rescue_rush.game_backend.utils.monitoring.EventMonitor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Service
public class BtnShutdownConfirm implements ButtonInteractionExecutor {

	private static final Logger LOGGER = Logger.getLogger(BtnShutdownConfirm.class.getName());

	@Override
	public void execute(ButtonInteractionEvent event) {
		event.deferReply().queue();

		event.getHook()
				.editOriginal(
						event.getMessage().getContentRaw() + "\n**Acknowledged by:**\n* " + event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + " " + event.getUser().getId() + ")")
				.setComponents(event.getMessage().getActionRows().stream().map(t -> t.asDisabled()).collect(Collectors.toList())).queue();

		EventMonitor.push(EventMonitor.R2API_SHUTDOWN_CONFIRMED, event.getGuild().getId() + ">" + event.getChannelId() + " by " + event.getUser().getId());

		LOGGER.info("User: " + event.getUser().getId() + " acknowledged backend shutdown.");

		NextTask.create(() -> {
			Thread.sleep(2_000);
			R2ApiMain.INSTANCE.shutdown();
		}).catch_(e -> {
			e.printStackTrace();
			event.getChannel().sendMessage(e.toString()).complete();
		}).runAsync();
	}

	@Override
	public String id() {
		return "backend_shutdown_confirm";
	}

}
