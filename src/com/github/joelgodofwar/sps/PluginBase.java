package com.github.joelgodofwar.sps;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
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

import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.api.ConfigAPI;



@SuppressWarnings("unused")
public class PluginBase extends JavaPlugin implements Listener{

	public static boolean UpdateCheck;
	public static boolean cancelbroadcast;
	public static boolean debug;
	public static String daLang;
	private boolean UpdateAviable = false;
	public final static Logger logger = Logger.getLogger("Minecraft");
	public boolean isCanceled = false;
	public int transitionTask = 0;
	private URL url;
	private static long mobSpawningStartTime = 13000;
	//mobs stop spawning at: 22813
	//mobs start to burn at: 23600
	private static long mobSpawningStopTime = 23600;
	File langFile;
    FileConfiguration lang;
    
	
	@Override // TODO:
	public void onEnable(){
		langFile = new File(getDataFolder(), "lang.yml");
		if(!langFile.exists()){                                  // checks if the yaml does not exists
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ConfigAPI.CheckForConfig(this);
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginBase.logger.info("**************************************************************");
		PluginBase.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " Has been enabled");
		PluginBase.logger.info("**************************************************************");
		getServer().getPluginManager().registerEvents(this, this);
		
		String varCheck = getConfig().getString("auto-update-check");
		String varCheck2 = getConfig().getString("cancelcolor");
		String varCheck3 = getConfig().getString("cancelbroadcast");
		String varCheck4 = getConfig().getString("debug");
		String varCheck5 = getConfig().getString("lang");
		//log("varCheck " + varCheck);
		//log("varCheck2 " + varCheck2);
		//log("varCheck3 " + varCheck3);
		if(varCheck.contains("default")){
			getConfig().set("auto-update-check", true);
		}
		if(varCheck2.contains("default")){
			getConfig().set("cancelcolor", "RED");
		}
		if(varCheck3.contains("default")){
			getConfig().set("cancelbroadcast", true);
		}
		if(varCheck4.contains("default")){
			getConfig().set("debug", false);
		}
		if(varCheck5.contains("default")){
			getConfig().set("lang", "en_US");
		}
		saveConfig();
		ConfigAPI.Reloadconfig(this, null);
		
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
		PluginBase.logger.info("**************************************************************");
		PluginBase.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " Has been disabled");
		PluginBase.logger.info("**************************************************************");
		
	}
	
	
	
	
	@EventHandler
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException{
		final Player player = event.getPlayer();
		final World world = player.getWorld();
		//Broadcast to Server
		TextComponent message2 = new TextComponent(player.getDisplayName() + " " + lang.get("issleep." + daLang + ""));
		TextComponent message = new TextComponent(ChatColorUtils.setColors(getConfig().getString("cancelcolor")) + " [" + lang.get("cancel." + daLang + "") + "]");
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cancel"));
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("" + lang.get("clickcancel." + daLang + "")).create()));
		//this.broadcast(player.getDisplayName() + " is sleeping ");
		message2.addExtra(message);
		this.broadcast(message2);
		//this.broadcast(message);
		//player.sendMessage( message );
		if(player.hasPermission("sps.hermits")||player.hasPermission("sps.op")){
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
		}else if(!player.hasPermission("sps.hermits")||player.hasPermission("sps.op")){
			player.sendMessage("" + lang.get("noperm." + daLang + ""));
		}
	}

	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(player.hasPermission("sps.downfall")||player.hasPermission("sps.op")){
				world.setStorm(false);
				if(debug){
					log("" + lang.get("setdownfall." + daLang + "") + "...");
					} //TODO: Logger
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")||player.hasPermission("sps.op")){
				world.setThundering(false);
				if(debug){
					log("" + lang.get("setthunder." + daLang + "") + "...");
					} //TODO: Logger
			}
		}
		long Relative_Time = 24000 - world.getTime();
		world.setFullTime(world.getFullTime() + Relative_Time);
		log("" + debug);
		if(debug){
			log("" + lang.get("settime." + daLang + "") + "...");
		} //TODO: Logger
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	    if (cmd.getName().equalsIgnoreCase("SPS"))
	    {
	      if (args.length == 0)
	      {
	    	sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	    	sender.sendMessage(ChatColor.GREEN + " " + lang.get("touse." + daLang + ""));//Sleep in a bed to use.");
	    	sender.sendMessage(ChatColor.WHITE + " ");
	    	sender.sendMessage(ChatColor.WHITE + " /Sleep - " + lang.get("sleephelp." + daLang + ""));//subject to server admin approval");
	    	sender.sendMessage(ChatColor.WHITE + " /Cancel - " + lang.get("cancelhelp." + daLang + ""));//Cancels SinglePlayerSleep");
	    	sender.sendMessage(ChatColor.WHITE + " ");
	        if(sender.isOp()||sender.hasPermission("sps.op")){
	        	sender.sendMessage(ChatColor.GOLD + " OP Commands");
	        	sender.sendMessage(ChatColor.GOLD + " /SPS update - " + lang.get("spsupdate." + daLang + ""));//Check for update.");
	        	sender.sendMessage(ChatColor.GOLD + " /SPS reload - " + lang.get("spsreload." + daLang + ""));//Reload config file.");
	        	sender.sendMessage(ChatColor.GOLD + " /SPS check true/false - " + lang.get("spscheck." + daLang + ""));//set auto-update-check to true or false.");
	        }
	        sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	        return true;
	      }
	      if(args[0].equalsIgnoreCase("check")){
	    	  if(args.length< 1){
					return false;
			  }
	    	  if(sender.isOp()||sender.hasPermission("sps.op")){
	    		  if(!args[1].equalsIgnoreCase("true") & !args[1].equalsIgnoreCase("false")){
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " §c" + lang.get("boolean." + daLang + "") + ": /sps check True/False");
	    		  }else if(args[1].contains("true") || args[1].contains("false")){
	    			    FileConfiguration config = getConfig();
						config.set("auto-update-check", "" + args[1]);
						
						saveConfig();
						ConfigAPI.Reloadconfig(this, null);
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("checkset." + daLang + "") + " " + args[1]);
						if(args[1].contains("false")){
							sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("nocheck." + daLang + ""));
						}else if(args[1].contains("true")){
							sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("yescheck." + daLang + ""));
						}
						reloadConfig();
						return true;
					}
	    	  }
	    	  
	      }
		  if(args[0].equalsIgnoreCase("reload")){
			  if(sender.isOp()||sender.hasPermission("sps.op")||!(sender instanceof Player)){
				  //ConfigAPI.Reloadconfig(this, p);
				  this.reloadConfig();
				  PluginBase plugin = this;
				  getServer().getPluginManager().disablePlugin(plugin);
                  getServer().getPluginManager().enablePlugin(plugin);
			  }else if(!sender.hasPermission("sps.op")){
				  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("noperm." + daLang + " reload"));
			  }
	      }
		  if(args[0].equalsIgnoreCase("update")){
			  // Player must be OP and auto-update-check must be true
			if(sender.isOp() && UpdateCheck||sender.hasPermission("sps.op") && UpdateCheck){	
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
		            	sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers." + daLang + ""));
					}else{
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.GREEN + " " + lang.get("curvers." + daLang + ""));
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
				sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("notop." + daLang + ""));
			}
		  }
	    }
		
		if(cmd.getName().equalsIgnoreCase("cancel")){
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			if(sender.hasPermission("sps.cancel") && IsNight(worlds.get(0))||sender.hasPermission("sps.op") && IsNight(worlds.get(0))) {
				Bukkit.getScheduler().cancelTask(transitionTask);
				//Broadcast to Serve.r
				if(cancelbroadcast){this.broadcast(sender.getName() +  " " + lang.get("canceledsleep." + daLang + ""));}
				isCanceled = true;
				
			}
			if(isCanceled){
				isCanceled = false;
			}
		}
		if(cmd.getName().equalsIgnoreCase("sleep")){
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			//World w = ((Entity) sender).getWorld();
			if(!IsNight(worlds.get(0))){
				sender.sendMessage("" + lang.get("mustbenight." + daLang + ""));
				return false;
			}
			
			if(sender.hasPermission("sps.command")||sender.hasPermission("sps.op")) {
				//final Player player1 = (Player) sender;
				final CommandSender daSender = sender;

				final World world = worlds.get(0);

				//Broadcast to Server
				TextComponent message2 = new TextComponent(sender.getName() + " " + lang.get("sleepcommand." + daLang + ""));
				TextComponent message = new TextComponent(ChatColorUtils.setColors(getConfig().getString("cancelcolor")) + " [" + lang.get("cancel." + daLang + "") + "]");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cancel"));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("" + lang.get("clickcancel." + daLang + "")).create()));
				//this.broadcast(player.getDisplayName() + " is sleeping ");
				message2.addExtra(message);
				this.broadcast(message2);
				//this.broadcast(message);
				//player.sendMessage( message );
				if(sender.hasPermission("sps.hermits")){
					//Thread.sleep(10000);
					
					if(!isCanceled){
						
						transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

							public void run() {
								// TODO Auto-generated method stub
								//getLogger().info("runnable");
								//setDatime(sender, world);
								if(world.hasStorm()){
									if(daSender.hasPermission("sps.downfall")||daSender.hasPermission("sps.op")){
										world.setStorm(false);
										if(debug){
											log("" + lang.get("setdownfall." + daLang + "") + "...");
											} //TODO: Logger
									}
								}
								if(world.isThundering()){
									if(daSender.hasPermission("sps.thunder")||daSender.hasPermission("sps.op")){
										world.setThundering(false);
										if(debug){
											log("" + lang.get("setthunder." + daLang + "") + "...");
											} //TODO: Logger
									}
								}
								long Relative_Time = 24000 - world.getTime();
								world.setFullTime(world.getFullTime() + Relative_Time);
								log("" + debug);
								if(debug){
									log("" + lang.get("settime." + daLang + "") + "...");
								} //TODO: Logger
							}
							
						}, 10 * 20);
						
					}else{
						
						isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else if(!sender.hasPermission("sps.hermits")){
					sender.sendMessage("" + lang.get("noperm." + daLang + " SinglePlayerSleep"));
				}
			}else{
				sender.sendMessage("" + lang.get("noperm." + daLang + " SinglePlayerSleep"));
			}
		}

		return true;
	}
	
	public  void log(String dalog){
		PluginBase.logger.info(this.getName() + " " + dalog);
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
					p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers." + daLang + ""));
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

	
	public static boolean IsNight(World w)
    {
    	long time = (w.getFullTime()) % 24000;
    	return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }

}
