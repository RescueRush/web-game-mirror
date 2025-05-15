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

import lu.rescue_rush.game_backend.data.MaterialCard;
import lu.rescue_rush.game_backend.db.data.game.scenario.ScenarioQuestionData;
import lu.rescue_rush.game_backend.db.data.user.UserPermissionTypeData;
import lu.rescue_rush.game_backend.db.tables.game.scenario.ScenarioQuestionTable;
import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.sb.component.annotation.NeedsPermission;
import lu.rescue_rush.game_backend.types.admin.A_QuestionScenarioTypes.InsertRequest;

@CrossOrigin
@RestController
@RequestMapping(value = "/admin/question/scenario")
public class A_QuestionScenarioEndpoints {

	public static final int PAGE_SIZE = 20;

	@Autowired
	private ScenarioQuestionTable QUESTIONS;

	@ExecutionTrack
	@NeedsPermission(value = { UserPermissionTypeData.KEY_QUESTIONS_INSERT, UserPermissionTypeData.KEY_QUESTIONS_ALL })
	@PostMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> insert(@CookieValue String token, @RequestBody InsertRequest request) {
		ScenarioQuestionData qd = QUESTIONS.safeInsert(request.description_LU, request.description_EN, request.description_FR, request.description_DE, request.answer_LU, request.answer_EN,
				request.answer_FR, request.answer_DE, MaterialCard.unwrap(request.answers).toArray(MaterialCard[]::new), request.points);
		return accepted().body(PCUtils.hashMap("id", qd.getId()));
	}

}
