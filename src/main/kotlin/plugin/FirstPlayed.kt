package plugin

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

// Extending this class is standard bukkit boilerplate for any plugin, or else the server software won't load the classes.
class FirstPlayed : JavaPlugin() {
    // config.yml file to object initialization.
    var pluginConfig: FileConfiguration = getConfig()

    // What to do when the plugin is run by the server.
    override fun onEnable() {
        // Run command when plugin event is triggered.

        getCommand("firstplayed")!!.setExecutor(CommandManager())

        // Save config.yml and reload settings.
        saveDefaultConfig()
        reloadConfig()
    }

    // Runs plugin asynchronously so multiple players can use it at once efficiently.
    fun runTaskAsynchronously(run: Runnable?): BukkitTask {
        // Schedule Processes.

        return server.scheduler.runTaskAsynchronously(this, run!!)
    }
}