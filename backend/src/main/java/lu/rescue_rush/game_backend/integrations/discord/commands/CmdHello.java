package lu.rescue_rush.game_backend.integrations.discord.commands;

import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.integrations.discord.messages.MessageHello;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class CmdHello implements SlashCommandExecutor, SlashCommandAutocomplete {

	public static final String ID = "hello";

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.getOption("user") != null) {
			event.reply(new MessageHello(event.getGuild().getMemberById(event.getOption("user").getAsString()).getAsMention()).body()).queue();
		} else {
			event.reply(new MessageHello(event.getMember().getAsMention()).body()).queue();
		}
	}

	@Override
	public void complete(CommandAutoCompleteInteractionEvent event) {
		if (event.getFocusedOption().getName().equals("user")) {
			event.replyChoices(event.getGuild().getMembers().parallelStream().filter(m -> m.getEffectiveName().toLowerCase().startsWith(event.getFocusedOption().getValue().toLowerCase()))
					.map(m -> new Command.Choice(m.getEffectiveName(), m.getId())).toList()).complete();
		}
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public OptionData[] options() {
		return new OptionData[] {new OptionData(OptionType.STRING, "user", "The user", false, true)};
	}

	@Override
	public String description() {
		return "Says hello. DUH !";
	}

}
