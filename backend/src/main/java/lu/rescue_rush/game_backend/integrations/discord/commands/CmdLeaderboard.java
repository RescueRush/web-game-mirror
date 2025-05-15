package lu.rescue_rush.game_backend.integrations.discord.commands;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.db.views.game.GameStats_FilteredLeaderboard_View;
import lu.rescue_rush.game_backend.db.views.game.GameStats_Leaderboard_View;
import lu.rescue_rush.game_backend.db.views.game.quiz.QuizGameStats_FilteredLeaderboard_View;
import lu.rescue_rush.game_backend.db.views.game.quiz.QuizGameStats_Leaderboard_View;
import lu.rescue_rush.game_backend.db.views.game.scenario.ScenarioGameStats_FilteredLeaderboard_View;
import lu.rescue_rush.game_backend.db.views.game.scenario.ScenarioGameStats_Leaderboard_View;
import lu.rescue_rush.game_backend.integrations.discord.embeds.leaderboard.LeaderboardEmbed;
import lu.rescue_rush.game_backend.types.UserTypes.LeaderboardEntry;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class CmdLeaderboard implements SlashCommandAutocomplete, SlashCommandExecutor {

	private static final Logger LOGGER = Logger.getLogger(CmdLeaderboard.class.getName());

	@Autowired
	private GameStats_Leaderboard_View UNFILTERED_LEADERBOARD;
	@Autowired
	private GameStats_FilteredLeaderboard_View FILTERED_LEADERBOARD;

	@Autowired
	private QuizGameStats_Leaderboard_View QUIZ_UNFILTERED_LEADERBOARD;
	@Autowired
	private QuizGameStats_FilteredLeaderboard_View QUIZ_FILTERED_LEADERBOARD;

	@Autowired
	private ScenarioGameStats_Leaderboard_View SCENARIO_UNFILTERED_LEADERBOARD;
	@Autowired
	private ScenarioGameStats_FilteredLeaderboard_View SCENARIO_FILTERED_LEADERBOARD;

	@Autowired
	private LeaderboardEmbed LEADERBOARD_EMBED;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.deferReply();

		final int page = event.getOption("page", 0, OptionMapping::getAsInt);
		final boolean unfiltered = event.getOption("unfiltered", false, OptionMapping::getAsBoolean);
		final String source = event.getOption("source", "combined", OptionMapping::getAsString);

		// most unreadable code ever
		final List<LeaderboardEntry> entries = (unfiltered ? switch (source) {
		case "quiz" -> QUIZ_UNFILTERED_LEADERBOARD;
		case "scenario" -> SCENARIO_UNFILTERED_LEADERBOARD;
		default -> UNFILTERED_LEADERBOARD;
		} : switch (source) {
		case "quiz" -> QUIZ_FILTERED_LEADERBOARD;
		case "scenario" -> SCENARIO_FILTERED_LEADERBOARD;
		default -> FILTERED_LEADERBOARD;
		}).leaderboard(page);

		event.replyEmbeds(LEADERBOARD_EMBED.build(page, entries, unfiltered, source).build()).setEphemeral(true).queue();
	}

	@Override
	public String id() {
		return "leaderboard";
	}

	@Override
	public String description() {
		return "Shows the leaderboard";
	}

	@Override
	public OptionData[] options() {
		return new OptionData[] {
		//@formatter:off
				new OptionData(OptionType.INTEGER, "page", "Page", false),
				new OptionData(OptionType.BOOLEAN, "unfiltered", "With banned/hidden users included", false),
				new OptionData(OptionType.STRING, "source", "Source: combined, quiz, scenario", false, false)
						.addChoice("Combined", "combined")
						.addChoice("Scenario", "scenario")
						.addChoice("Quiz", "quiz")
		};
		//@formatter:on
	}

	@Override
	public void complete(CommandAutoCompleteInteractionEvent event) {

	}

}
