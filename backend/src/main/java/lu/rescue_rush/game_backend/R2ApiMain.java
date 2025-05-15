package lu.rescue_rush.game_backend;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import lu.pcy113.pclib.PCUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.integrations.discord.DiscordSender;
import lu.rescue_rush.game_backend.integrations.discord.embeds.system.EmbedShutdown;
import lu.rescue_rush.game_backend.integrations.discord.embeds.system.EmbedStartup;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.EventMonitor;
import lu.rescue_rush.game_backend.utils.monitoring.ExecutionMonitor;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
public class R2ApiMain {

	public static R2ApiMain INSTANCE;

	public static String[] PROFILES;
	public static String NAME, VERSION, BUILD, SHARED_VERSION;
	public static boolean DEBUG = false, TEST = false;

	public static final File CONFIG_DIR = new File("../config");
	public static final Logger LOGGER = Logger.getLogger(R2ApiMain.class.getName());

	private static Environment environment;

	@Autowired
	private ConfigurableApplicationContext context;

	// only here to initiate all the db connection/creation/initialization
	@Autowired
	private TableProxy TABLE_PROXY;

	@Autowired
	private DiscordSender DISCORD_SENDER;

	@Autowired
	private EmbedStartup EMBED_STARTUP;
	@Autowired
	private EmbedShutdown EMBED_SHUTDOWN;

	private static long START_TIME;

	private static void extractEnvironmentConsts() {
		if (environment == null) {
			LOGGER.warning("Environment is null before start.");
			return;
		}

		PROFILES = environment.getActiveProfiles();
		DEBUG = Arrays.stream(PROFILES).anyMatch(s -> "debug".equals(s));
		NAME = environment.getProperty("spring.application.name");
		VERSION = environment.getProperty("spring.application.version");
		BUILD = environment.getProperty("spring.application.build");
		SHARED_VERSION = environment.getProperty("spring.application.shared-version");
		for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
			if (s.getClassName().contains("org.junit.jupiter")) {
				TEST = true;
				break;
			}
		}
		TEST = false;
		LOGGER.info("Started profile: " + Arrays.toString(PROFILES) + " (" + DEBUG + ", " + TEST + ")");
		LOGGER.info("Application version: " + NAME + " v." + VERSION + "-build." + BUILD + "-shared." + SHARED_VERSION);
	}

	public R2ApiMain() {
		INSTANCE = this;
	}

	@PostConstruct
	public void setup() throws Exception {
		ExecutionMonitor.startTracker();

		if (environment == null) {
			environment = context.getEnvironment();
			extractEnvironmentConsts();
		}

		EventMonitor.push(EventMonitor.R2API_START, "Debug: " + Boolean.toString(DEBUG) + ", Test: " + Boolean.toString(TEST) + ", App: " + NAME + "#" + VERSION);

		DISCORD_SENDER.prepareSendEmbed().catch_(SpringUtils.catch_(LOGGER, "Couldn't send startup embed"))
				.runAsync(EMBED_STARTUP.build(PROFILES, DEBUG, NAME, VERSION, BUILD, SHARED_VERSION, environment, context));

		START_TIME = System.currentTimeMillis();
	}

	@PreDestroy
	public void destroy() throws InterruptedException {
		EventMonitor.push(EventMonitor.R2API_STOP);

		DISCORD_SENDER.prepareSendEmbed().catch_(SpringUtils.catch_(LOGGER, "Couldn't send shutdown embed")).run(EMBED_SHUTDOWN.build(START_TIME));

		ExecutionMonitor.stopTracker();
		LOGGER.info("Saving ExecutionTracker data due to graceful shutdown.");
		ExecutionMonitor.forceSave();
	}

	public void shutdown(ExitCodeGenerator exitCode) {
		LOGGER.info("Shutdown initiated by: " + PCUtils.getCallerClassName(2, false));
		SpringApplication.exit(context, exitCode);
	}

	public void shutdown() {
		LOGGER.info("Shutdown initiated by: " + PCUtils.getCallerClassName(1, false));
		SpringApplication.exit(context, () -> 0);
	}

	public ConfigurableApplicationContext getContext() {
		return context;
	}

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(R2ApiMain.class);
		app.addListeners(new ApplicationPidFileWriter(new File(CONFIG_DIR, "r2api.pid")));

		app.addListeners(new ApplicationListener<ApplicationEnvironmentPreparedEvent>() {
			@Override
			public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
				R2ApiMain.environment = event.getEnvironment();
				R2ApiMain.extractEnvironmentConsts();
			}
		});

		app.run(args);
	}

}
