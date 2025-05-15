package lu.rescue_rush.game_backend.types.game;

import java.util.List;

public final class GameQuizTypes {

	public static class GameStateResponse {

		public boolean exists;
		public QuestionResponse question;

		public GameStateResponse() {
		}

		public GameStateResponse(boolean exists) {
			this.exists = exists;
		}

		public GameStateResponse(boolean exists, QuestionResponse question) {
			this.exists = exists;
			this.question = question;
		}

	}

	public static class QuestionResponse {

		public int id;
		public String desc;
		public float multiplier;
		public int streak;
		public int points;
		public List<QuestionEntry> possibleAnswers;
		public long remainingTime; // ms

		public QuestionResponse() {
		}

		public QuestionResponse(int id, String desc, float multiplier, int streak, int points, List<QuestionEntry> possibleAnswers, long remainingTime) {
			this.id = id;
			this.desc = desc;
			this.multiplier = multiplier;
			this.streak = streak;
			this.points = points;
			this.possibleAnswers = possibleAnswers;
			this.remainingTime = remainingTime;
		}

		public static class QuestionEntry {

			public String code, question;

			public QuestionEntry() {
			}

			public QuestionEntry(String code, String question) {
				this.code = code;
				this.question = question;
			}

			@Override
			public String toString() {
				return "QuestionEntry [code=" + code + ", question=" + question + "]";
			}

		}

	}

	public static class AnswerRequest {

		public String answer;

		public AnswerRequest() {
		}

		public AnswerRequest(String answer) {
			this.answer = answer;
		}

	}

	public static class AnswerResponse {

		public boolean success;
		public String answer;
		public float multiplier;
		public int streak;
		public int points;

		public AnswerResponse() {
		}

		public AnswerResponse(boolean success, String answer, float multiplier, int streak, int points) {
			this.success = success;
			this.answer = answer;
			this.multiplier = multiplier;
			this.streak = streak;
			this.points = points;
		}

	}

}
