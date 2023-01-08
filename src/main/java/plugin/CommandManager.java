/*
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package plugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

// Extends bukkit classes to run commands with tab completion.
public class CommandManager implements CommandExecutor, TabExecutor {

	// Enable the conversion of text from config.yml to objects.
    public FileConfiguration config = Bukkit.getPluginManager().getPlugin("FirstPlayed-OG").getConfig();

    // Command execution event handler extending bukkit's CommandManager.
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    	// If no username is specified after /firstplayed, do this.
        if (args.length == 0 && sender instanceof Player) {
        	
        	// Make sure the player has permission to fetch own join information first.
            if (! sender.hasPermission("firstplayed.me")) {
            	
            	// Without permission, stop here.
            	return false;

            }
            // Command sender has permission.
            else {

	        	// With no parameters, plugin will assume player wants information about themselves.
	            sendOwnInfo(sender);
	
	            // Healthy exit status.
	            return true;
            
            }

        }
        // If a username is specified after /firstplayed, do this.
        else if (args.length == 1) {

        	// Make sure the player has permission to fetch others' join information first.
            if (! sender.hasPermission("firstplayed.other")) {
            	
            	// Without permission, stop here.
            	return false;

            }
            // With permission, proceed.
            else {
            	
            	// Get the string that the user put in after /firstplayed.
                String target = args[0];

            	// If online, do fast lookup and forward information to command runner.
            	sendOtherInfo(sender, target);

            	// Healthy exit status.
                return true;
            	
            }

        }
        // If the player passed too many arguments.
        else {
        	
        	// Send error to command runner.
        	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("too_many_arguments_error")));
        	
            // Command Failed (will show /usage).
            return false;
        	
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

        // Feed color codes from config.yml into formatted message and send it to command runner (self).
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("message_me") + date));

    }
    
    // Runs when player specifies one argument.
    public void sendOtherInfo(CommandSender sender, String target) {
    	
    	// If offline, do slow lookup. Always returns an object, never null.
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        // Manually check if player is real because offlinePlayer is always a valid Object.
        if (offlinePlayer.hasPlayedBefore()) {
        	
        	// Get join data from world files.
            long timestamp = offlinePlayer.getFirstPlayed();
            // Convert timestamp to String
            String date = new SimpleDateFormat(config.getString("date_format")).format(new Date(timestamp));
            
            // Feed color codes from config.yml into formatted message and send it to command runner.
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + "&a" + target + " " + config.getString("message_other") + date));
        	
        }
        else {
        
            // Notify command sender that the player they specified does not exist.
        	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("invalid_player_error") + target));
        	
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