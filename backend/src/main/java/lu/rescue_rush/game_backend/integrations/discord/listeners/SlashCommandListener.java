package lu.rescue_rush.game_backend.integrations.discord.listeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.async.NextTask;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.DiscordBotConfig;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandAutocomplete;
import lu.rescue_rush.game_backend.integrations.discord.commands.SlashCommandExecutor;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

@Service
public class SlashCommandListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(SlashCommandListener.class.getName());

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordBotConfig config;

	private Guild guild;

	private Map<String, SlashCommandExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		guild = jda.getGuildById(config.serverId);

		jda.addEventListener(this);

		NextTask.create(() -> {
			final Map<String, SlashCommandExecutor> beans = R2ApiMain.INSTANCE.getContext().getBeansOfType(SlashCommandExecutor.class);

			beans.values().stream().forEach(this::registerCommand);
			
			/*guild.updateCommands().addCommands(Collections.emptyList()).complete();
			LOGGER.info("Registering slash commands: " + beans.size());
			final CommandListUpdateAction updateCommands = guild.updateCommands();
			System.err.println(beans.values());
			beans.values().stream().forEach(b -> System.err.println(b));
			beans.values().stream().forEach(b -> System.err.println(b.build()));
			System.err.println(beans.values().stream().map(ce -> ce.build()).toList());
			updateCommands.addCommands(beans.values().stream().map(ce -> ce.build()).toList());
			updateCommands.queue((c) -> {
				LOGGER.info("Registered slash commands: " + beans.size());
			}, (e) -> {
				LOGGER.info("Exception while registering slash commands: " + PCUtils.getRootCauseMessage(e));
				if (R2ApiMain.DEBUG) {
					e.printStackTrace();
				}
			});*/
		}).catch_(SpringUtils.catch_(LOGGER, "Error registering slash commands.")).runAsync();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (!event.getChannelId().equals(config.channelLogId)) {
			LOGGER.info("Ignoring slash command from channel: " + event.getChannelId());
			return;
		}

		if (listeners.containsKey(event.getName())) {
			if (R2ApiMain.DEBUG) {
				LOGGER.info("Got slash command '" + event.getName() + "' from channel: " + event.getChannelId());
			}

			try {
				listeners.get(event.getName()).execute(event);
			} catch (Exception e) {
				final String msg = "A method executor (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")";
				event.getHook().sendMessage(msg).queue(null, (f) -> event.getChannel().sendMessage(msg).queue());
				if (R2ApiMain.DEBUG) {
					e.printStackTrace();
				}
			}
		} else {
			LOGGER.warning("No slash command registered for: " + event.getName());
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		if (!event.getChannelId().equals(config.channelLogId)) {
			if (R2ApiMain.DEBUG) {
				LOGGER.info("Ignoring slash command from channel: " + event.getChannelId());
			}
			return;
		}

		if (listeners.containsKey(event.getName())) {
			if (R2ApiMain.DEBUG) {
				LOGGER.info("Got slash command autocomplete '" + event.getName() + "' (" + event.getFocusedOption().getName() + ") from channel: " + event.getChannelId() + " = '"
						+ event.getFocusedOption().getValue() + "'");
			}

			final SlashCommandExecutor listener = listeners.get(event.getName());

			if (listener instanceof SlashCommandAutocomplete autocompleteListener) {
				try {
					autocompleteListener.complete(event);
				} catch (Exception e) {
					event.getChannel().sendMessage("A method completer (`" + event.getName() + "`) raised an exception: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")").queue();
				}
			} else {
				LOGGER.warning("No slash command autocomplete registered for: " + event.getName() + " (" + event.getFocusedOption().getName() + ")");
			}
		} else {
			LOGGER.warning("No slash command registered for: " + event.getName() + " (" + event.getFocusedOption().getName() + ")");
		}
	}

	@Deprecated
	public void registerCommand(SlashCommandExecutor command) {
		listeners.put(command.id(), command);

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Registering slash command: " + command.id() + " (" + command.description() + ")");
		}

		guild.upsertCommand(command.build()).queue((c) -> {
			LOGGER.info("Registered slash command: " + command.id() + " (" + command.description() + ")");
		}, (e) -> {
			LOGGER.info("Exception while registering slash command: " + command.id() + " (" + command.description() + "): " + PCUtils.getRootCauseMessage(e));
			if (R2ApiMain.DEBUG) {
				e.printStackTrace();
			}
		});
	}

}
