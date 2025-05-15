package lu.rescue_rush.game_backend.db.data.game.scenario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.stream.Collectors;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.datastructure.pair.Pair;
import lu.pcy113.pclib.datastructure.pair.Pairs;
import lu.pcy113.pclib.db.DataBaseTable;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKey;
import lu.pcy113.pclib.db.annotations.entry.GeneratedKeyUpdate;
import lu.pcy113.pclib.db.annotations.entry.Reload;
import lu.pcy113.pclib.db.impl.SQLEntry;
import lu.pcy113.pclib.db.impl.SQLQuery;
import lu.pcy113.pclib.db.impl.SQLQueryable;
import lu.pcy113.pclib.db.utils.SQLBuilder;

import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.data.Localizable;
import lu.rescue_rush.game_backend.data.MaterialCard;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@GeneratedKey("id")
public class ScenarioQuestionData implements SQLEntry.SafeSQLEntry, Localizable {

	public static final float CORRECT_ANSWER_THRESHOLD = 0.5f;
	public static final int POSSIBLE_ANSWERS_NEEDED_COUNT = 6;

	private int id;
	private String description_LU, answer_LU;
	private String description_EN, answer_EN;
	private String description_FR, answer_FR;
	private String description_DE, answer_DE;
	private int answers;
	private int points;

	private List<MaterialCard> answerCards;

	public ScenarioQuestionData() {
	}

	public ScenarioQuestionData(int id) {
		this.id = id;
	}

	/**
	 * Use QuizQuestionTable#safeInsert( ... ) instead -> no duplicate questions
	 */
	@Deprecated
	public ScenarioQuestionData(String description_LU, String answer_LU, String description_EN, String answer_EN, String description_FR, String answer_FR, String description_DE, String answer_DE,
			MaterialCard[] answers, int points) {
		this.description_LU = description_LU;
		this.answer_LU = answer_LU;
		this.description_EN = description_EN;
		this.answer_EN = answer_EN;
		this.description_FR = description_FR;
		this.answer_FR = answer_FR;
		this.description_DE = description_DE;
		this.answer_DE = answer_DE;
		this.points = points;

		setAnswerCards(answers);
	}

	public Pair<Boolean, Float> handleAnswer(int answer) {
		List<MaterialCard> givenCards = MaterialCard.unwrap(answer);
		int correctCards = 0;

		for (MaterialCard givenCard : givenCards) {
			if (answerCards.contains(givenCard)) {
				correctCards++;
			} else {
				correctCards--;
			}
		}

		float ratio = (float) correctCards / answerCards.size();

		return Pairs.readOnly(ratio >= CORRECT_ANSWER_THRESHOLD, PCUtils.clampGreaterOrEquals(0f, ratio));
	}

	public void setAnswerCards(MaterialCard[] cards) {
		this.answers = 0;
		for (MaterialCard card : cards) {
			this.answers |= 1 << card.getId();
		}
		loadAnswerCards();
	}

	public List<MaterialCard> loadAnswerCards() {
		return answerCards = MaterialCard.unwrap(answers);
	}

	public List<MaterialCard> getAnswerCards() {
		return answerCards;
	}

	public int getPossibleAnswers() {
		return getPossibleAnswers(answers);
	}

	public static int getPossibleAnswers(int answers) {
		final int start = 0, end = MaterialCard.values().length - 1, neededRandom = POSSIBLE_ANSWERS_NEEDED_COUNT - MaterialCard.computeCardsCount(answers, MaterialCard.values().length);

		List<Integer> numbers = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			if ((answers & (1 << i)) != 0) { // exclude needed numbers from list
				continue;
			}
			numbers.add(i);
		}

		Collections.shuffle(numbers);

		numbers = numbers.subList(0, neededRandom);

