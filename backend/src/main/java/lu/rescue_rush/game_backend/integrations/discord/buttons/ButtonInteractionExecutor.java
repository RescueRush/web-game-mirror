package lu.rescue_rush.game_backend.integrations.discord.buttons;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ButtonInteractionExecutor {

	void execute(ButtonInteractionEvent event);

	String id();

}
