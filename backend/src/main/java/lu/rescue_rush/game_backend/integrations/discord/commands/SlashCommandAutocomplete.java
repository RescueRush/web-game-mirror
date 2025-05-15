package lu.rescue_rush.game_backend.integrations.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public interface SlashCommandAutocomplete {

	void complete(CommandAutoCompleteInteractionEvent event);

}
