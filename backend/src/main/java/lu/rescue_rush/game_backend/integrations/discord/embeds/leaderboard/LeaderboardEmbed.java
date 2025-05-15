package lu.rescue_rush.game_backend.integrations.discord.embeds.leaderboard;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;

import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbed;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbedBuilder;
import lu.rescue_rush.game_backend.types.UserTypes.LeaderboardEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class LeaderboardEmbed implements DiscordEmbedBuilder {

	public DiscordEmbed build(int page, List<LeaderboardEntry> entries, boolean unfiltered, String source) {
		Objects.requireNonNull(entries);

		return new DiscordEmbed() {

			@Override
			public MessageEmbed build() {
				final EmbedBuilder builder = new EmbedBuilder();

				final int maxNameLength = entries.parallelStream().mapToInt(e -> e.username.length()).max().orElse(10);
				builder.addField("List: ", entries.stream().map(e -> "`" + PCUtils.leftPadString(Integer.toString(e.position), " ", 3) + ". " + PCUtils.leftPadString(e.username, ".", maxNameLength)
						+ " ." + PCUtils.rightPadString(Integer.toString(e.points), " ", 6) + "`").collect(Collectors.joining("\n")), false);

				builder.setTitle("Leaderboard page #" + page + " (" + (unfiltered ? "unfiltered" : "filtered") + ") [" + source + "]");
				builder.setColor(Color.CYAN);

				return builder.build();
			}

		};
	}

}
