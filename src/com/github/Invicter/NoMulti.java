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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class NoMulti extends JavaPlugin implements Listener {

	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	public static final Logger logger = Bukkit.getLogger();

	public void reloadCustomConfig() {
	    if (customConfigFile == null) {
	    customConfigFile = new File(getDataFolder(), "players.yml");
	    }
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	    
	    InputStream defConfigStream = getResource("players.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
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
	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "[NoMulti] Could not save config to " + customConfigFile, ex);
	    }
	}
	
	public FileConfiguration getCustomConfig() {
	    if (customConfig == null) {
	        reloadCustomConfig();
	    }
	    return customConfig;
	}

	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);
		Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Enabling plugin");
		this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.getCustomConfig().options().copyDefaults(true);
        this.saveCustomConfig();
		Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Plugin enabled");
	}
	
	@Override
	public void onDisable()
	{
		Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Disabling plugin");
		this.saveConfig();
		this.saveCustomConfig();
		Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Plugin disabled");
	}

	@EventHandler
	public boolean onPlayerLogin(PlayerLoginEvent event){

		Player player = event.getPlayer();
		String adress = event.getAddress().getHostAddress();
		String playername = player.getName();
        
		this.saveCustomConfig();
        this.reloadCustomConfig();
        
		if(!player.hasPermission("nomulti.exempt"))
		{
			if(this.getCustomConfig().getString(adress) == null ||
			this.getCustomConfig().getString(adress) == "" ||
			this.getCustomConfig().getString(adress) == "null" ||
			this.getCustomConfig().getString(adress) == "NULL" ||
			this.getCustomConfig().getString(adress) == "false")
			{
				this.getCustomConfig().set(adress, playername);
			}
			else 
			{
				if (playername != this.getCustomConfig().getString(adress)) 
				{
					event.setKickMessage(getConfig().getString("kick-message"));
			    	event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
				}
				else if (playername == this.getCustomConfig().getString(adress))
				{
					//Nothing
				}
			}
				
	        this.saveCustomConfig();
	        this.reloadCustomConfig();
	        
		}
	return true;
	}
}
