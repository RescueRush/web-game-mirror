package lu.rescue_rush.game_backend.integrations.discord.modals;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;

import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionReasonData;
import lu.rescue_rush.game_backend.integrations.email.EmailSender;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

@Service
public class ModalUserHideConfirm implements ModalInteractionExecutor {

	private static final Logger LOGGER = Logger.getLogger(ModalUserHideConfirm.class.getName());

	@Autowired
	private EmailSender emailSender;

	@Override
	public void execute(ModalInteractionEvent event) {
		event.deferReply().queue();

		if (event.getModalId().split(":").length == 1) {
			event.getHook().sendMessage("No user found :'( (" + event.getModalId() + ")").setEphemeral(true).queue();
			return;
		}
		final int userId = PCUtils.parseInteger(event.getModalId().split(":")[1], -1);
		if (userId == -1) {
			event.getHook().sendMessage("No user found :'( '" + event.getModalId() + ")").setEphemeral(true).queue();
			return;
		}

		String reason = event.getValue("reason").getAsString();
		if (reason.isBlank()) {
			event.getHook().sendMessage("Blank reason, using default 'Hidden.'.").setEphemeral(true).queue();
			reason = "Hidden.";
		}

		final UserData ud = TableProxy.USER.byId(userId);
		final UserData discord = TableProxy.USER.byEmail("discord@rescue-rush.lu");
		if (ud == null) {
			event.reply("I'm sorry, " + event.getUser().getAsMention() + ", this user doesn't exists LOoOoOoOoOL.").setEphemeral(true).queue();
			return;
		}

		ud.loadSanctionReasonDatas();
		if (ud.hasActiveSanction(UserSanctionReasonData.KEY_HIDDEN)) {
			event.getHook().sendMessage("I'm sorry, " + event.getUser().getAsMention() + ", this user is already hidden :saluting_face:.").queue();
			return;
		}

		ud.addSanction(UserSanctionReasonData.KEY_HIDDEN, discord, reason);

		event.getHook().sendMessage("```User: " + ud.getId() + " (" + ud.getName() + " / " + ud.getEmail() + ")```**Hidden by:**\n* " + event.getUser().getAsMention() + " ("
				+ event.getUser().getAsTag() + " / " + event.getUser().getId() + ")\n**Reason:**```" + reason + "```").complete();

		LOGGER.info("User: " + event.getUser().getId() + " confirmed user hide onto: " + userId + ".");

		emailSender.prepareUserHiddenEmail().catch_(SpringUtils.catch_(LOGGER, "Couldn't send user hidden notice email")).runAsync(ud);
	}

	@Override
	public String title() {
		return "Hide user";
	}

	@Override
	public ItemComponent component() {
		return TextInput.create("reason", "Reason", TextInputStyle.PARAGRAPH).build();
	}

	@Override
	public String name() {
		return "user_hide_confirm";
	}

}
