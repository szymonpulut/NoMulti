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
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
        	Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "[NoMulti] Cannot submit data into PluginMetrics");
        }
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
				this.getCustomConfig().set(adress, playername.toLowerCase());
				this.saveCustomConfig();
			}
			else 
			{
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
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception add <nickName> - Adds a new player exception");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception remove <nickName> - Removes player exception");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database remove <IP> - Remove IP from database");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database add <IP> <player> - Adds IP to player binding");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti reload - Reloads plugin configs");
			}
			else if(args[0].equalsIgnoreCase("exception"))
			{
				if(args.length > 1)
				{
					if(args[1].equalsIgnoreCase("add"))
					{
						if(p.hasPermission("nomulti.exception.add"))
						{
							if(args.length == 3)
							{
								List<String> exceptions = getConfig().getStringList("exceptions");
								List<String> newExceptions = getConfig().getStringList("exceptions");
								newExceptions.add(args[2].toLowerCase());
								if(exceptions.equals(newExceptions))
								{
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Player "+ChatColor.GOLD+args[2]+ChatColor.RED+" already was on exceptions list");
								}
								else
								{
									this.getConfig().set("exceptions", newExceptions);
									this.saveConfig();
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Player "+ChatColor.GOLD+args[2]+ChatColor.RED+" added to exceptions list");
								}
							}
							else
							{
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Correct syntax:");
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception add <nickName> - Adds a new player exception");
							}
						}
						else
						{
							Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
							p.sendMessage(ChatColor.RED+"You don't have permissions!");
						}
					}
					else if(args[1].equalsIgnoreCase("remove"))
					{
						if(p.hasPermission("nomulti.exception.remove"))
						{
							if(args.length == 3)
							{
								List<String> exceptions = getConfig().getStringList("exceptions");
								List<String> newExceptions = getConfig().getStringList("exceptions");
								newExceptions.remove(args[2].toLowerCase());
								if(exceptions.equals(newExceptions))
								{
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Player "+ChatColor.GOLD+args[2]+ChatColor.RED+" wasn't on exceptions list");
								}
								else
								{
									this.getConfig().set("exceptions", newExceptions);
									this.saveConfig();
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Player "+ChatColor.GOLD+args[2]+ChatColor.RED+" removed from exceptions list");
								}
							}
							else
							{
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Correct syntax:");
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception remove <nickName> - Removes player exception");
							}
						}
						else
						{
							Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
							p.sendMessage(ChatColor.RED+"You don't have permissions!");
						}
					}
				}
			}
			else if(args[0].equalsIgnoreCase("database"))
			{
				if(args.length > 1)
				{
					if(args[1].equalsIgnoreCase("remove"))
					{
						if(p.hasPermission("nomulti.database.remove"))
						{
							if(args.length == 3)
							{
								if(this.getCustomConfig().getString(args[2]) == null ||
								this.getCustomConfig().getString(args[2]) == "" ||
								this.getCustomConfig().getString(args[2]) == "null" ||
								this.getCustomConfig().getString(args[2]) == "NULL" ||
								this.getCustomConfig().getString(args[2]) == "false")
								{
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"IP "+ChatColor.GOLD+args[2]+ChatColor.RED+" wasn't in database");
								}
								else
								{
									this.getCustomConfig().set(args[2], null);
									this.saveCustomConfig();
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"IP "+ChatColor.GOLD+args[2]+ChatColor.RED+" removed from database");
								}
							}
							else
							{
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Correct syntax:");
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database remove <IP> - Removes selected IP from database");
							}
						}
						else
						{
							Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
							p.sendMessage(ChatColor.RED+"You don't have permissions!");
						}
					}
					else if(args[1].equalsIgnoreCase("add"))
					{
						if(p.hasPermission("nomulti.database.add"))
						{
							if(args.length == 4)
							{
								if(this.getCustomConfig().getString(args[2]) == null ||
								this.getCustomConfig().getString(args[2]) == "" ||
								this.getCustomConfig().getString(args[2]) == "null" ||
								this.getCustomConfig().getString(args[2]) == "NULL" ||
								this.getCustomConfig().getString(args[2]) == "false")
								{
									this.getCustomConfig().set(args[2], args[3].toLowerCase());
									this.saveCustomConfig();
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"IP "+ChatColor.GOLD+args[2]+ChatColor.RED+" binded to player "+ChatColor.GOLD+args[3]);
								}
								else
								{
									p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"IP "+ChatColor.GOLD+args[2]+ChatColor.RED+" already is in database");
								}
							}
							else
							{
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Correct syntax:");
								p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database add <IP> <nickName> - Adds a new player to IP binding");
							}
						}
						else
						{
							Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" hasn't permissions to do that");
							p.sendMessage(ChatColor.RED+"You don't have permissions!");
						}
					}
				}
				else
				{
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Bad command syntax:");
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception add <nickName> - Adds a new player exception");
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception remove <nickName> - Removes player exception");
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database remove <IP> - Remove IP from database");
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database add <IP> <player> - Adds IP to player binding");
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti reload - Reloads plugin configs");
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" tried to use that command, but it doesn't exist");
				}
			}
			else if(args[0].equalsIgnoreCase("reload"))
			{
				if(p.hasPermission("nomulti.reload"))
				{
					p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"NoMulti reloaded!");
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
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"Bad command syntax:");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception add <nickName> - Adds a new player exception");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti exception remove <nickName> - Removes player exception");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database remove <IP> - Remove IP from database");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti database add <IP> <player> - Adds IP to player binding");
				p.sendMessage(ChatColor.BLUE+"[NoMulti] "+ChatColor.RED+"/nomulti reload - Reloads plugin configs");
				Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, p.getName()+" tried to use that command, but it doesn't exist");
			}	
			
		}

		return true;
	}
}
