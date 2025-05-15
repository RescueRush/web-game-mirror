package lu.rescue_rush.game_backend.types.game;

public final class GameScenarioTypes {

	public static class GameStateResponse {
		public int total;
		public int correct;
		public int streak;
		public float multiplier;
		public int points;
		public long remainingTime;

		public GameStateResponse(int total, int correct, int streak, float multiplier, int points, long remainingTime) {
			this.total = total;
			this.correct = correct;
			this.streak = streak;
			this.multiplier = multiplier;
			this.points = points;
			this.remainingTime = remainingTime;
		}

		public GameStateResponse(int total, int correct, int streak, float multiplier, int points) {
			this(total, correct, streak, multiplier, points, 0);
		}

		public GameStateResponse() {
		}

	}

	public static class QuestionResponse {
		public int id;
		public String desc;
		public float multiplier;
		public int streak;
		public int points;
		public int possibleAnswers;

		public QuestionResponse(int id, String desc, float multiplier, int streak, int points, int possibleAnswers) {
			this.id = id;
			this.desc = desc;
			this.multiplier = multiplier;
			this.streak = streak;
			this.points = points;
			this.possibleAnswers = possibleAnswers;
		}

	}

	public static class AnswerResponse {

		public int answer;
		public float multiplier;
		public int streak;
		public int points;
		public String text_answer;

		public AnswerResponse(int answer, float multiplier, int streak, int points, String text_answer) {
			this.answer = answer;
			this.multiplier = multiplier;
			this.streak = streak;
			this.points = points;
			this.text_answer = text_answer;
		}

	}
}
