package com.github.Invicter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
		Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Enabling plugin");
		getServer().getPluginManager().registerEvents(this, this);
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
		List<String> exceptions = getConfig().getStringList("exceptions");
		if(!exceptions.contains(playername))
		{
			if(this.getCustomConfig().getString(adress) == null ||
			this.getCustomConfig().getString(adress) == "" ||
			this.getCustomConfig().getString(adress) == "null" ||
			this.getCustomConfig().getString(adress) == "NULL" ||
			this.getCustomConfig().getString(adress) == "false")
			{
				this.getCustomConfig().set(adress, playername);
				this.saveCustomConfig();
			}
			else 
			{
				this.saveCustomConfig();
				this.reloadCustomConfig();
				
				if (!this.getCustomConfig().getString(adress).equalsIgnoreCase(playername)) 
				{
					event.setKickMessage(this.getConfig().getString("kick-message"));
			    	event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
				}
			}
		} 
		return true;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player p = (Player) sender;
		String arguments = "";
		
		if(cmd.getName().equalsIgnoreCase("nomulti")){
			for(int i = 0; i<args.length; i++){
				arguments += " "+args[i];
			}
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[PLAYER_COMMAND] "+p.getName()+": /"+commandLabel+arguments);
			
			if(args.length < 1)
			{
				p.sendMessage(ChatColor.RED+"/nomulti add <nickName> - Adds a new player exception");
				p.sendMessage(ChatColor.RED+"/nomulti remove <IP> - Remove IP from database");
				p.sendMessage(ChatColor.RED+"/nomulti reload - Reloads plugin configs");
			}
			else if(args[0].equalsIgnoreCase("add"))
			{
				if(p.hasPermission("nomulti.add"))
				{
					if(args.length == 2)
					{
						List<String> newExceptions = getConfig().getStringList("exceptions");
						newExceptions.add(args[1]);
						this.getConfig().set("exceptions", newExceptions);
						this.saveConfig();
						p.sendMessage(ChatColor.RED+"Player "+args[1]+" added to exceptions list");
					}
					else
					{
						p.sendMessage(ChatColor.RED+"Correct syntax:");
						p.sendMessage(ChatColor.RED+"/nomulti add <nickName> - Adds a new player exception");
					}
				}
				else
				{
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
					p.sendMessage(ChatColor.RED+"You don't have permissions!");
				}
			}
			else if(args[0].equalsIgnoreCase("remove"))
			{
				if(p.hasPermission("nomulti.remove"))
				{
					if(args.length == 2)
					{
						if(this.getCustomConfig().getString(args[1]) == null ||
						this.getCustomConfig().getString(args[1]) == "" ||
						this.getCustomConfig().getString(args[1]) == "null" ||
						this.getCustomConfig().getString(args[1]) == "NULL" ||
						this.getCustomConfig().getString(args[1]) == "false")
						{
							p.sendMessage(ChatColor.RED+"IP "+args[1]+" wasn't in database");
						}
						else
						{
							this.getCustomConfig().set(args[1], null);
							this.saveCustomConfig();
							p.sendMessage(ChatColor.RED+"IP "+args[1]+" removed from database");
						}
					}
					else
					{
						p.sendMessage(ChatColor.RED+"Correct syntax:");
						p.sendMessage(ChatColor.RED+"/nomulti remove <IP> - Adds a new player exception");
					}
				}
				else
				{
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
					p.sendMessage(ChatColor.RED+"You don't have permissions!");
				}
			}
			else if(args[0].equalsIgnoreCase("reload"))
			{
				if(p.hasPermission("nomulti.reload"))
				{
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Reloading configs");
					this.reloadConfig();
					this.reloadCustomConfig();
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Configs reloaded");
				}
				else
				{
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
					p.sendMessage(ChatColor.RED+"You don't have permissions!");
				}
			}
			else
			{
				Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" tried to use that command, but it doesn't exist");
			}	
			
		}

		return true;
	}
}
