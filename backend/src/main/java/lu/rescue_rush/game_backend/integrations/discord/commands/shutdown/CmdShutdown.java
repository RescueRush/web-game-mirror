package lu.rescue_rush.game_backend.integrations.discord.commands.shutdown;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.integrations.discord.buttons.shutdown.BtnShutdownCancel;
import lu.rescue_rush.game_backend.integrations.discord.buttons.shutdown.BtnShutdownConfirm;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Component
public class CmdShutdown implements SlashCommandExecutor {

	private static final Logger LOGGER = Logger.getLogger(CmdShutdown.class.getName());

	@Autowired
	private BtnShutdownConfirm btnShutdownConfirm;

	@Autowired
	private BtnShutdownCancel btnShutdownCancel;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.getHook()
				.sendMessage(":exclamation: :warning: **__Backend shutdown requested__** :warning: :exclamation:\n* " + event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + " "
						+ event.getUser().getId() + ")")
				.addActionRow(Button.danger(btnShutdownConfirm.id(), "Yes, shut that thang down !"), Button.primary(btnShutdownCancel.id(), "No, I'm sorry I won't do it again :sob:")).queue();

		LOGGER.info("User: " + event.getUser().getId() + " requested backend shutdown.");
	}

	@Override
	public String id() {
		return "shutdown";
	}

	public String description() {
		return "Requests a backend shutdown.";
	}

}
