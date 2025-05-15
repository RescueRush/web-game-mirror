package lu.rescue_rush.game_backend.endpoints.game;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lu.pcy113.pclib.datastructure.pair.Pair;

import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.data.Locales;
import lu.rescue_rush.game_backend.data.MaterialCard;
import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioGameData;
import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioGameProgressData;
import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioQuestionData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioGameProgressTable;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioGameTable;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioQuestionTable;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.types.game.GameScenarioTypes.AnswerResponse;
import lu.rescue_rush.game_backend.types.game.GameScenarioTypes.GameStateResponse;
import lu.rescue_rush.game_backend.types.game.GameScenarioTypes.QuestionResponse;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.AllowAnonymous;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@CrossOrigin
@RestController
@RequestMapping(value = "/game/scenario")
public class GameScenarioEndpoints {

	private static final int TOTAL_ANONYMOUS_QUESTIONS = 6;

	private Logger LOGGER = Logger.getLogger(GameScenarioEndpoints.class.getName());

	@Autowired
	private ScenarioGameTable SCENARIO_GAME;

	@Autowired
	private ScenarioGameProgressTable SCENARIO_GAME_PROGRESS;

	@Autowired
	private ScenarioQuestionTable SCENARIO_QUESTIONS;

	/**
	 * Ends any active {@link ScenarioGameData game} associated with the currently
	 * logged in user/anonymous session and creates a new one. <br>
	 * Returns the current (first) {@link ScenarioQuestionData question} of the new
	 * game, in the current locale.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#NOT_FOUND} User not found.</li>
	 * <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} Current question unanswered.
	 * (unlikely)</li>
	 * <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} Couldn't fetch question.</li>
	 * <li>{@link HttpStatus#BAD_REQUEST} No questions available.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED} : {@link QuestionResponse}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "new", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> newGame(HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();

		if (SCENARIO_QUESTIONS.count().run() == 0) {
			SpringUtils.badRequest(true, "No questions available.");
		}

		if (auth instanceof AnonymousAuthenticationToken) {
			final HashMap<String, Object> principal = (HashMap<String, Object>) SpringUtils.getContextPrincipal();

			if (principal.get("scenario_game") == null) {
				principal.put("scenario_game", new ScenarioGameData());
			} else {
				principal.put("scenario_game", new ScenarioGameData());
			}

			principal.put("scenario_game_remaining", TOTAL_ANONYMOUS_QUESTIONS);

			final ScenarioGameData qgd = (ScenarioGameData) principal.get("scenario_game");
			InteractionMonitor.pushAccepted();

			final Pair<Integer, ScenarioQuestionData> cq = qgd.nextQuestion();

			return ResponseEntity.accepted().body(
					new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), cq.getValue().getPossibleAnswers()));
		}

		final UserData ud = SpringUtils.needContextUser();

		if (SCENARIO_GAME.hasGame(ud)) {
			SCENARIO_GAME.endGame(ud);
		}

		final ScenarioGameData qgd = SCENARIO_GAME.create(ud);
		final ScenarioGameProgressData qgpd = SCENARIO_GAME_PROGRESS.updateScenarioGameProgressData(SCENARIO_GAME_PROGRESS.loadOrInsertByUser(ud).startGame(qgd));

		InteractionMonitor.pushAccepted();

		final Pair<Integer, ScenarioQuestionData> cq = qgd.nextQuestion();

		SCENARIO_GAME.updateScenarioGameData(qgd);

		return ResponseEntity.accepted().body(
				new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), cq.getValue().getPossibleAnswers()));
	}

	/**
	 * Picks a new question according to {@link ScenarioGameData#nextQuestion()},
	 * returns it as text in the current locale.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} No game started.</li>
	 * <li>{@link HttpStatus#NOT_FOUND} User not found.</li>
	 * <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} Current question
	 * unanswered.</li>
	 * <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} Couldn't fetch question.</li>
	 * <li>{@link HttpStatus#I_AM_A_TEAPOT} If the {@link #TOTAL_ANONYMOUS_QUESTIONS
	 * maximum count of questions} has been reach for an anonymous session.</li>
	 * <li>{@link HttpStatus#BAD_REQUEST} No questions available.</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED} : {@link QuestionResponse}
	 */

	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "next", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> nextQuestion(HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();

		if (SCENARIO_QUESTIONS.count().run() == 0) {
			SpringUtils.badRequest(true, "No questions available.");
		}

		if (auth instanceof AnonymousAuthenticationToken) {
			final HashMap<String, Object> principal = (HashMap<String, Object>) SpringUtils.getContextPrincipal();

			final ScenarioGameData qgd = (ScenarioGameData) principal.get("scenario_game");
			SpringUtils.badRequest(qgd == null, "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));

			final Pair<Integer, ScenarioQuestionData> cq = qgd.nextQuestion();

			final int possibleAnswers = cq.getValue().getPossibleAnswers();

			InteractionMonitor.pushAccepted();

			principal.put("scenario_game_remaining", principal.get("scenario_game_remaining") == null ? TOTAL_ANONYMOUS_QUESTIONS : (int) principal.get("scenario_game_remaining") - 1);

			if ((int) principal.get("scenario_game_remaining") <= 0) {
				principal.remove("scenario_game");

				return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();
			} else {
				return ResponseEntity.accepted()
						.body(new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), possibleAnswers));

			}
		}

