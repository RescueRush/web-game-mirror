package lu.rescue_rush.game_backend.types.admin;

import com.fasterxml.jackson.annotation.JsonAlias;

public final class A_QuestionScenarioTypes {

	public static class InsertRequest {

		@JsonAlias("desc_LU")
		public String description_LU;
		@JsonAlias("desc_EN")
		public String description_EN;
		@JsonAlias("desc_FR")
		public String description_FR;
		@JsonAlias("desc_DE")
		public String description_DE;

		public String answer_LU;
		public String answer_EN;
		public String answer_FR;
		public String answer_DE;

		public int answers;

		public int points;

		public InsertRequest() {
		}

		public InsertRequest(String description_LU, String description_EN, String description_FR, String description_DE, String answer_LU, String answer_EN, String answer_FR, String answer_DE,
				int answers, int points) {
			this.description_LU = description_LU;
			this.description_EN = description_EN;
			this.description_FR = description_FR;
			this.description_DE = description_DE;

			this.answer_LU = answer_LU;
			this.answer_EN = answer_EN;
			this.answer_FR = answer_FR;
			this.answer_DE = answer_DE;

			this.answers = answers;
			this.points = points;
		}

		@Override
		public String toString() {
			return "InsertRequest [description_LU=" + description_LU + ", description_EN=" + description_EN + ", description_FR=" + description_FR + ", description_DE=" + description_DE
					+ ", answer_LU=" + answer_LU + ", answer_EN=" + answer_EN + ", answer_FR=" + answer_FR + ", answer_DE=" + answer_DE + ", answers=" + answers + ", points=" + points + "]";
		}

	}

}
