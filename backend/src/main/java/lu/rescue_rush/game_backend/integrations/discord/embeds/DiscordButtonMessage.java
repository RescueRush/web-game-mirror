package lu.rescue_rush.game_backend.integrations.discord.embeds;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public interface DiscordButtonMessage {

	Button button();

	default Button[] buttons() {
		return new Button[] { button() };
	}

}
