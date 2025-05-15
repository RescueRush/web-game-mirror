package lu.rescue_rush.game_backend.types.admin;

import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonAlias;

public final class A_QuestionQuizTypes {

	public static class InsertRequest {

		@JsonAlias("desc_LU")
		public String description_LU;
		@JsonAlias("desc_EN")
		public String description_EN;
		@JsonAlias("desc_FR")
		public String description_FR;
		@JsonAlias("desc_DE")
		public String description_DE;

		public Map<String, String> answer_LU;
		public Map<String, String> answer_EN;
		public Map<String, String> answer_FR;
		public Map<String, String> answer_DE;

		public int answer;
		public int level;
		public int points;

		public InsertRequest() {
		}

		public InsertRequest(String description_LU, String description_EN, String description_FR, String description_DE, Map<String, String> answer_LU, Map<String, String> answer_EN,
				Map<String, String> answer_FR, Map<String, String> answer_DE, int answer, int level, int points) {
			this.description_LU = description_LU;
			this.description_EN = description_EN;
			this.description_FR = description_FR;
			this.description_DE = description_DE;

			this.answer_LU = answer_LU;
			this.answer_EN = answer_EN;
			this.answer_FR = answer_FR;
			this.answer_DE = answer_DE;

			this.answer = answer;
			this.level = level;
			this.points = points;
		}

		public JSONObject getAnswerLU() {
			return new JSONObject(answer_LU);
		}

		public JSONObject getAnswerEN() {
			return new JSONObject(answer_EN);
		}

		public JSONObject getAnswerFR() {
			return new JSONObject(answer_FR);
		}

		public JSONObject getAnswerDE() {
			return new JSONObject(answer_DE);
		}

		@Override
		public String toString() {
			return "InsertRequest [description_LU=" + description_LU + ", description_EN=" + description_EN + ", description_FR=" + description_FR + ", description_DE=" + description_DE
					+ ", answer_LU=" + answer_LU + ", answer_EN=" + answer_EN + ", answer_FR=" + answer_FR + ", answer_DE=" + answer_DE + ", answer=" + answer + ", level=" + level + ", points="
					+ points + "]";
		}

	}

}
