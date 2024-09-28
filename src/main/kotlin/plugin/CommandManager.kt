package plugin

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.*
// Extends bukkit classes to run commands with tab completion.
class CommandManager : CommandExecutor, TabExecutor {
    var config: FileConfiguration = Bukkit.getPluginManager().getPlugin("FirstPlayed-OG")!!.config
    // TODO: get these from config
    val redisURL: String = config.getString("redis_url") ?: "localhost"
    val redisPort = config.getInt("redis_port")
    val redisClient = newClient(Endpoint(redisURL,redisPort))
    private val databasePrefix = config.getString("redis_database_prefix") ?: "firstplayedog:uuid:"
    // Enable the conversion of text from config.yml to objects.

    // Command execution event handler extending bukkit's CommandManager.
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        // Make sure the player has permission to fetch others' join information first.

        if (!sender.hasPermission("firstplayed.other")) {
            // Without permission, stop here.

            return false
        } else {
            // If no username is specified after /firstplayed, do this.

            if (args.size == 0 && sender is Player) {
                // With no parameters, plugin will assume player wants information about themselves.

                sendOwnInfo(sender)

                // Healthy exit status.
                return true
            } else if (args.size == 1) {
                // Get the string that the user put in after /firstplayed.

                val target = args[0]

                // If the username that was specified is the command sender's own username, do this...
                if (sender.name.equals(target, ignoreCase = true)) {
                    // Send the player their own join message.

                    sendOwnInfo(sender)

                    // Healthy exit status.
                    return true
                } else {
                    // Send the player their requested join message.

                    sendOtherInfo(sender, target)

                    // Healthy exit status.
                    return true
                }
            } else {
                // Create a colored too many arguments error message using the TextComponent API.

                val tooManyArgumentsError = config.getString("prefix") + config.getString("too_many_arguments_error")
                val tooManyArgumentsContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(tooManyArgumentsError)

                // Send error to command runner.
                sender.sendMessage(tooManyArgumentsContainer)

                // Command Failed (will show /usage).
                return false
            }
        }
    }

    // Runs when player specifies no arguments.
    fun sendOwnInfo(sender: CommandSender) {
    // Derive player object from the username of the person who ran the command.

        GlobalScope.launch {
            val p = sender as Player
            val uuid = p.uniqueId
            val timestamp_from_database: String? = redisClient.get(databasePrefix + uuid.toString())
            // not giving timestamp a default value helps with compile time checking on whether we have set it or not
            var timestamp: Long
            if(timestamp_from_database == null) {
                // not in database
                println("$uuid needs to be added to cache")
                timestamp = p.firstPlayed
                // try adding timestamp to cache
                println("adding $uuid to cache")
                redisClient.set(databasePrefix + uuid.toString(), timestamp.toString())
            }

            else {
                // in database
                timestamp = timestamp_from_database.toLong()
            }

            // Convert timestamp to String.
            val date = SimpleDateFormat(config.getString("date_format")).format(Date(timestamp))

            // Format the player's own join information using the TextComponent API.
            val ownInfo = config.getString("prefix") + config.getString("message_me") + date
            val ownInfoContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(ownInfo)

            // Send the player their own join information in chat.
            sender.sendMessage(ownInfoContainer)

        }

    }

    // Runs when player specifies one argument.
    fun sendOtherInfo(sender: CommandSender, target: String) {
        // If offline, do slow lookup. Always returns an object, never null.

        val offlinePlayer = Bukkit.getOfflinePlayer(target)
        // Manually check if player is real because offlinePlayer is always a valid Object.
        if (offlinePlayer.hasPlayedBefore()) {
            GlobalScope.launch {
                val uuid = offlinePlayer.uniqueId
                // not giving timestamp a default value helps with compile time checking on whether we have set it or not
                var timestamp: Long
                val timestamp_from_database: String? = redisClient.get(databasePrefix + uuid)
                if(timestamp_from_database == null) {
                    // get from world file
                    println("$uuid is not in cache")
                    timestamp = offlinePlayer.firstPlayed
                    println("adding $uuid to cache")
                    redisClient.set(databasePrefix + uuid.toString(), timestamp.toString())
                } else {
                    // get from database
                    println("$uuid is in cache!")
                    timestamp = timestamp_from_database.toLong()
                }
                // continue as usual
                // Convert timestamp to String.
                val date = SimpleDateFormat(config.getString("date_format")).format(Date(timestamp))

                // Format the player's requested join information using the TextComponent API.
                val requestedInfo = config.getString("prefix") + "&a" + target + " " + config.getString("message_other") + date
                val requestedInfoContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(requestedInfo)

                // Send the player their requested join information in chat.
                sender.sendMessage(requestedInfoContainer)
            }
        } else {
            // Format the player not found error using the TextComponent API.

            val invalidPlayerError = config.getString("prefix") + config.getString("invalid_player_error") + target
            val invalidPlayerErrorContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(invalidPlayerError)

            // Notify command sender that the player they specified does not exist.
            sender.sendMessage(invalidPlayerErrorContainer)
        }
    }

    // Tab completion (online players only for performance reasons).
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        // Check to make sure that tab completion should be operating.

        if (args.size == 1 && sender.hasPermission("firstplayed.other")) {
            // Make an empty list of strings to store potential players for tab completion.

            val players: MutableList<String> = ArrayList()
            // Optimized java 5 style Collections loop, not list or array loop, for efficiency and compliance with bukkit API.
            for (player in Bukkit.getOnlinePlayers()) {
                // Case-agnosticism.

                if (player.name.lowercase(Locale.getDefault()).startsWith(args[0].lowercase(Locale.getDefault()))) {
                    // Feeds individual player to list on each run of loop.

                    players.add(player.name)
                }
            }

            // Completed list of online players to tab complete.
            return players
        }

        // Show all options.
        return ArrayList()
    }
}
