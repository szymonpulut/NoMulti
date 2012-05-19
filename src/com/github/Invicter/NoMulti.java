package com.github.Invicter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class NoMulti extends JavaPlugin implements Listener {

	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	public static final Logger logger = Bukkit.getLogger();

	public void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(getDataFolder(), "");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		InputStream defConfigStream = getResource("players.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			customConfig.setDefaults(defConfig);
		}
	}

	public void saveCustomConfig() {
		if (customConfig == null || customConfigFile == null) {
			return;
		}
		try {
			customConfig.save(customConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"[NoMulti] Could not save config to " + customConfigFile,
					ex);
		}
	}

	public FileConfiguration getCustomConfig() {
		if (customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	@EventHandler
	public boolean playerJoinEvent(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		String adress = player.getAddress().getHostName();
		adress.replace("/", "");

		String playername = player.getName();

		if (this.getCustomConfig().getString(adress) == null
				|| this.getCustomConfig().getString(adress) == "NULL"
				|| this.getCustomConfig().getString(adress) == "null"
				|| this.getCustomConfig().getString(adress) == "") {
			this.getCustomConfig().set(adress, playername);
		} else if (this.getCustomConfig().getString(adress) != null
				&& this.getCustomConfig().getString(adress) != "NULL"
				&& this.getCustomConfig().getString(adress) != "null"
				&& this.getCustomConfig().getString(adress) != "") {

			if (playername != this.getCustomConfig().getString(adress)) {
				player.kickPlayer(getConfig().getString("kick-message"));
			}
		}
		return true;
	}
}
