package plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

// Extending this class is standard bukkit boilerplate for any plugin, or else the server software won't load the classes.
public class FirstPlayed extends JavaPlugin {

	// config.yml file to object initialization.
	public FileConfiguration config = getConfig();

	// What to do when the plugin is run by the server.
	@Override
	public void onEnable() {

		// Run command when plugin event is triggered.
		this.getCommand("firstplayed").setExecutor(new CommandManager());

		// Save config.yml and reload settings.
		saveDefaultConfig();
		reloadConfig();

	}

	// Runs plugin asynchronously so multiple players can use it at once efficiently.
	public BukkitTask runTaskAsynchronously(final Runnable run) {

		// Schedule Processes.
		return this.getServer().getScheduler().runTaskAsynchronously(this, run);

	}

}