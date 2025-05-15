package lu.rescue_rush.game_backend.db.data.game.guess;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

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
import lu.rescue_rush.game_backend.types.game.GameQuizTypes;
import lu.rescue_rush.game_backend.types.game.GameQuizTypes.QuestionResponse.QuestionEntry;
import lu.rescue_rush.game_backend.utils.SpringUtils;

@GeneratedKey("id")
public class QuizQuestionData implements SQLEntry.SafeSQLEntry, Localizable {

	private static final Logger LOGGER = Logger.getLogger(QuizQuestionData.class.getName());

	private int id;
	private String description_LU, description_EN, description_FR, description_DE;
	private JSONObject answer_LU, answer_EN, answer_FR, answer_DE;
	private int answer, level, points;

	public QuizQuestionData() {
	}

	public QuizQuestionData(int id) {
		this.id = id;
	}

	/**
	 * Use GuessQuestionTable#safeInsert( ... ) instead -> no duplicate questions
	 */
	@Deprecated
	public QuizQuestionData(String description_LU, String description_EN, String description_FR, String description_DE, JSONObject answer_LU, JSONObject answer_EN, JSONObject answer_FR,
			JSONObject answer_DE, int answer, int level, int points) {
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

	@GeneratedKeyUpdate(type = GeneratedKeyUpdate.Type.INDEX)
	public void updateGeneratedKeys(BigInteger bigint) throws SQLException {
		id = bigint.intValue();
	}

	@Reload
	public void reload(ResultSet rs) throws SQLException {
		id = rs.getInt("id");

		description_LU = rs.getString("desc_LU");
		description_EN = rs.getString("desc_EN");
		description_FR = rs.getString("desc_FR");
		description_DE = rs.getString("desc_DE");

		answer_LU = new JSONObject(rs.getString("answer_LU"));
		answer_EN = new JSONObject(rs.getString("answer_EN"));
		answer_FR = new JSONObject(rs.getString("answer_FR"));
		answer_DE = new JSONObject(rs.getString("answer_DE"));

		answer = rs.getInt("answer");
		level = rs.getInt("level");
		points = rs.getInt("points");
	}

	@Override
	public <T extends SQLEntry> String getPreparedInsertSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeInsert(table, new String[] { "desc_LU", "answer_LU", "desc_EN", "answer_EN", "desc_FR", "answer_FR", "desc_DE", "answer_DE", "answer", "level", "points" });
	}

