package lu.rescue_rush.game_backend.integrations.discord.embeds.system;

import java.awt.Color;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lu.rescue_rush.game_backend.integrations.discord.buttons.shutdown.BtnShutdown;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordButtonEmbed;
import lu.rescue_rush.game_backend.integrations.discord.embeds.DiscordEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Service
public class EmbedStartup implements DiscordEmbedBuilder {

	private static final Logger LOGGER = Logger.getLogger(EmbedStartup.class.getName());

	@Autowired
	private BtnShutdown btnShutdown;

	public DiscordButtonEmbed build(String[] profiles, boolean debug, String name, String version, String build, String sharedVersion, Environment environment,
			ConfigurableApplicationContext context) {
		return new DiscordButtonEmbed() {

			@Override
			public MessageEmbed build() {
				EmbedBuilder builder = new EmbedBuilder();

				Field networkField = null;

				try {
					InetAddress lh = InetAddress.getLocalHost();

					final StringBuilder sb = new StringBuilder();
					sb.append("**Interfaces:**\n");

					Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
					while (interfaces.hasMoreElements()) {
						NetworkInterface ni = interfaces.nextElement();

						if (!ni.isUp() || ni.isLoopback())
							continue;

						Enumeration<InetAddress> addresses = ni.getInetAddresses();
						while (addresses.hasMoreElements()) {
							InetAddress address = addresses.nextElement();
							sb.append("* __" + ni.getName() + "__ :arrow_right: `" + address.getHostAddress() + "` (" + (address.getHostName() == null ? "*NULL*" : address.getHostName()) + ")\n");
						}
					}

					networkField = new Field("Network", "**Localhost:** `" + lh.getHostAddress() + "` (" + (lh.getHostName() == null ? "*NULL*" : lh.getHostName()) + ")\n" + sb.toString(), false);
				} catch (SocketException | UnknownHostException e) {
					e.printStackTrace();

					networkField = new Field("Network", "**Error getting hosts:** " + e.getClass().getName() + ": " + e.getMessage(), true);
				}

				final StringBuilder systemSb = new StringBuilder();
				systemSb.append("**Operating System:** " + System.getProperty("os.name") + "\n");
				systemSb.append("**OS Version:** " + System.getProperty("os.version") + "\n");
				systemSb.append("**OS Architecture:** " + System.getProperty("os.arch") + "\n");
				systemSb.append("**Java Version:** " + System.getProperty("java.version") + "\n");
				systemSb.append("**User Name:** " + System.getProperty("user.name") + "\n");
				Field systemField = new Field("System", systemSb.toString(), true);

				final StringBuilder hardwareSb = new StringBuilder();
				hardwareSb.append("**Cores:** " + Runtime.getRuntime().availableProcessors() + "\n");
				hardwareSb.append("**Max Memory:** " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB\n");
				hardwareSb.append("**Allocated Memory:** " + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB\n");
				hardwareSb.append("**Free Memory:** " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB\n");
				Field hardwareField = new Field("Hardware", hardwareSb.toString(), true);

				Field springBootField = null;
				// String endpoints = null;
				try {
					final StringBuilder springBootSb = new StringBuilder();
					springBootSb.append("**Server port:** " + environment.getProperty("server.port") + "\n");
					springBootSb.append("**Application version:** " + name + " v." + version + "-build." + build + "-shared." + sharedVersion + "\n");
					springBootSb.append("**Profiles:** " + Arrays.toString(profiles) + "\n");
					springBootSb.append("**Debug:** " + (debug ? ":white_check_mark:" : ":x:") + "\n");

					/*
					 * final RequestMappingHandlerMapping requestMappingHandler =
					 * context.getBean("requestMappingHandlerMapping",
					 * RequestMappingHandlerMapping.class); endpoints = "**Endpoints:** " +
					 * requestMappingHandler.getHandlerMethods().size() + "\n```" +
					 * requestMappingHandler.getHandlerMethods().keySet().stream().flatMap(c ->
					 * c.getDirectPaths().stream()).sorted().map(c -> "- " +
					 * c).collect(Collectors.joining("\n")) + "```";
					 * 
					 * if (endpoints.length() >= 1024) { springBootSb.append("**Endpoints:** " +
					 * requestMappingHandler.getHandlerMethods().size() + "\n```*too long*```");
					 * endpoints = null; } else if (endpoints.length() < (1024 -
					 * springBootSb.length())) { springBootSb.append(endpoints); endpoints = null; }
					 */

					springBootField = new Field("Spring Boot", springBootSb.toString(), false);
				} catch (Exception e) {
					e.printStackTrace();

					springBootField = new Field("Spring Boot", "**Error getting spring boot information:** " + e.getClass().getName() + ": " + e.getMessage(), false);
				}

				builder.setTitle("Rescue-rush.lu | Startup");
				builder.setColor(Color.BLUE);

				builder.addField(networkField);
				builder.addField(systemField);
				builder.addField(hardwareField);
				builder.addField(springBootField);

				/*
				 * if (endpoints != null) { builder.addField(new Field("Spring Boot +",
				 * endpoints, false)); }
				 */

				return builder.build();
			}

			@Override
			public Button button() {
				return Button.danger(btnShutdown.id(), "Shutdown backend");
			}

		};
	}

}
