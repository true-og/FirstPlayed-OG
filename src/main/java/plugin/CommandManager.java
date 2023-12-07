package plugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

// Extends bukkit classes to run commands with tab completion.
public class CommandManager implements CommandExecutor, TabExecutor {

	// Enable the conversion of text from config.yml to objects.
	public FileConfiguration config = Bukkit.getPluginManager().getPlugin("FirstPlayed-OG").getConfig();

	// Command execution event handler extending bukkit's CommandManager.
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// Make sure the player has permission to fetch others' join information first.
		if (! sender.hasPermission("firstplayed.other")) {

			// Without permission, stop here.
			return false;

		}
		// With permission, proceed.
		else {

			// If no username is specified after /firstplayed, do this.
			if (args.length == 0 && sender instanceof Player) {

				// With no parameters, plugin will assume player wants information about themselves.
				sendOwnInfo(sender);

				// Healthy exit status.
				return true;

			}
			// If a username is specified after /firstplayed, do this.
			else if (args.length == 1) {

				// Get the string that the user put in after /firstplayed.
				String target = args[0];

				// If the username that was specified is the command sender's own username, do this...
				if(sender.getName().equalsIgnoreCase(target)) {

					// Send the player their own join message.
					sendOwnInfo(sender);

					// Healthy exit status.
					return true;

				}
				else {

					// Send the player their requested join message.
					sendOtherInfo(sender, target);

					// Healthy exit status.
					return true;

				}

			}
			// If the player passed too many arguments.
			else {

				// Create a colored too many arguments error message using the TextComponent API.
				String tooManyArgumentsError = config.getString("prefix") + config.getString("too_many_arguments_error");		
				TextComponent tooManyArgumentsContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(tooManyArgumentsError);

				// Send error to command runner.
				sender.sendMessage(tooManyArgumentsContainer);

				// Command Failed (will show /usage).
				return false;

			}

		}

	}

	// Runs when player specifies no arguments.
	public void sendOwnInfo(CommandSender sender) {

		// Derive player object from the username of the person who ran the command.
		Player playerToLookUp = (Player) sender;
		// Get join data from world files.
		long timestamp = playerToLookUp.getFirstPlayed();
		// Convert timestamp to String.
		String date = new SimpleDateFormat(config.getString("date_format")).format(new Date(timestamp));

		// Format the player's own join information using the TextComponent API.
		String ownInfo = config.getString("prefix") + config.getString("message_me") + date;		
		TextComponent ownInfoContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(ownInfo);

		// Send the player their own join information in chat.
		sender.sendMessage(ownInfoContainer);

	}

	// Runs when player specifies one argument.
	public void sendOtherInfo(CommandSender sender, String target) {

		// If offline, do slow lookup. Always returns an object, never null.
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
		// Manually check if player is real because offlinePlayer is always a valid Object.
		if (offlinePlayer.hasPlayedBefore()) {

			// Get join data from world files.
			long timestamp = offlinePlayer.getFirstPlayed();
			// Convert timestamp to String.
			String date = new SimpleDateFormat(config.getString("date_format")).format(new Date(timestamp));

			// Format the player's requested join information using the TextComponent API.
			String requestedInfo = config.getString("prefix") + "&a" + target + " " + config.getString("message_other") + date;
			TextComponent requestedInfoContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(requestedInfo);

			// Send the player their requested join information in chat.
			sender.sendMessage(requestedInfoContainer);

		}
		else {

			// Format the player not found error using the TextComponent API.
			String invalidPlayerError = config.getString("prefix") + config.getString("invalid_player_error") + target;		
			TextComponent invalidPlayerErrorContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(invalidPlayerError);

			// Notify command sender that the player they specified does not exist.
			sender.sendMessage(invalidPlayerErrorContainer);

		}

	}

	// Tab completion (online players only for performance reasons).
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

		// Check to make sure that tab completion should be operating.
		if (args.length == 1 && sender.hasPermission("firstplayed.other")) {

			// Make an empty list of strings to store potential players for tab completion.
			List<String> players = new ArrayList<String>();
			// Optimized java 5 style Collections loop, not list or array loop, for efficiency and compliance with bukkit API.
			for (Player player : Bukkit.getOnlinePlayers()) {

				// Case-agnosticism.
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {

					// Feeds individual player to list on each run of loop.
					players.add(player.getName());

				}

			}

			// Completed list of online players to tab complete.
			return players;

		}

		// Show all options.
		return new ArrayList<>();
	}

}