	@Override
	public <T extends SQLEntry> String getPreparedUpdateSQL(DataBaseTable<T> table) {
		return SQLBuilder.safeUpdate(table, new String[] { "desc_LU", "answer_LU", "desc_EN", "answer_EN", "desc_FR", "answer_EN", "desc_DE", "answer_DE", "answer", "level", "points" },
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
		stmt.setString(2, answer_LU.toString());
		stmt.setString(3, description_EN);
		stmt.setString(4, answer_EN.toString());
		stmt.setString(5, description_FR);
		stmt.setString(6, answer_FR.toString());
		stmt.setString(7, description_DE);
		stmt.setString(8, answer_DE.toString());

		stmt.setInt(9, answer);
		stmt.setInt(10, level);
		stmt.setInt(11, points);
	}

	@Override
	public void prepareUpdateSQL(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, description_LU);
		stmt.setString(2, answer_LU.toString());
		stmt.setString(3, description_EN);
		stmt.setString(4, answer_EN.toString());
		stmt.setString(5, description_FR);
		stmt.setString(6, answer_FR.toString());
		stmt.setString(7, description_DE);
		stmt.setString(8, answer_DE.toString());

		stmt.setInt(9, answer);
		stmt.setInt(10, level);
		stmt.setInt(11, points);

		stmt.setInt(12, id);
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
	public QuizQuestionData clone() {
		return new QuizQuestionData();
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

	public JSONObject getAnswer_LU() {
		return answer_LU;
	}

	public void setAnswer_LU(JSONObject answer_LU) {
		this.answer_LU = answer_LU;
	}

	public JSONObject getAnswer_EN() {
		return answer_EN;
	}

	public void setAnswer_EN(JSONObject answer_EN) {
		this.answer_EN = answer_EN;
	}

	public JSONObject getAnswer_FR() {
		return answer_FR;
	}

	public void setAnswer_FR(JSONObject answer_FR) {
		this.answer_FR = answer_FR;
	}

	public JSONObject getAnswer_DE() {
		return answer_DE;
	}

	public void setAnswer_DE(JSONObject answer_DE) {
		this.answer_DE = answer_DE;
	}

	public int getAnswer() {
		return answer;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	@Override
	public String toString() {
		return "GuessQuestionData [id=" + id + ", description_LU=" + description_LU + ", description_EN=" + description_EN + ", description_FR=" + description_FR + ", description_DE=" + description_DE
				+ ", answer_LU=" + answer_LU + ", answer_EN=" + answer_EN + ", answer_FR=" + answer_FR + ", answer_DE=" + answer_DE + ", answer=" + answer + ", level=" + level + ", points=" + points
				+ "]";
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
			return answer_EN.getString(Integer.toString(answer));
		case FRENCH:
			return answer_FR.getString(Integer.toString(answer));
		case LUXEMBOURISH:
			return answer_LU.getString(Integer.toString(answer));
		case GERMAN:
			return answer_DE.getString(Integer.toString(answer));
		default:
			return null;
		}
	}

	public String answerForContextLocale() {
		return answerForLocale(SpringUtils.getContextLocale());
	}

	public List<String> getPossibleAnswersForLocale(Locale lang) {
		switch (Language.byLocale(lang)) {
		case ENGLISH:
			return answer_EN.keySet().stream().map(answer_EN::getString).collect(Collectors.toList());
		case FRENCH:
			return answer_FR.keySet().stream().map(answer_FR::getString).collect(Collectors.toList());
		case LUXEMBOURISH:
			return answer_LU.keySet().stream().map(answer_LU::getString).collect(Collectors.toList());
		case GERMAN:
			return answer_DE.keySet().stream().map(answer_DE::getString).collect(Collectors.toList());
		default:
			return null;
		}
	}

	public List<GameQuizTypes.QuestionResponse.QuestionEntry> getPossibleAnswersForLocale(QuizGameData ggd, Locale lang) {
		switch (Language.byLocale(lang)) {
		case ENGLISH:
			return answer_EN.keySet().stream().map(k -> new QuestionEntry(computeHash(ggd.getQuestionOffset() + id + k), answer_EN.getString(k))).collect(Collectors.toList());
		case FRENCH:
			return answer_FR.keySet().stream().map(k -> new QuestionEntry(computeHash(ggd.getQuestionOffset() + id + k), answer_FR.getString(k))).collect(Collectors.toList());
		case LUXEMBOURISH:
			return answer_LU.keySet().stream().map(k -> new QuestionEntry(computeHash(ggd.getQuestionOffset() + id + k), answer_LU.getString(k))).collect(Collectors.toList());
		case GERMAN:
			return answer_DE.keySet().stream().map(k -> new QuestionEntry(computeHash(ggd.getQuestionOffset() + id + k), answer_DE.getString(k))).collect(Collectors.toList());
		default:
			return null;
		}
	}

	public List<GameQuizTypes.QuestionResponse.QuestionEntry> getPossibleAnswersForContextLocale(QuizGameData ggd) {
		return getPossibleAnswersForLocale(ggd, SpringUtils.getContextLocale());
	}

	public String getCorrectAnswerForLocale(QuizGameData qgd, Locale lang) {
		JSONObject obj = switch (Language.byLocale(lang)) {
		case ENGLISH -> answer_EN;
		case FRENCH -> answer_FR;
		case LUXEMBOURISH -> answer_LU;
		case GERMAN -> answer_DE;
		};
		if (!obj.has(Integer.toString(answer))) {
			LOGGER.warning("[" + this.id + "] No answer '" + answer + "' in: " + obj);
		}
		return obj.getString(Integer.toString(answer));
	}

	public String getCorrectAnswerHash(QuizGameData qgd) {
		return computeHash(qgd.getQuestionOffset() + qgd.getCurrentQuestionId() + Integer.toString(this.answer));
	}

	public static String computeHash(String value) {
		return SpringUtils.hash(value).substring(0, 10);
	}

	public static SQLQuery<QuizQuestionData> byAnyLocale(String qLu, String qEn, String qFr, String qDe) {
		return new SQLQuery.SafeSQLQuery<QuizQuestionData>() {
			@Override
			public String getPreparedQuerySQL(SQLQueryable<QuizQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE (" + "`desc_LU` = ? AND `desc_LU` != 'NULL NO VALUE FOUND') OR "
						+ "(`desc_EN` = ? AND `desc_EN` != 'NULL NO VALUE FOUND') OR (`desc_FR` = ? AND `desc_FR` != 'NULL NO VALUE FOUND') OR "
						+ "(`desc_DE` = ? AND `desc_DE` != 'NULL NO VALUE FOUND');";

			}

			@Override
			public void updateQuerySQL(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, qLu);
				stmt.setString(2, qEn);
				stmt.setString(3, qFr);
				stmt.setString(4, qDe);
			}

			@Override
			public QuizQuestionData clone() {
				return new QuizQuestionData();
			}
		};
	}

	public static SQLQuery<QuizQuestionData> byRange(int min, int max) {
		return new SQLQuery.UnsafeSQLQuery<QuizQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<QuizQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " WHERE id >= " + min + " AND id <" + max + ";";
			}

			@Override
			public QuizQuestionData clone() {
				return new QuizQuestionData();
			}
		};
	}

	public static SQLQuery<QuizQuestionData> getAll() {
		return new SQLQuery.UnsafeSQLQuery<QuizQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<QuizQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + ";";
			}

			@Override
			public QuizQuestionData clone() {
				return new QuizQuestionData();
			}
		};
	}

	@Deprecated
	public static SQLQuery<QuizQuestionData> random() {
		return new SQLQuery.UnsafeSQLQuery<QuizQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<QuizQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " ORDER BY RAND() LIMIT 1;";
			}

			@Override
			public QuizQuestionData clone() {
				return new QuizQuestionData();
			}
		};
	}

	public static SQLQuery<QuizQuestionData> random(Queue<Integer> notIds) {
		return new SQLQuery.UnsafeSQLQuery<QuizQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<QuizQuestionData> table) {
				return "SELECT * FROM " + table.getQualifiedName() + " " + (notIds.size() <= 1 ? (notIds.isEmpty() ? "" : "WHERE `id` != " + notIds.peek())
						: "WHERE `id` NOT IN (" + notIds.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ") ") + " ORDER BY RAND() LIMIT 1;";
			}

			@Override
			public QuizQuestionData clone() {
				return new QuizQuestionData();
			}
		};
	}

	public static SQLQuery<QuizQuestionData> byOffset(int offset, int limit) {
		return new SQLQuery.UnsafeSQLQuery<QuizQuestionData>() {
			@Override
			public String getQuerySQL(SQLQueryable<QuizQuestionData> table) {
				String query = SQLBuilder.safeSelect(table, null, limit);
				return new StringBuilder(query).insert(query.length() - 1, " OFFSET " + offset).toString();
			}

			@Override
			public QuizQuestionData clone() {
				return new QuizQuestionData();
			}
		};
	}

}
