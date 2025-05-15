package lu.rescue_rush.game_backend.ws.game;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.datastructure.pair.Pair;
import lu.pcy113.pclib.datastructure.triplet.Triplet;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.db.data.game.guess.QuizGameData;
import lu.rescue_rush.game_backend.db.data.game.guess.QuizGameProgressData;
import lu.rescue_rush.game_backend.db.data.game.guess.QuizQuestionData;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizGameProgressTable;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizGameTable;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizQuestionTable;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.sb.ws.R2WSHandler;
import lu.rescue_rush.game_backend.sb.ws.R2WebSocketHandler.WebSocketSessionData;
import lu.rescue_rush.game_backend.sb.ws.WSMapping;
import lu.rescue_rush.game_backend.sb.ws.WSResponseMapping;
import lu.rescue_rush.game_backend.types.game.GameQuizTypes;
import lu.rescue_rush.game_backend.types.game.GameQuizTypes.AnswerResponse;
import lu.rescue_rush.game_backend.types.game.GameQuizTypes.GameStateResponse;
import lu.rescue_rush.game_backend.types.game.GameQuizTypes.QuestionResponse;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@Service
@WSMapping(path = "/game/quiz")
public class GameQuizWS extends R2WSHandler {

	private static final Logger LOGGER = Logger.getLogger(GameQuizWS.class.getName());

	private static final String TID_ANSWER_TIMEOUT = "/answer-timeout";

	@Autowired
	private QuizGameTable QUIZ_GAME;

	@Autowired
	private QuizQuestionTable QUIZ_QUESTIONS;

	@Autowired
	private QuizGameProgressTable QUIZ_GAME_PROGRESS;

	@WSMapping(path = "/ping")
	public String test2(WebSocketSessionData sessionData, String message) {
		return "Pong (" + message + ") !";
	}

	@ExecutionTrack
	@WSMapping(path = "/new")
	@WSResponseMapping(path = "/next")
	public QuestionResponse newGame(WebSocketSessionData sessionData) {
		final UserData ud = SpringUtils.needContextUser();
		final Language lang = SpringUtils.getContextLanguage();
		SpringUtils.badRequest(QUIZ_QUESTIONS.count().run() == 0, "No questions available.");

		if (QUIZ_GAME.hasGame(ud)) {
			QUIZ_GAME.endGame(ud);
		}

		final QuizGameData qgd = QUIZ_GAME.create(ud);
		final QuizGameProgressData qgpd = QUIZ_GAME_PROGRESS.updateQuizGameProgressData(QUIZ_GAME_PROGRESS.loadOrInsertByUser(ud).startGame(qgd));

		InteractionMonitor.pushAccepted();

		final Pair<Integer, QuizQuestionData> cq = qgd.nextQuestion();

		QUIZ_GAME.updateQuizGameData(qgd);

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Correct answer: " + cq.getValue().getCorrectAnswerForLocale(qgd, SpringUtils.getContextLocale()) + " (" + cq.getValue().getCorrectAnswerHash(qgd) + ")");
		}

		scheduleAnswerTimeoutTask(sessionData);

