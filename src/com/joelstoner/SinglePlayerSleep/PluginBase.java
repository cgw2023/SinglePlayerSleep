package com.joelstoner.SinglePlayerSleep;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.mcstats.MetricsLite;
import org.spigotmc.Metrics;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;


@SuppressWarnings("unused")
public class PluginBase extends JavaPlugin implements Listener{

	public boolean UpdateCheck;
	public boolean debug;
	private boolean UpdateAviable = false;
	public final Logger logger = Logger.getLogger("Minecraft");
	public boolean isCanceled = false;
	public int transitionTask = 0;
	private URL url;
	private static long mobSpawningStartTime = 13187;
	//mobs stop spawning at: 22813
	//mobs start to burn at: 23600
	private static long mobSpawningStopTime = 23600;
	
	@Override // TODO:
	public void onEnable(){
		CheckForConfig();
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("**************************************************************");
		this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " Has been enabled");
		this.logger.info("**************************************************************");
		getServer().getPluginManager().registerEvents(this, this);
		
		Reloadconfig();
		
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		}catch (Exception e){
			// Failed to submit the stats
		}
		
	}
	
	@Override // TODO:
	public void onDisable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("**************************************************************");
		this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " Has been disabled");
		this.logger.info("**************************************************************");
		
	}
	
	private void CheckForConfig() {
		try{
			PluginDescriptionFile pdfFile = this.getDescription();
			if(!getDataFolder().exists()){
				this.log(pdfFile.getName() + ": Data Folder doesn't exist"); //Logger
				this.log(pdfFile.getName() + ": Creating Data Folder"); //Logger
				getDataFolder().mkdirs();
				this.log(pdfFile.getName() + ": Data Folder Created at " + getDataFolder()); //Logger
			}
			File  file = new File(getDataFolder(), "config.yml");
			getLogger().info("" + file);
			if(!file.exists()){
				this.log(pdfFile.getName() + ": config.yml not found, creating!"); //Logger
				saveDefaultConfig();
				FileConfiguration config = getConfig();
				
				config.options().copyDefaults(true);
				saveConfig();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException{
		final Player player = event.getPlayer();
		final World world = player.getWorld();
		//Broadcast to Server
		TextComponent message2 = new TextComponent(player.getDisplayName() + " is sleeping");
		TextComponent message = new TextComponent("[CANCEL] ");
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cancel"));
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to cancel sleep").create()));
		//this.broadcast(player.getDisplayName() + " is sleeping ");
		message.addExtra(message2);
		this.broadcast(message);
		//this.broadcast(message);
		//player.sendMessage( message );
		if(player.hasPermission("sps.hermits")){
			//Thread.sleep(10000);
			if(!isCanceled){
				
				transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

					public void run() {
						//getLogger().info("runnable");
						setDatime(player, world);
					}
					
				}, 10 * 20);
				
			}else{
				
				isCanceled = false;
			}
			//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
		}else if(!player.hasPermission("sps.hermits")){
			player.sendMessage("You do not have permission to Single Player Sleep!");
		}
	}
	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(player.hasPermission("sps.downfall")){
				world.setStorm(false);
				player.sendMessage("Clearing downfall...");
				this.log("clearing downfall..."); //Logger
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")){
				world.setThundering(false);
				player.sendMessage("Clearing thunderstorm...");
				this.log("clearing thunderstorm..."); //Logger
			}
		}
		long Relative_Time = 24000 - world.getTime();
		world.setFullTime(world.getFullTime() + Relative_Time);
		player.sendMessage("Setting time to day...");
		this.log("Setting time to day..."); //Logger
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player p = (Player)sender;
	    if (cmd.getName().equalsIgnoreCase("SPS"))
	    {
	      if (args.length == 0)
	      {
	        p.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	        p.sendMessage(ChatColor.GREEN + " Sleep in a bed to use.");
	        p.sendMessage(ChatColor.WHITE + " ");
	        p.sendMessage(ChatColor.WHITE + " /Sleep - subject to server admin approval");
	        p.sendMessage(ChatColor.WHITE + " /Cancel - Cancels SinglePlayerSleep");
	        p.sendMessage(ChatColor.WHITE + " ");
	        if(p.isOp()){
		        p.sendMessage(ChatColor.GOLD + " OP Commands");
		        p.sendMessage(ChatColor.GOLD + " /SPS update - Check for update.");
		        p.sendMessage(ChatColor.GOLD + " /SPS reload -  Reload config file.");
		        p.sendMessage(ChatColor.GOLD + " /SPS check true/false - set auto-update-check to true or false.");
	        }
	        p.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	        return true;
	      }
	      if(args[0].equalsIgnoreCase("check")){
	    	  if(args.length< 1){
					return false;
			  }
	    	  if(!args[1].equalsIgnoreCase("true") & !args[1].equalsIgnoreCase("false")){
					sender.sendMessage(ChatColor.YELLOW + this.getName() + " §cArgument must be boolean. Usage: /sps check True/False");
				}else if(args[1].contains("true") || args[1].contains("false")){
					FileConfiguration config = getConfig();
					config.set("auto-update-check", "" + args[1]);
					
					saveConfig();
					Reloadconfig();
					sender.sendMessage(ChatColor.YELLOW + this.getName() + " auto-update-check has been set to " + args[1]);
					if(args[1].contains("false")){
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " will not check for updates!");
					}else if(args[1].contains("true")){
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " will check for updates!");
					}
					reloadConfig();
					return true;
				}
	    	  
	      }
		  if(args[0].equalsIgnoreCase("reload")){
			  Reloadconfig();
	      }
		  if(args[0].equalsIgnoreCase("update")){
			  // Player must be OP and auto-update-check must be true
			if(p.isOp() && UpdateCheck){	
				try {
				
					URL url = new URL("https://raw.githubusercontent.com/JoelGodOfwar/SinglePlayerSleep/master/version.txt");
					final URLConnection conn = url.openConnection();
		            conn.setConnectTimeout(5000);
		            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		            final String response = reader.readLine();
		            final String localVersion = this.getDescription().getVersion();
		            if(debug){this.log("response= ." + response + ".");} //TODO: Logger
		            if(debug){this.log("localVersion= ." + localVersion + ".");} //TODO: Logger
		            if (!response.equalsIgnoreCase(localVersion)) {
						p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " New version available!");
					}else{
						p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.GREEN + " Version is up to date!");
					}
				} catch (MalformedURLException e) {
					this.log("MalformedURLException");
					e.printStackTrace();
				} catch (IOException e) {
					this.log("IOException");
					e.printStackTrace();
				}catch (Exception e) {
					this.log("Exception");
					e.printStackTrace();
				}
				
			}else{
				p.sendMessage(ChatColor.YELLOW + this.getName() + " " + p.getDisplayName() + " You are not OP, or auto-update-check is set to false in config.yml");
			}
		  }
	    }
		
		if(cmd.getName().equalsIgnoreCase("cancel")){
			Player player = (Player) sender;
			if(player.hasPermission("sps.cancel")) {
				Bukkit.getScheduler().cancelTask(transitionTask);
				//Broadcast to Server
				this.broadcast(player.getDisplayName() +  " canceled sleeping");
				isCanceled = true;
				
			}
			if(isCanceled){
				isCanceled = false;
			}
		}
		if(cmd.getName().equalsIgnoreCase("sleep")){
			Player player = (Player) sender;
			World w = player.getWorld();
			if(!IsNight(w)){
				player.sendMessage("It must be night to sleep. " + IsNight(w));
				return false;
			}
			
			if(player.hasPermission("sps.command")) {
				final Player player1 = (Player) sender;
				final World world = player1.getWorld();
				//Broadcast to Server
				TextComponent message2 = new TextComponent(player1.getDisplayName() + " is sleeping <command>");
				TextComponent message = new TextComponent("[CANCEL] ");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cancel"));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to cancel sleep").create()));
				//this.broadcast(player.getDisplayName() + " is sleeping ");
				message.addExtra(message2);
				this.broadcast(message);
				//this.broadcast(message);
				//player.sendMessage( message );
				if(player1.hasPermission("sps.hermits")){
					//Thread.sleep(10000);
					if(!isCanceled){
						
						transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

							public void run() {
								// TODO Auto-generated method stub
								//getLogger().info("runnable");
								setDatime(player1, world);
							}
							
						}, 10 * 20);
						
					}else{
						
						isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else if(!player1.hasPermission("sps.hermits")){
					player1.sendMessage("You do not have permission to Single Player Sleep!");
				}
			}
		}

		return true;
	}
	
	public void log(String dalog){
		this.logger.info(this.getName() + " " + dalog);
	}
	
	public void broadcast(String message){
		getServer().broadcastMessage("" + message);
	}
	public void broadcast(TextComponent message){
		Bukkit.getServer().spigot().broadcast(message);
	}
	  
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	  {
	    Player p = event.getPlayer();
	    if(p.isOp() && UpdateCheck){	
			try {
			
				URL url = new URL("https://raw.githubusercontent.com/JoelGodOfwar/SinglePlayerSleep/master/version.txt");
				final URLConnection conn = url.openConnection();
	            conn.setConnectTimeout(5000);
	            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            final String response = reader.readLine();
	            final String localVersion = this.getDescription().getVersion();
	            if(debug){this.log("response= ." + response + ".");} //TODO: Logger
	            if(debug){this.log("localVersion= ." + localVersion + ".");} //TODO: Logger
	            if (!response.equalsIgnoreCase(localVersion)) {
					p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " New version available!");
				}
			} catch (MalformedURLException e) {
				this.log("MalformedURLException");
				e.printStackTrace();
			} catch (IOException e) {
				this.log("IOException");
				e.printStackTrace();
			}catch (Exception e) {
				this.log("Exception");
				e.printStackTrace();
			}
			
		}
	}
	public void Reloadconfig(){
		// Load config.
		FileConfiguration config = getConfig();
		String daString = config.getString("debug").replace("'", "") + ",";
		
		if(daString.contains("true")){
			debug = true;
		}else{
			debug = false;
		}
		String daString2 = config.getString("auto-update-check").replace("'", "") + ",";
		if(daString2.contains("true")){
			UpdateCheck = true;
		}else{
			UpdateCheck = false;
		}
		
		if(debug){this.log("UpdateCheck = " + UpdateCheck);} //TODO: Logger
	}
	
	public static boolean IsNight(World w)
    {
    	long time = (w.getFullTime()) % 24000;
    	return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }

}
