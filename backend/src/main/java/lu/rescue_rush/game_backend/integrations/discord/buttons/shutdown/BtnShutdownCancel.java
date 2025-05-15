package lu.rescue_rush.game_backend.integrations.discord.buttons.shutdown;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lu.rescue_rush.game_backend.integrations.discord.buttons.ButtonInteractionExecutor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Service
public class BtnShutdownCancel implements ButtonInteractionExecutor {

	private static final Logger LOGGER = Logger.getLogger(BtnShutdownCancel.class.getName());

	@Override
	public void execute(ButtonInteractionEvent event) {
		event.deferEdit().queue();

		event.getHook()
				.editOriginal(event.getMessage().getContentRaw() + "\n**Cancelled by:**\n* " + event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + " " + event.getUser().getId() + ")")
				.setComponents(event.getMessage().getActionRows().stream().map(t -> t.asDisabled()).collect(Collectors.toList())).queue();

		LOGGER.info("User: " + event.getUser().getId() + " cancelled backend shutdown.");
	}

	@Override
	public String id() {
		return "backend_shutdown_cancel";
	}

}
