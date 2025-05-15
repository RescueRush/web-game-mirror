package lu.rescue_rush.game_backend.integrations.discord.commands.ban;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.pcy113.pclib.PCUtils;

import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandAutocomplete;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandExecutor;
import lu.rescue_rush.game_backend.integrations.discord.modals.ModalUserBanConfirm;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class CmdUserBan implements SlashCommandExecutor, SlashCommandAutocomplete {

	private static final Logger LOGGER = Logger.getLogger(CmdUserBan.class.getName());

	@Autowired
	private ModalUserBanConfirm modalUserBan;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		// event.deferReply().queue();

		UserData ud = null;
		switch (event.getOption("inputtype").getAsString()) {
		case "id":
			ud = TableProxy.USER.byId(PCUtils.parseInteger(event.getOption("value").getAsString(), -1));
			break;
		case "email":
			ud = TableProxy.USER.byEmail(event.getOption("value").getAsString());
			break;
		case "name":
			ud = TableProxy.USER.byName(event.getOption("value").getAsString());
			break;
		}

		if (ud == null) {
			event.getHook().sendMessage("I'm sorry, " + event.getUser().getAsMention() + ", this user doesn't exists LOoOoOoOoOL.").queue();
			return;
		}

		event.replyModal(modalUserBan.build(ud.getId())).complete();
	}

	@Override
	public void complete(CommandAutoCompleteInteractionEvent event) {
		if (event.getFocusedOption().getName().equals("value")) {
			switch (event.getOption("inputtype").getAsString()) {
			case "id":
				// do not autocomplete by id
				break;
			case "email":
				event.replyChoices(TableProxy.USER.byMatchingEmail(event.getFocusedOption().getValue(), 25).parallelStream()
						.map(v -> new Command.Choice(v.getEmail() + " (" + v.getName() + ")", v.getEmail())).toList()).complete();
				break;
			case "name":
				event.replyChoices(TableProxy.USER.byMatchingName(event.getFocusedOption().getValue(), 25).parallelStream()
						.map(v -> new Command.Choice(v.getName() + " (" + v.getEmail() + ")", v.getName())).toList()).complete();
				break;
			default:
				LOGGER.warning("Unexpected input type: " + event.getOption("inputtype").getAsString());
				break;
			}
		}
	}

	@Override
	public String id() {
		return "userban";
	}

	@Override
	public String description() {
		return "Bans the specified user.";
	}

	@Override
	public OptionData[] options() {
		return new OptionData[] {
		//@formatter:off
				new OptionData(OptionType.STRING, "inputtype", "Input method: id, email, name", true)
						.addChoice("ID", "id")
						.addChoice("Email", "email")
						.addChoice("Username", "name"),
				new OptionData(OptionType.STRING, "value", "ID, email or name", true, true)
		};
		//@formatter:on
	}

}
