package lu.rescue_rush.game_backend.db;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.DataBaseView;
import lu.pcy113.pclib.db.TableHelper;

import jakarta.annotation.PostConstruct;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.data.user.UserPermissionTypeData;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionReasonData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizGameProgressTable;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizGameTable;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizQuestionTable;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioGameProgressTable;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioGameTable;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioQuestionTable;
import lu.rescue_rush.game_backend.db.tables.monitor.email.VerifiedEmailTable;
import lu.rescue_rush.game_backend.db.tables.monitor.event.EventTable;
import lu.rescue_rush.game_backend.db.tables.monitor.event.EventTypeTable;
import lu.rescue_rush.game_backend.db.tables.monitor.execution.ExecutionMonitorTable;
import lu.rescue_rush.game_backend.db.tables.monitor.interaction.InteractionMonitorTable;
import lu.rescue_rush.game_backend.db.tables.monitor.interaction.InteractionMonitorTypeTable;
import lu.rescue_rush.game_backend.db.tables.monitor.traffic.TrafficMonitorTable;
import lu.rescue_rush.game_backend.db.tables.monitor.user.UserPassResetTable;
import lu.rescue_rush.game_backend.db.tables.monitor.user.UserProfileHistoryTable;
import lu.rescue_rush.game_backend.db.tables.newsletter.NewsletterSubscriptionTable;
import lu.rescue_rush.game_backend.db.tables.support.SupportFormTable;
import lu.rescue_rush.game_backend.db.tables.user.UserPermissionTable;
import lu.rescue_rush.game_backend.db.tables.user.UserPermissionTypeTable;
import lu.rescue_rush.game_backend.db.tables.user.UserSanctionReasonTable;
import lu.rescue_rush.game_backend.db.tables.user.UserSanctionTable;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;
import lu.rescue_rush.game_backend.db.views.R2DBView;
import lu.rescue_rush.game_backend.db.views.game.GameProgress_Stats_View;
import lu.rescue_rush.game_backend.db.views.game.GameProgress_View;
import lu.rescue_rush.game_backend.db.views.monitor.execution.ExecutionMonitor_SummaryPerKeyPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.execution.ExecutionMonitor_SummaryPerKey_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_ActivityPerUserPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_ActivityPerUser_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_CountPerSource_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_CountPerType_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_Newest_View;
import lu.rescue_rush.game_backend.db.views.monitor.interaction.InteractionMonitor_SummaryOutcome_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPer10Minutes_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerHour_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_SummaryPerMinute_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopRefererPerDay_View;
import lu.rescue_rush.game_backend.db.views.monitor.traffic.TrafficMonitor_TopRefererPerHour_View;

@Component
public class TableProxy {

	private static final Logger LOGGER = Logger.getLogger(TableProxy.class.getName());

	public static TableProxy INSTANCE;

	private Map<Class<?>, DataBase> databases = new HashMap<>();
	private Map<Class<?>, DataBaseTable<?>> tables = new HashMap<>();
	private Map<Class<?>, DataBaseView<?>> views = new HashMap<>();

	@Autowired
	private R2DBBase base;

	@Autowired
	private ScenarioQuestionTable quizQuestions;
	@Autowired
	private ScenarioGameProgressTable quizGameProgresses;
	@Autowired
	private ScenarioGameTable quizGames;

	@Autowired
	private QuizQuestionTable guessQuestions;
	@Autowired
	private QuizGameProgressTable guessGameProgresses;
	@Autowired
	private QuizGameTable guessGames;

	@Autowired
	private GameProgress_View gameProgress;
	@Autowired
	private GameProgress_Stats_View gameStats;

	@Autowired
	private EventTable events;
	@Autowired
	private EventTypeTable eventTypes;

	@Autowired
	private ExecutionMonitorTable perfs;
	@Autowired
	private ExecutionMonitor_SummaryPerKey_View execution_summaryPerKey;
	@Autowired
	private ExecutionMonitor_SummaryPerKeyPerDay_View execution_summaryPerKeyPerDay;

	@Autowired
	private TrafficMonitorTable trafficMonitoring;
	@Autowired
	private TrafficMonitor_SummaryPerDay_View homepageTraffic_summaryPerDay;
	@Autowired
	private TrafficMonitor_SummaryPerHour_View homepageTraffic_summaryPerHour;
	@Autowired
	private TrafficMonitor_SummaryPer10Minutes_View homepageTraffic_summaryPer10Minutes;
	@Autowired
	private TrafficMonitor_SummaryPerMinute_View homepageTraffic_summaryPerMinute;
	@Autowired
	private TrafficMonitor_TopRefererPerDay_View homepageTraffic_topRefererPerDay;
	@Autowired
	private TrafficMonitor_TopRefererPerHour_View homepageTraffic_topRefererPerHour;