		final UserData ud = SpringUtils.needContextUser();

		SpringUtils.badRequest(!SCENARIO_GAME.hasGame(ud), "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));

		final ScenarioGameData qgd = SCENARIO_GAME.byUser(ud);

		final Pair<Integer, ScenarioQuestionData> cq = qgd.nextQuestion();

		// we copy the value here because QuestionData#getPossibleAnswers() is
		// non-deterministic
		final int possibleAnswers = cq.getValue().getPossibleAnswers();

		final String lang = ud.getLang();

		if (R2ApiMain.DEBUG) {
			LOGGER.info("needed: " + qgd.getQuestionData().getAnswerCards().toString());
			LOGGER.info("needed: " + qgd.getQuestionData().getAnswerCards().stream().map(c -> Integer.toString(c.getId())).collect(Collectors.joining(" ")));
			LOGGER.info("possible: " + MaterialCard.unwrap(possibleAnswers).toString());
			LOGGER.info("possible: " + MaterialCard.unwrap(possibleAnswers).stream().map(c -> Integer.toString(c.getId())).collect(Collectors.joining(" ")));
			LOGGER.info("language: " + lang + " (" + Locales.byCode(lang) + ")");
			LOGGER.info("question: " + cq.getValue().forLocale(Locales.byCode(lang)));
		}

		SCENARIO_GAME.updateScenarioGameData(qgd);

		InteractionMonitor.pushAccepted();

		return ResponseEntity.accepted()
				.body(new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), possibleAnswers));
	}

	/**
	 * Evaluates the client's answer and returns the correct answer<br>
	 * The client should request to proceed to the {@link #nextQuestion}.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} No game started.</li>
	 * <li>{@link HttpStatus#NOT_FOUND} User not found.</li>
	 * <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} Undefined game type.</li>
	 * <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} No current question.</li>
	 * <li>{@link HttpStatus#ACCEPTED} if the answer was correct,</li>
	 * <li>{@link HttpStatus#BAD_REQUEST} if the answer was incorrect,</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#BAD_REQUEST} / {@link HttpStatus#ACCEPTED} :
	 *         {@link AnswerResponse}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "answer", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> answerQuestion(@RequestParam int answer, HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			final HashMap<String, Object> principal = (HashMap<String, Object>) SpringUtils.getContextPrincipal();

			final ScenarioGameData qgd = (ScenarioGameData) principal.get("scenario_game");
			SpringUtils.badRequest(qgd == null, "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));

			final ScenarioQuestionData question = qgd.loadQuestionData();
			final Pair<Boolean, Integer> answerResp = qgd.handleAnswer(answer);

			InteractionMonitor.pushAccepted();

			if (answerResp.getKey()) {
				return ResponseEntity.accepted()
						.body(new AnswerResponse(question.getAnswers(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), question.answerForContextLocale()));
			} else {
				return ResponseEntity.badRequest()
						.body(new AnswerResponse(question.getAnswers(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), question.answerForContextLocale()));
			}
		}

		final UserData ud = SpringUtils.needContextUser();

		SpringUtils.badRequest(!SCENARIO_GAME.hasGame(ud), "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));

		final ScenarioGameData qgd = SCENARIO_GAME.byUser(ud);
		final ScenarioQuestionData question = qgd.loadQuestionData();

		final Pair<Boolean, Integer> answerResp = qgd.handleAnswer(answer);

		SCENARIO_GAME.updateScenarioGameData(qgd);

		InteractionMonitor.pushAccepted();

		SCENARIO_GAME_PROGRESS.updateScenarioGameProgressData(SCENARIO_GAME_PROGRESS.loadOrInsertByUser(ud).updateQuestion(qgd, answerResp.getValue(), answerResp.getKey()));

		if (answerResp.getKey()) {
			return ResponseEntity.accepted()
					.body(new AnswerResponse(question.getAnswers(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), question.answerForContextLocale()));
		} else {
			return ResponseEntity.badRequest()
					.body(new AnswerResponse(question.getAnswers(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(), question.answerForContextLocale()));
		}
	}

	/**
	 * <b>For logged in users:</b> Returns the current state of the game, even when
	 * there's no game ongoing.<br>
	 * <b>For anonymous sessions:</b> Returns the current state of the game, throws
	 * a {@link HttpStatus#BAD_REQUEST} if the session has no game attached to it..
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#BAD_REQUEST} No game started. (for anonymous sessions
	 * only)</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED} : {@link GameStateResponse}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "state", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> state(HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			final HashMap<String, Object> principal = (HashMap<String, Object>) SpringUtils.getContextPrincipal();
			final ScenarioGameData qgd = (ScenarioGameData) principal.get("scenario_game");

			SpringUtils.badRequest(qgd == null, "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));
			InteractionMonitor.pushAccepted();

			return ResponseEntity.accepted()
					.body(new GameStateResponse(qgd.getTotalQuestionCount(), qgd.getCorrectQuestionCount(), qgd.getStreakCount(), qgd.getCorrectMultiplier(), qgd.getCurrentPoints()));
		}

		final UserData ud = SpringUtils.needContextUser();

		if (!SCENARIO_GAME.hasGame(ud)) {
			InteractionMonitor.pushAcceptedDesc("No game started.");
			return ResponseEntity.accepted().body(new GameStateResponse());
		}

		final ScenarioGameData qgd = SCENARIO_GAME.byUser(ud);

		InteractionMonitor.pushAccepted();

		return ResponseEntity.accepted()
				.body(new GameStateResponse(qgd.getTotalQuestionCount(), qgd.getCorrectQuestionCount(), qgd.getStreakCount(), qgd.getCorrectMultiplier(), qgd.getCurrentPoints()));
	}

	/**
	 * <b>For logged in users:</b> Ends the current game. Doesn't throw an error if
	 * no game is ongoing.<br>
	 * <b>For anonymous sessions:</b> Throws a {@link HttpStatus#FORBIDDEN},
	 * anonymous users cannot end their attached game.
	 * 
	 * <br>
	 * <br>
	 * <b>Returns:</b>
	 * <ul>
	 * <li>{@link HttpStatus#FORBIDDEN} Anonymous user cannot end game. (for
	 * anonymous sessions only).</li>
	 * </ul>
	 * 
	 * @return {@link HttpStatus#ACCEPTED} : {@link GameStateResponse}
	 */
	@ExecutionTrack
	@AllowAnonymous
	@GetMapping(value = "end")
	public ResponseEntity<?> endGame(HttpServletRequest request) {
		final Authentication auth = SpringUtils.getContextAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			SpringUtils.forbidden(true, "Anonymous user cannot end game.");
			return null; // never reached
		}

		final UserData ud = SpringUtils.needContextUser();

		if (!SCENARIO_GAME.hasGame(ud)) {
			InteractionMonitor.pushAcceptedDesc("No game started.");
			return ResponseEntity.accepted().body(new GameStateResponse());
		}

		SCENARIO_GAME.endGame(ud);

		InteractionMonitor.pushAccepted();

		return ResponseEntity.accepted().body(new GameStateResponse());
	}

}
