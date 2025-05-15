package lu.rescue_rush.game_backend.endpoints.admin;

import static org.springframework.http.ResponseEntity.accepted;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lu.pcy113.pclib.PCUtils;

import lu.rescue_rush.game_backend.db.data.game.guess.QuizQuestionData;
import lu.rescue_rush.game_backend.db.data.user.UserPermissionTypeData;
import lu.rescue_rush.game_backend.db.tables.game.quiz.QuizQuestionTable;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.sb.component.annotation.NeedsPermission;
import lu.rescue_rush.game_backend.types.admin.A_QuestionQuizTypes.InsertRequest;

@CrossOrigin
@RestController
@RequestMapping(value = "/admin/question/quiz")
public class A_QuestionQuizEndpoints {

	public static final int PAGE_SIZE = 20;

	@Autowired
	private QuizQuestionTable QUESTIONS;

	@ExecutionTrack
	@NeedsPermission(value = { UserPermissionTypeData.KEY_QUESTIONS_INSERT, UserPermissionTypeData.KEY_QUESTIONS_ALL })
	@PostMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> insert(@CookieValue String token, @RequestBody InsertRequest request) {
		QuizQuestionData qd = QUESTIONS.safeInsert(request.description_LU, request.description_EN, request.description_FR, request.description_DE, request.getAnswerLU(), request.getAnswerEN(),
				request.getAnswerFR(), request.getAnswerDE(), request.answer, request.level, request.points);
		return accepted().body(PCUtils.hashMap("id", qd.getId()));
	}

}