	@Autowired
	private InteractionMonitorTable interactionMonitorings;
	@Autowired
	private InteractionMonitor_ActivityPerUser_View userTrackings_activityPerUser;
	@Autowired
	private InteractionMonitor_ActivityPerUserPerDay_View userTrackings_activityPerUserPerDay;
	@Autowired
	private InteractionMonitor_CountPerSource_View userTrackings_countPerSource;
	@Autowired
	private InteractionMonitor_CountPerType_View userTrackings_countPerType;
	@Autowired
	private InteractionMonitor_Newest_View userTrackings_newest;
	@Autowired
	private InteractionMonitor_SummaryOutcome_View userTrackings_summaryOutcome;
	@Autowired
	private InteractionMonitorTypeTable userTrackingTypes;

	@Autowired
	private UserPermissionTypeTable userPermissionTypes;
	@Autowired
	private UserPermissionTable userPermissions;
	@Autowired
	private UserSanctionReasonTable userSanctionReasons;
	@Autowired
	private UserSanctionTable userSanctions;
	@Autowired
	private UserProfileHistoryTable userProfileHistory;
	@Autowired
	private UserPassResetTable userPassResets;
	@Autowired
	private UserTable users;

	@Autowired
	private VerifiedEmailTable emailVerifications;

	@Autowired
	private SupportFormTable supportEmailForms;

	@Autowired
	private NewsletterSubscriptionTable newsletterEmails;

	public TableProxy(ApplicationContext context) {
		for (DataBase db : context.getBeansOfType(DataBase.class).values()) {
			Class<?> clazz = AopUtils.getTargetClass(db);
			if (!R2DBBase.class.isAssignableFrom(clazz)) {
				LOGGER.warning("Base `" + clazz.getSimpleName() + "` is NOT of type R2DBBase. This is deprecated/discouraged.");
			}
			this.databases.put(clazz, db);
		}

		for (DataBaseTable<?> table : context.getBeansOfType(DataBaseTable.class).values()) {
			Class<?> clazz = AopUtils.getTargetClass(table);
			if (!R2DBTable.class.isAssignableFrom(clazz)) {
				LOGGER.warning("Table `" + clazz.getSimpleName() + "` is NOT of type R2DBTable. This is deprecated/discouraged.");
			}
			this.tables.put(clazz, table);
		}

		for (DataBaseView<?> view : context.getBeansOfType(DataBaseView.class).values()) {
			Class<?> clazz = AopUtils.getTargetClass(view);
			if (!R2DBView.class.isAssignableFrom(clazz)) {
				LOGGER.warning("View `" + clazz.getSimpleName() + "` is NOT of type R2DBView. This is deprecated/discouraged.");
			}
			this.views.put(clazz, view);
		}

		INSTANCE = this;
	}

	@PostConstruct
	public void init() throws Exception {
		DATABASES = databases;
		TABLES = tables;
		VIEWS = views;

		BASE = base;

		SCENARIO_QUESTION = quizQuestions;
		SCENARIO_GAME_PROGRESS = quizGameProgresses;
		SCENARIO_GAME = quizGames;

		QUIZ_QUESTION = guessQuestions;
		QUIZ_GAME_PROGRESS = guessGameProgresses;
		QUIZ_GAME = guessGames;

		GAME_PROGRESS = gameProgress;
		GAME_STATS = gameStats;

		EVENT_TYPES = eventTypes;
		EVENTS = events;

		PERFS = perfs;

		PERFS.SUMMARY_PER_KEY = execution_summaryPerKey;
		PERFS.SUMMARY_PER_KEY_PER_DAY = execution_summaryPerKeyPerDay;

		TRAFFIC_HOMEPAGE = trafficMonitoring;
		TRAFFIC_HOMEPAGE.SUMMARY_PER_DAY = homepageTraffic_summaryPerDay;
		TRAFFIC_HOMEPAGE.SUMMARY_PER_HOUR = homepageTraffic_summaryPerHour;
		TRAFFIC_HOMEPAGE.SUMMARY_PER_10_MINUTES = homepageTraffic_summaryPer10Minutes;
		TRAFFIC_HOMEPAGE.SUMMARY_PER_MINUTE = homepageTraffic_summaryPerMinute;
		TRAFFIC_HOMEPAGE.TOP_REFERER_PER_DAY = homepageTraffic_topRefererPerDay;
		TRAFFIC_HOMEPAGE.TOP_REFERER_PER_HOUR = homepageTraffic_topRefererPerHour;

		INTERACTION_MONITOR = interactionMonitorings;
		INTERACTION_MONITOR.ACTIVITY_PER_USER = userTrackings_activityPerUser;
		INTERACTION_MONITOR.ACTIVITY_PER_USER_PER_DAY = userTrackings_activityPerUserPerDay;
		INTERACTION_MONITOR.COUNT_PER_SOURCE = userTrackings_countPerSource;
		INTERACTION_MONITOR.COUNT_PER_TYPE = userTrackings_countPerType;
		INTERACTION_MONITOR.NEWEST = userTrackings_newest;
		INTERACTION_MONITOR.SUMMARY_OUTCOME = userTrackings_summaryOutcome;
		INTERACTION_MONITOR_TYPE = userTrackingTypes;

		USER_PERMISSION_TYPE = userPermissionTypes;
		USER_PERMISSION = userPermissions;
		USER_SANCTION = userSanctions;

		USER_SANCTION_REASON = userSanctionReasons;
		USER_PROFILE_HISTORY = userProfileHistory;
		USER_PASS_RESET = userPassResets;
		USER = users;

		EMAIL_VERIFICATIONS = emailVerifications;

		SUPPORT_EMAIL_FORM = supportEmailForms;

		NEWSLETTER_EMAIL = newsletterEmails;

		insertDefaults();
	}

