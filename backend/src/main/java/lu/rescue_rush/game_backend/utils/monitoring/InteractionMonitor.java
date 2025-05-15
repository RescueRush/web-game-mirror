package lu.rescue_rush.game_backend.utils.monitoring;

import java.util.logging.Logger;

import org.springframework.lang.Nullable;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.monitor.interaction.InteractionMonitoringEntryData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.utils.SpringUtils;

public class InteractionMonitor {

	private static final Logger LOGGER = Logger.getLogger(InteractionMonitor.class.getName());

	//@formatter:off
	public static final String
			R2API_USER_REGISTER = "r2api.user.register",
			R2API_USER_LOGIN = "r2api.user.login",
			R2API_GAME_CARDS = "r2api.game.cards",
			R2API_GAME_CREATED = "r2api.game.created",
			R2API_GAME_END = "r2api.game.end",
			R2API_GAME_STATE = "r2api.game.state",
			R2API_GAME_ANSWER = "r2api.game.answer",
			R2API_GAME_NEXT_QUESTION = "r2api.game.next_question",
			R2API_USER_LOGOUT = "r2api.user.logout",
			R2API_USER_LOGOUT_ALL = "r2api.user.logout_all",
			R2API_USER_DELETE = "r2api.user.delete",
			R2API_USER_TOKEN_VALID = "r2api.user.token_valid",
			R2API_USER_LEADERBOARD = "r2api.user.leaderboard",
			R2API_USER_UPDATE_NAME = "r2api.user.update_name",
			R2API_USER_UPDATE_PASS = "r2api.user.update_pass",
			R2API_USER_UPDATE_EMAIL = "r2api.user.update_email",
			R2API_SUPPORT_FORM_EMAIL = "r2api.support.form.email",
			R2API_ADMIN_EVENTS = "r2api.admin.events",
			R2API_NEWSLETTER_SUBSCRIBE = "r2api.newsletter.form.subscribe",
			R2API_NEWSLETTER_UNSUBSCRIBE = "r2api.newsletter.form.unsubscribe",
			R2API_LOGS_HOMEPAGE = "r2api.logs.homepage",
			R2API_INVALID_ENDPOINT = "r2api.endpoint.invalid",
			R2API_DEPRECATED_ENDPOINT = "r2api.endpoint.deprecated",
			R2API_WS_BEFORE_HANDSHAKE = "r2api.ws.before_handshake";
	//@formatter:on

	public static void pushAccepted() {
		push(SpringUtils.getContextUser(), SpringUtils.getContextPath(), true, SpringUtils.getContextSource());
	}

	public static void pushDenied() {
		push(SpringUtils.getContextUser(), SpringUtils.getContextPath(), false, SpringUtils.getContextSource());
	}

	public static void pushAcceptedDesc(@Nullable String desc) {
		pushDesc(SpringUtils.getContextUser(), SpringUtils.getContextPath(), true, SpringUtils.getContextSource(), desc);
	}

	public static void pushDeniedDesc(@Nullable String desc) {
		pushDesc(SpringUtils.getContextUser(), SpringUtils.getContextPath(), false, SpringUtils.getContextSource(), desc);
	}

	public static void pushOutcomeDesc(UserData ud, boolean outcome, @Nullable String desc) {
		pushDesc(ud, SpringUtils.getContextPath(), outcome, SpringUtils.getContextSource(), desc);
	}

	public static void pushOutcome(UserData ud, boolean outcome) {
		pushDesc(ud, SpringUtils.getContextPath(), outcome, SpringUtils.getContextSource(), null);
	}

	public static void pushOutcomeDesc(boolean outcome, @Nullable String desc) {
		pushDesc(SpringUtils.getContextUser(), SpringUtils.getContextPath(), outcome, SpringUtils.getContextSource(), desc);
	}

	public static void pushOutcome(boolean outcome) {
		pushDesc(SpringUtils.getContextUser(), SpringUtils.getContextPath(), outcome, SpringUtils.getContextSource(), null);
	}

	public static void pushOutcomeSourceDesc(boolean outcome, String source, @Nullable String desc) {
		pushDesc(SpringUtils.getContextUser(), SpringUtils.getContextPath(), outcome, source, desc);
	}

	public static void pushOutcomeSource(boolean outcome, String source) {
		pushDesc(SpringUtils.getContextUser(), SpringUtils.getContextPath(), outcome, source, null);
	}

	public static void pushDesc(UserData ud, boolean outcome, String source, @Nullable String desc) {
		pushDesc(ud, SpringUtils.getContextPath(), outcome, source, desc);
	}

	public static void push(UserData ud, boolean outcome, String source) {
		pushDesc(ud, SpringUtils.getContextPath(), outcome, source, null);
	}

	public static void push(UserData ud, String key, boolean outcome, String source) {
		pushDesc(ud, key, outcome, source, null);
	}

	public static void push(int userId, String key, boolean outcome, String source) {
		pushDesc(userId, key, outcome, source, null);
	}

	public static void pushDesc(UserData ud, String key, boolean outcome, String source, @Nullable String desc) {
		pushDesc(ud != null ? ud.getId() : 1, key, outcome, source, desc);
	}

	public static void pushDesc(int userId, String key, boolean outcome, String source, @Nullable String desc) {
		TableProxy.INTERACTION_MONITOR.insert(new InteractionMonitoringEntryData(TableProxy.INTERACTION_MONITOR_TYPE.loadOrInsertByKey(key), userId, outcome, source, desc)).catch_(e -> {
			LOGGER.severe("Failed to insert user tracking entry (from '" + key + "'): " + e.getMessage());
			if (R2ApiMain.DEBUG) {
				e.printStackTrace();
			}
		}).runAsync();
	}

}
