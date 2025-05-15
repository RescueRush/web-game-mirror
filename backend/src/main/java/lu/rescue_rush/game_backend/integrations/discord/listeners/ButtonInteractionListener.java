package lu.rescue_rush.game_backend.integrations.discord.listeners;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.async.NextTask;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.integrations.discord.DiscordBotConfig;
import lu.rescue_rush.game_backend.integrations.discord.buttons.ButtonInteractionExecutor;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Service
public class ButtonInteractionListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(ButtonInteractionListener.class.getName());

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordBotConfig config;

	private Map<String, ButtonInteractionExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		jda.addEventListener(this);

		NextTask.create(() -> {
			Map<String, ButtonInteractionExecutor> beans = R2ApiMain.INSTANCE.getContext().getBeansOfType(ButtonInteractionExecutor.class);
			beans.values().forEach(this::registerInteraction);
		}).catch_(SpringUtils.catch_(null, "Error registering button interactions.")).runAsync();
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (!event.getChannelId().equals(config.channelLogId)) {
			LOGGER.info("Ignoring button interaction from channel: " + event.getChannelId());
			return;
		}

		if (listeners.containsKey(event.getComponentId())) {
			LOGGER.info("Got button interaction '" + event.getComponentId() + "' from channel: " + event.getChannelId());
			listeners.get(event.getComponentId()).execute(event);
		} else {
			LOGGER.warn("No button interaction registered for: " + event.getComponentId());
		}
	}

	public void registerInteraction(ButtonInteractionExecutor interaction) {
		listeners.put(interaction.id(), interaction);

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Registered button interaction: " + interaction.id());
		}
	}

}