		return answers | MaterialCard.wrap(numbers);
	}

	@GeneratedKeyUpdate
	public void updateGeneratedKeys(ResultSet rs) throws SQLException {
		id = rs.getInt(1);
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		description_LU = rs.getString("desc_LU");
		description_EN = rs.getString("desc_EN");
		description_FR = rs.getString("desc_FR");
		description_DE = rs.getString("desc_DE");
		answer_LU = rs.getString("answer_LU");
		answer_EN = rs.getString("answer_EN");
		answer_FR = rs.getString("answer_FR");
		answer_DE = rs.getString("answer_DE");
		answers = rs.getInt("answers");
		points = rs.getInt("points");

		loadAnswerCards();
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "desc_LU", "answer_LU", "desc_EN", "answer_EN", "desc_FR", "answer_FR", "desc_DE", "answer_DE", "answers", "points" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "desc_LU", "answer_LU", "desc_EN", "answer_EN", "desc_FR", "answer_FR", "desc_DE", "answer_DE", "answers", "points" },
				new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedDeleteSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeDelete(table, new String[] { "id" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedSelectSQL(SQLQueryable<T> table) {
		return SQLBuilder.safeSelect(table, new String[] { "id" });
	}

	@Override
	public void prepareInsertSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, description_LU);
		stmt.setString(2, answer_LU);
		stmt.setString(3, description_EN);
		stmt.setString(4, answer_EN);
		stmt.setString(5, description_FR);
		stmt.setString(6, answer_FR);
		stmt.setString(7, description_DE);
		stmt.setString(8, answer_DE);

		stmt.setInt(9, answers);
		stmt.setInt(10, points);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, description_LU);
		stmt.setString(2, answer_LU);
		stmt.setString(3, description_EN);
		stmt.setString(4, answer_EN);
		stmt.setString(5, description_FR);
		stmt.setString(6, answer_FR);
		stmt.setString(7, description_DE);
		stmt.setString(8, answer_DE);

		stmt.setInt(9, answers);
		stmt.setInt(10, points);

		stmt.setInt(11, id);
	}

	@Override
	public void prepareDeleteSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public void prepareSelectSQL(PreparedStatement stmt) throws SQLException {
		stmt.setInt(1, id);
	}

	@Override
	public ScenarioQuestionData clone() {
		return new ScenarioQuestionData();
	}

	public int getId() {
		return id;
	}

	public String getDescription_LU() {
		return description_LU;
	}

	public void setDescription_LU(String description_LU) {
		this.description_LU = description_LU;
	}

	public String getDescription_EN() {
		return description_EN;
	}

	public void setDescription_EN(String description_EN) {
		this.description_EN = description_EN;
	}

	public String getDescription_FR() {
		return description_FR;
	}

	public void setDescription_FR(String description_FR) {
		this.description_FR = description_FR;
	}

	public String getDescription_DE() {
		return description_DE;
	}

	public void setDescription_DE(String description_DE) {
		this.description_DE = description_DE;
	}

	public int getAnswers() {
		return answers;
	}

	public void setAnswers(int answers) {
		this.answers = answers;
	}

	public String getAnswer_LU() {
		return answer_LU;
	}

	public void setAnswer_LU(String answer_LU) {
		this.answer_LU = answer_LU;
	}

	public String getAnswer_EN() {
		return answer_EN;
	}

	public void setAnswer_EN(String answer_EN) {
		this.answer_EN = answer_EN;
	}

	public String getAnswer_FR() {
		return answer_FR;
	}

	public void setAnswer_FR(String answer_FR) {
		this.answer_FR = answer_FR;
	}

	public String getAnswer_DE() {
		return answer_DE;
	}

	public void setAnswer_DE(String answer_DE) {
		this.answer_DE = answer_DE;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@Override
	public String toString() {
		return "QuizQuestionData [id=" + id + ", description_LU=" + description_LU + ", answer_LU=" + answer_LU + ", description_EN=" + description_EN + ", answer_EN=" + answer_EN
				+ ", description_FR=" + description_FR + ", answer_FR=" + answer_FR + ", description_DE=" + description_DE + ", answer_DE=" + answer_DE + ", answers=" + answers + ", points=" + points
				+ ", answerCards=" + answerCards + "]";
	}

	@Override
	public String forLocale(Locale lang) {
		switch (Language.byLocale(lang)) {
		case ENGLISH:
			return description_EN;
		case FRENCH:
			return description_FR;
		case LUXEMBOURISH:
			return description_LU;
		case GERMAN:
			return description_DE;
		default:
			return null;
		}
	}

	public String forContextLocale() {
		return forLocale(SpringUtils.getContextLocale());
	}

	public String answerForLocale(Locale lang) {
		switch (Language.byLocale(lang)) {
		case ENGLISH:
			return answer_EN;
		case FRENCH:
			return answer_FR;
		case LUXEMBOURISH:
			return answer_LU;
		case GERMAN:
			return answer_DE;
		default:
			return null;
		}
	}

	public String answerForContextLocale() {
		return answerForLocale(SpringUtils.getContextLocale());
	}

	public static SQLQuery<ScenarioQuestionData> byAnyLocale(String qLu, String qEn, String qFr, String qDe) {
		return new SQLQuery.SafeSQLQuery<ScenarioQuestionData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<ScenarioQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE `desc_LU` = ? OR `desc_EN` = ? OR `desc_FR` = ? OR `desc_DE` = ?;";
			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, qLu);
				stmt.setString(2, qEn);
				stmt.setString(3, qFr);
				stmt.setString(4, qDe);
			}

			@Override
			public ScenarioQuestionData clone() {
				return new ScenarioQuestionData();
			}
		};
	}

	public static SQLQuery<ScenarioQuestionData> byRange(int min, int max) {
		return new SQLQuery.UnsafeSQLQuery<ScenarioQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<ScenarioQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE id >= " + min + " AND id <" + max + ";";
			}

			@Override
			public ScenarioQuestionData clone() {
				return new ScenarioQuestionData();
			}
		};
	}

	public static SQLQuery<ScenarioQuestionData> getAll() {
		return new SQLQuery.UnsafeSQLQuery<ScenarioQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<ScenarioQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + ";";
			}

			@Override
			public ScenarioQuestionData clone() {
				return new ScenarioQuestionData();
			}
		};
	}

	@Deprecated
	public static SQLQuery<ScenarioQuestionData> random() {
		return new SQLQuery.UnsafeSQLQuery<ScenarioQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<ScenarioQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " ORDER BY RAND() LIMIT 1;";
			}

			@Override
			public ScenarioQuestionData clone() {
				return new ScenarioQuestionData();
			}
		};
	}

	public static SQLQuery<ScenarioQuestionData> random(Queue<Integer> notIds) {
		return new SQLQuery.UnsafeSQLQuery<ScenarioQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<ScenarioQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " "
						+ (notIds == null || notIds.isEmpty() ? "" : "WHERE `id` NOT IN (" + notIds.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ") ")
						+ " ORDER BY RAND() LIMIT 1;";
			}

			@Override
			public ScenarioQuestionData clone() {
				return new ScenarioQuestionData();
			}
		};
	}

	public static SQLQuery<ScenarioQuestionData> byOffset(int offset, int limit) {
		return new SQLQuery.UnsafeSQLQuery<ScenarioQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<ScenarioQuestionData> table) {
				String query = SQLBuilder.safeSelect(table, null, limit);
				return new StringBuilder(query).insert(query.length() - 1, " OFFSET " + offset).toString();
			}

			@Override
			public ScenarioQuestionData clone() {
				return new ScenarioQuestionData();
			}
		};
	}

}