	public void insertDefaults() throws Exception {
		// -- Add default users
		final UserData admin = TableHelper
				.insertOrLoad(USER, new UserData("admin@rescue-rush.lu", "Superuser", UserData.hashPass("admin"), Language.LUXEMBOURISH), () -> UserData.byEmail("admin@rescue-rush.lu")).runThrow();
		admin.addSanctionIfNotPresent(UserSanctionReasonData.KEY_HIDDEN, admin, "Set admin as hidden.");
		admin.addPermission(UserPermissionTypeData.KEY_SUPERUSER, admin, "Set admin as superuser.");

		final UserData discord = TableHelper
				.insertOrLoad(USER, new UserData("discord@rescue-rush.lu", "Discord", UserData.hashPass("discord"), Language.LUXEMBOURISH), () -> UserData.byEmail("discord@rescue-rush.lu"))
				.runThrow();
		discord.addSanctionIfNotPresent(UserSanctionReasonData.KEY_HIDDEN, admin, "Set discord as hidden.");
		discord.addPermission(UserPermissionTypeData.KEY_SUPERUSER, admin, "Set discord as superuser.");

		if (R2ApiMain.DEBUG) {
			System.err.println("Regen new token for admin: '" + admin.genNewToken() + "'");
			admin.push();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends DataBase> T database(Class<T> clazz) {
		return (T) DATABASES.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T extends DataBaseTable<?>> T table(Class<T> clazz) {
		return (T) TABLES.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T extends DataBaseView<?>> T view(Class<T> clazz) {
		return (T) VIEWS.get(clazz);
	}

	public static Map<Class<?>, DataBase> DATABASES;
	public static Map<Class<?>, DataBaseTable<?>> TABLES;
	public static Map<Class<?>, DataBaseView<?>> VIEWS;

	public static R2DBBase BASE;

	public static ScenarioQuestionTable SCENARIO_QUESTION;
	public static ScenarioGameProgressTable SCENARIO_GAME_PROGRESS;
	public static ScenarioGameTable SCENARIO_GAME;

	public static QuizQuestionTable QUIZ_QUESTION;
	public static QuizGameProgressTable QUIZ_GAME_PROGRESS;
	public static QuizGameTable QUIZ_GAME;

	public static GameProgress_View GAME_PROGRESS;
	public static GameProgress_Stats_View GAME_STATS;

	public static EventTable EVENTS;
	public static EventTypeTable EVENT_TYPES;

	public static ExecutionMonitorTable PERFS;

	public static TrafficMonitorTable TRAFFIC_HOMEPAGE;

	public static InteractionMonitorTable INTERACTION_MONITOR;
	public static InteractionMonitorTypeTable INTERACTION_MONITOR_TYPE;

	public static UserTable USER;
	public static UserSanctionTable USER_SANCTION;
	public static UserSanctionReasonTable USER_SANCTION_REASON;
	public static UserPermissionTable USER_PERMISSION;
	public static UserPermissionTypeTable USER_PERMISSION_TYPE;
	public static UserProfileHistoryTable USER_PROFILE_HISTORY;
	public static UserPassResetTable USER_PASS_RESET;

	public static VerifiedEmailTable EMAIL_VERIFICATIONS;

	public static SupportFormTable SUPPORT_EMAIL_FORM;

	public static NewsletterSubscriptionTable NEWSLETTER_EMAIL;

}
