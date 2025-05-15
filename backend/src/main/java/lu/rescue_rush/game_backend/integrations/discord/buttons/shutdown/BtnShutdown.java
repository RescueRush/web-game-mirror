package lu.rescue_rush.game_backend.integrations.discord.buttons.shutdown;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.rescue_rush.game_backend.integrations.discord.buttons.ButtonInteractionExecutor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Service
public class BtnShutdown implements ButtonInteractionExecutor {

	private static final Logger LOGGER = Logger.getLogger(BtnShutdown.class.getName());

	@Autowired
	private BtnShutdownConfirm btnShutdownConfirm;

	@Autowired
	private BtnShutdownCancel btnShutdownCancel;

	@Override
	public void execute(ButtonInteractionEvent event) {
		event.getHook()
				.sendMessage(":exclamation: :warning: **__Backend shutdown requested__** :warning: :exclamation:\n* " + event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + " "
						+ event.getUser().getId() + ")")
				.addActionRow(Button.danger(btnShutdownConfirm.id(), "Yes, shut that thang down !"), Button.primary(btnShutdownCancel.id(), "No, I'm sorry I won't do it again :sob:")).queue();

		LOGGER.info("User: " + event.getUser().getId() + " requested backend shutdown.");
	}

	@Override
	public String id() {
		return "backend_shutdown";
	}

}
