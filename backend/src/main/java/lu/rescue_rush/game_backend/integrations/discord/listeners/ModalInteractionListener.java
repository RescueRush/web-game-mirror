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
import lu.rescue_rush.game_backend.integrations.discord.modals.ModalInteractionExecutor;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Service
public class ModalInteractionListener extends ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(ModalInteractionListener.class.getName());

	@Autowired
	private JDA jda;

	@Autowired
	private DiscordBotConfig config;

	private Map<String, ModalInteractionExecutor> listeners = new HashMap<>();

	@PostConstruct
	public void init() {
		jda.addEventListener(this);

		NextTask.create(() -> {
			Map<String, ModalInteractionExecutor> beans = R2ApiMain.INSTANCE.getContext().getBeansOfType(ModalInteractionExecutor.class);
			beans.values().forEach(this::registerInteraction);
		}).catch_(SpringUtils.catch_(null, "Error registering modal interactions.")).runAsync();
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if (!event.getChannelId().equals(config.channelLogId)) {
			LOGGER.info("Ignoring modal interaction from channel: " + event.getChannelId());
			return;
		}

		if (hasModal(event.getModalId())) {
			LOGGER.info("Got modal interaction '" + event.getModalId() + "' from channel: " + event.getChannelId());
			getListener(event.getModalId()).execute(event);
		} else {
			LOGGER.warn("No modal interaction registered for: " + event.getModalId());
		}
	}

	private ModalInteractionExecutor getListener(String modalId) {
		modalId = modalId.contains(":") ? modalId.split(":")[0] : modalId;
		return listeners.get(modalId);
	}

	private boolean hasModal(String modalId) {
		return listeners.containsKey(modalId) || listeners.values().stream().anyMatch(list -> modalId.contains(":") ? modalId.split(":")[0].equals(list.name()) : false);
	}

	public void registerInteraction(ModalInteractionExecutor interaction) {
		listeners.put(interaction.name(), interaction);

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Registered modal interaction: " + interaction.name());
		}
	}

}