		return new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(),
				cq.getValue().getPossibleAnswersForContextLocale(qgd), qgd.getRemainingQuestionTime());
	}

	@ExecutionTrack
	@WSMapping(path = "/answer")
	public GameQuizTypes.AnswerResponse answer(WebSocketSessionData sessionData, GameQuizTypes.AnswerRequest request) {
		final UserData ud = SpringUtils.needContextUser();
		final Locale lang = SpringUtils.getContextLocale();
		SpringUtils.badRequest(!QUIZ_GAME.hasGame(ud), "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));

		clearScheduledTasks(sessionData, TID_ANSWER_TIMEOUT);

		final QuizGameData qgd = QUIZ_GAME.byUser(ud);
		final QuizQuestionData question = qgd.loadQuestionData();
		final Triplet<Boolean, String, Integer> answerResp = qgd.handleAnswer(request.answer);

		QUIZ_GAME.updateQuizGameData(qgd);
		QUIZ_GAME_PROGRESS.updateQuizGameProgressData(QUIZ_GAME_PROGRESS.loadOrInsertByUser(ud).updateQuestion(qgd, answerResp.getThird(), answerResp.getFirst()));

		InteractionMonitor.pushAccepted();

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Correct sent answer: " + answerResp.getSecond());
			LOGGER.info("Points added=total: " + answerResp.getThird() + " = " + qgd.getCurrentPoints());
		}

		return new AnswerResponse(answerResp.getFirst(), answerResp.getSecond(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints());
	}

	@ExecutionTrack
	@WSMapping(path = "/next")
	public GameQuizTypes.QuestionResponse next(WebSocketSessionData sessionData) {
		final UserData ud = SpringUtils.needContextUser();
		SpringUtils.badRequest(!QUIZ_GAME.hasGame(ud), "No game started.", () -> InteractionMonitor.pushDeniedDesc("No game started."));

		clearScheduledTasks(sessionData, TID_ANSWER_TIMEOUT);

		final QuizGameData qgd = QUIZ_GAME.byUser(ud);
		final Pair<Integer, QuizQuestionData> cq = qgd.nextQuestion();

		QUIZ_GAME.updateQuizGameData(qgd);
		InteractionMonitor.pushAccepted();

		if (R2ApiMain.DEBUG) {
			LOGGER.info("Correct answer: " + cq.getValue().getCorrectAnswerForLocale(qgd, SpringUtils.getContextLocale()) + " (" + cq.getValue().getCorrectAnswerHash(qgd) + ")");
		}

		scheduleAnswerTimeoutTask(sessionData);

		return new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(),
				cq.getValue().getPossibleAnswersForContextLocale(qgd), qgd.getRemainingQuestionTime());
	}

	private void scheduleAnswerTimeoutTask(WebSocketSessionData sessionData) {
		final QuizGameData qgdn = QUIZ_GAME.byUser(sessionData.getUser());

		scheduleTask(sessionData, () -> {
			final UserData ud = sessionData.getUser();

			if (!QUIZ_GAME.hasGame(ud)) {
				// no active game: task timed out for some reason
				LOGGER.warning("Game for user: " + ud + " timed out.");
				return;
			}

			final QuizGameData qgd = QUIZ_GAME.byUser(ud);
			final Triplet<Boolean, String, Integer> answerResp = qgd.handleAnswer(null); // handle as a failed answer, reset state, etc
			QUIZ_GAME.updateQuizGameData(qgd);
			QUIZ_GAME_PROGRESS.updateQuizGameProgressData(QUIZ_GAME_PROGRESS.loadOrInsertByUser(ud).updateQuestion(qgd, 0, false));

			if (R2ApiMain.DEBUG) {
				LOGGER.warning("Send game timeout to user: " + ud);
			}
			send(sessionData, TID_ANSWER_TIMEOUT, new AnswerResponse(answerResp.getFirst(), answerResp.getSecond(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints()));
		}, TID_ANSWER_TIMEOUT, qgdn.getRemainingQuestionTime(), TimeUnit.MILLISECONDS);
	}

	@ExecutionTrack
	@WSMapping(path = "/recover")
	public GameStateResponse recoverGame(WebSocketSessionData sessionData) {
		final UserData ud = SpringUtils.needContextUser();
		final Language lang = SpringUtils.getContextLanguage();
		SpringUtils.badRequest(QUIZ_QUESTIONS.count().run() == 0, "No questions available.");

		if (!QUIZ_GAME.hasGame(ud)) {
			InteractionMonitor.pushDeniedDesc("No game.");
			return new GameStateResponse(false);
		}

		final QuizGameData qgd = QUIZ_GAME.byUser(ud);

		InteractionMonitor.pushAccepted();

		if (qgd.getCurrentQuestionId() == -1) {
			return new GameStateResponse(true, null);
		} else {
			final Pair<Integer, QuizQuestionData> cq = qgd.currentQuestion();

			return new GameStateResponse(true, new QuestionResponse(cq.getKey(), cq.getValue().forContextLocale(), qgd.getCorrectMultiplier(), qgd.getStreakCount(), qgd.getCurrentPoints(),
					cq.getValue().getPossibleAnswersForContextLocale(qgd), qgd.getRemainingQuestionTime()));
		}
	}

}
