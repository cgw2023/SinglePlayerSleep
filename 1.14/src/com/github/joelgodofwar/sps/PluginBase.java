package com.github.joelgodofwar.sps;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import com.github.joelgodofwar.sps.api.Ansi;
//import net.md_5.bungee.api.chat.ClickEvent;
//import net.md_5.bungee.api.chat.ComponentBuilder;
//import net.md_5.bungee.api.chat.HoverEvent;
//import net.md_5.bungee.api.chat.TextComponent;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.api.ConfigAPI;
import com.github.joelgodofwar.sps.api.JsonMessages;


/**
 * @author JoelGodOfWar(JoelYahwehOfWar)
 * some code added by ColdCode(coldcode69)
 */

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
	public int transitionTaskUnrestricted = 1;
	public long pTime = 0;
	public Map<String, Long> playersCancelled = new HashMap<String, Long>();
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
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		
		ConfigAPI.CheckForConfig(this);
		ConfigAPI.Reloadconfig(this, null);
		getServer().getPluginManager().registerEvents(this, this);
		
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		}catch (Exception e){
			// Failed to submit the stats
		}
		consoleInfo("enabled");
		log("MC v" + Bukkit.getVersion() + " debug=" + debug + " in " + this.getDataFolder() + "/config.yml");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		if(debug==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto-update-check=" + getConfig().getBoolean("auto-update-check"));
			logDebug("unrestrictedsleep=" + getConfig().getBoolean("unrestrictedsleep"));
			logDebug("namecolor=" + getConfig().getString("namecolor"));
			logDebug("cancelcolor=" + getConfig().getString("cancelcolor"));
			logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast"));
			logDebug("sleeplimit=" + getConfig().getInt("sleeplimit"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
		}
	}
	
	@Override // TODO:
	public void onDisable() {
		consoleInfo("disabled");		
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginBase.logger.info(Ansi.YELLOW + "**************************************" + Ansi.SANE);
		PluginBase.logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.SANE + " is " + state);
		PluginBase.logger.info(Ansi.YELLOW + "**************************************" + Ansi.SANE);
	}
	
	public String nameColor() {
    	//Only change name colours if one is set
    	if (!getConfig().getString("namecolor").contains("NONE")) {
    		String nameColor = ChatColorUtils.setColors(getConfig().getString("namecolor"));
    		return nameColor;
    	} else {
    		return "";
    	}
	}
	
	
	@EventHandler
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException{
		List<World> worlds = Bukkit.getWorlds();
		//boolean debug = getConfig().getBoolean("debug");
		final Player player = event.getPlayer();
		if(debug){logDebug(" PIS player set. ...");}
		final World world = player.getWorld();
		if(debug){logDebug(" PIS world set. ...");}

		if(getServer().getPluginManager().getPlugin("EssentialsX") != null){
		log("perm essentials.sleepingignored=" + player.hasPermission("essentials.sleepingignored"));
			if(player.hasPermission("essentials.sleepingignored") && !player.isOp()){
				player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + " you have the permission (" + ChatColor.GOLD + 
						"essentials.sleepingignored" + ChatColor.YELLOW + 
						") which is conflicting with SinglePlaySleep. Please ask for it to be removed. " + ChatColor.RED + "WARNING! ");
				logWarn("Player " + player.getName() + "has the permission " + Ansi.RED + "essentials.sleepingignored" + Ansi.SANE + " which is known to conflict with SinglePlayerSleep.");
				return;
			}
		}
		
		
		//Check it's night or if storm
		if (IsNight(worlds.get(0))||player.getWorld().isThundering()) {
			
			log(player.getName() + " is sleeping.");
			long time = System.currentTimeMillis() / 1000;
			
			//Set default timer for when the player has never slept before
			long timer = 0;
			
			//check if player has already tried sleeping to prevent spam
			if (getConfig().getInt("sleeplimit") > 0) {
				timer = time - pTime;
			}
			
				//Tell the player why they can't sleep
				if (timer < getConfig().getInt("sleeplimit")) {			    
					String sleeplimit = "" + lang.get("sleeplimit." + daLang + "").toString();
					player.sendMessage(ChatColor.YELLOW + sleeplimit);			
				} else {
					
					//Save the time the player last tried to sleep, skip if player has unrestricted sleep since it will always be successful
					if (!player.hasPermission("sps.unrestricted")) {
						pTime = (int) time;
					}
					
					//Check if players can sleep without the ability for others to cancel it
					if (getConfig().getBoolean("unrestrictedsleep")) {
						String dastring = "" + lang.get("issleep." + daLang + "");
						dastring = dastring.replace("<player>", player.getDisplayName());
						this.broadcast(dastring);
						transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
							public void run() {
								setDatime(player, world);
							}						
						}, 10 * 20);
					} else {
						
						//Don't show cancel option if player has unrestricted sleep perm
						if (player.hasPermission("sps.unrestricted")) {
							
							//Broadcast "player is sleeping"
							String dastring = "" + lang.get("issleep." + daLang + "");
							dastring = dastring.replace("<player>", player.getDisplayName());
							this.broadcast(dastring);
							
							transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								public void run() {
									setDatime(player, world);
								}						
							}, 10 * 20);
							
						} else {
							
							if(player.hasPermission("sps.hermits")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
								if(debug){logDebug(" PIS Has perm or is op. ...");}
								
								//Broadcast to Server
								String dastring = "" + lang.get("issleep." + daLang + "");
									
								dastring = dastring.replace("<player>", "");
								//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
							    //String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
							    String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
							    String sleepmsg;
							    if (getConfig().getBoolean("randomsleepmsgs")){
							    	int maxmsgs = getConfig().getInt("numberofsleepmsgs");
							    	int randomnumber = RandomNumber(maxmsgs);
							    	sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
							    }else{
							    	sleepmsg = getConfig().getString(ChatColor.WHITE + "<player> is sleeping");
							    }
							    	String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
							    	if(debug){logDebug(" PIS ... msgcolor=" + msgcolor);}
							    damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
							    //damsg = damsg.replace(" is sleeping [", dastring + " [");
							    	String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
							    	if(debug){logDebug(" PIS ... playercolor=" + playercolor);}
								damsg = damsg.replace("<player>", playercolor + player.getDisplayName() + msgcolor);
									String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
									if(debug){logDebug(" PIS ... cancelcolor=" + cancelcolor);}
								damsg = damsg.replace("dacancel", cancelcolor + lang.get("cancel." + daLang + "") + msgcolor);
								//change cancel color based on config
								damsg = damsg.replace("tooltip", "" + lang.get("clickcancel." + daLang + ""));
								if(debug){logDebug(" PIS string processed. ...");}
								
								JsonMessages.SendAllJsonMessage(damsg);
								if(debug){logDebug(" PIS SendAllJsonMessage. ...");}

								//Thread.sleep(10000);
								if(!isCanceled){
									if(debug){logDebug(" PIS !isCanceled. ...");}
									transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					
										public void run() {
											setDatime(player, world);
											if(debug){logDebug(" PIS setDatime has run. ...");}
										}
										
									}, 10 * 20);
									
								}else{
									
									isCanceled = false;
								}
								//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
							}else{ //Player doesn't have permission so tell them
								player.sendMessage("" + lang.get("noperm." + daLang + ""));
							}
						}
					}
				}
		}else{ //It is not Night or Storming so tell the player
			player.sendMessage(ChatColorUtils.setColors("" + lang.get("nightorstorm." + daLang + "")));
			if(debug){logDebug(" it was not night and player was notified. ...");}
			
			//if (player.getBedSpawnLocation().equals(null)) {
				Block bed = event.getBed();
				player.setBedSpawnLocation(event.getBed().getLocation());
				player.sendMessage("Your Bedspawn has been set to X:" + bed.getX() + " , Y:" + bed.getY() + " , Z:" + bed.getZ());
				if(debug){logDebug(" bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");}
			//}
		}
	}

	
/** 
	@EventHandler
	public void onClickBlock(PlayerInteractEvent event) {
	      Player player = event.getPlayer();
	      Block block = event.getClickedBlock();
	      //List<Material>  material = Material.WHITE_BED;
	      if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
	    	  //if(debug){logDebug(" player right clicked");}
	    	  if(debug){logDebug(" player right clicked " + event.getClickedBlock().getType().toString());}
	    	  if (event.getClickedBlock().getType().toString().contains("_BED")) {
	    		  if(debug){logDebug(" contains _BED in name");}
	    		  if (!IsNight(player.getWorld())) {
	    			  player.setBedSpawnLocation(event.getClickedBlock().getLocation());
	    			  player.sendMessage("Your Bedspawn has been set to X:" + block.getX() + " , Y:" + block.getY() + " , Z:" + block.getZ());
	    		  }
	    	  }
	      }
	      
	}
**/
	

	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(player.hasPermission("sps.downfall")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
				world.setStorm(false);
				if(debug){logDebug("" + lang.get("setdownfall." + daLang + "") + "...");}
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
				world.setThundering(false);
				if(debug){logDebug("" + lang.get("setthunder." + daLang + "") + "...");}
			}
		}
		long Relative_Time = 24000 - world.getTime();
		world.setFullTime(world.getFullTime() + Relative_Time);
		if(debug){logDebug("" + lang.get("settime." + daLang + "") + "...");}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	    if (cmd.getName().equalsIgnoreCase("SPS"))
	    {
	      if (args.length == 0)
	      {
	    	sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	    	sender.sendMessage(ChatColor.YELLOW + " " + lang.get("touse." + daLang + ""));//Sleep in a bed to use.");
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
	    	  if(sender.isOp()||sender.hasPermission("sps.op")||sender.hasPermission("sps.*")){
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
			  if(sender.isOp()||sender.hasPermission("sps.op")||!(sender instanceof Player)||sender.hasPermission("sps.*")){
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
			if(sender.isOp() && UpdateCheck||sender.hasPermission("sps.op") && UpdateCheck||sender.hasPermission("sps.*") && UpdateCheck){	
				try {
				
					URL url = new URL("https://raw.githubusercontent.com/JoelGodOfwar/SinglePlayerSleep/master/version.txt");
					final URLConnection conn = url.openConnection();
		            conn.setConnectTimeout(5000);
		            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		            final String response = reader.readLine();
		            final String localVersion = this.getDescription().getVersion();
		            if(debug){this.logDebug("response= ." + response + ".");} //TODO: Logger
		            if(debug){this.logDebug("localVersion= ." + localVersion + ".");} //TODO: Logger
		            if (!response.equalsIgnoreCase(localVersion)) {
		            	sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers." + daLang + ""));
					}else{
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.GREEN + " " + lang.get("curvers." + daLang + ""));
					}
				} catch (MalformedURLException e) {
					this.logDebug("MalformedURLException");
					e.printStackTrace();
				} catch (IOException e) {
					this.logDebug("IOException");
					e.printStackTrace();
				}catch (Exception e) {
					this.logDebug("Exception");
					e.printStackTrace();
				}
				
			}else{
				sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("notop." + daLang + ""));
			}
		  }
	    }
		
		if(cmd.getName().equalsIgnoreCase("cancel")){
			if(debug){logDebug("CMD Can command cancel selected");}
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			if(sender.isOp() || sender.hasPermission("sps.cancel") || sender.hasPermission("sps.op")||sender.hasPermission("sps.*")) {
				if(sender.isOp()){if(debug){logDebug("CMD Can " + sender.getName() + " is OP");}}
				if(sender.hasPermission("sps.cancel")){if(debug){logDebug("CMD Can " + sender.getName() + " has sps.cancel");}}
				if(sender.hasPermission("sps.op")){if(debug){logDebug("CMD Can " + sender.getName() + " has sps.op");}}
				
				//Check it's night
				if (IsNight(worlds.get(0))||worlds.get(0).hasStorm()) {
					if(debug){if(IsNight(worlds.get(0))){logDebug("CMD Can It is night");}}
					if(debug){if(worlds.get(0).hasStorm()){logDebug("CMD Can it is storming");}}
					
					//Prevent cancelling if unrestricted sleep is enabled
					if (!getConfig().getBoolean("unrestrictedsleep")) {
						if(debug){logDebug("CMD Can !unrestricted sleep");}
						
						//Check if this is an unrestricted sleep or not
						if (Bukkit.getScheduler().isCurrentlyRunning((transitionTask)) || Bukkit.getScheduler().isQueued((transitionTask))) {
							if(debug){logDebug("CMD Can runnable is scheduled");}
							
							long time = System.currentTimeMillis() / 1000;
							
							//Set default timer
							long timer = 0;
	
							long pTimeCancel = 0;
							if (playersCancelled.get(sender.getName()) != null) {
								pTimeCancel = playersCancelled.get(sender.getName());
								if(debug){logDebug("CMD Can playerscancelled is not null");}
							}
	
							//check if player has already tried cancelling to prevent spam
							if (getConfig().getInt("sleeplimit") > 0) {
								timer = time - pTimeCancel;
								if(debug){logDebug("CMD Can timer is: " + timer);}
							}
							
							//Tell the player why they can't sleep
							if (timer < getConfig().getInt("sleeplimit")) {	    
								String sleeplimit = "" + lang.get("sleeplimit." + daLang + "").toString();
								sender.sendMessage(ChatColor.YELLOW + sleeplimit);
								if(debug){logDebug("CMD Can tell player why they cant sleep");}
							} else {
								if(debug){logDebug("CMD Can sleeplimit not reached");}
								
								//Set the time this player cancelled to prevent spam
								playersCancelled.put(sender.getName().toString(), time);
								
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(transitionTask);
								
								//Broadcast to Server
								//if(cancelbroadcast){this.broadcast("" +  ChatColorUtils.setColors("" + lang.get("canceledsleep." + daLang + "")).replace("<player>", sender.getName()));}
								
								//Broadcast to Server
								if (!(cancelbroadcast == false)) {
									this.broadcast(lang.get("canceledsleep." + daLang + "").toString().replace("<player>", nameColor() + sender.getName() + ChatColor.RESET));
									if(debug){logDebug("CMD Can broadcast sent");}
								}else if (cancelbroadcast == false){
									if(debug){logDebug("CMD Can broadcast = false");}
								}
								isCanceled = true;
								double oldHealth;
								Player player = null;
								Location location;
								Location bedspawn;
								
								//Sleep canceled so kick players from beds.
								for (Player p: Bukkit.getOnlinePlayers()){
									player = p;// ((CraftPlayer)p);
									if(debug){logDebug("CMD Can cycling player " + player.getDisplayName());}
									
									if(debug){logDebug("CMD Can cancel player=" + player.getDisplayName());}
									
									try {
										bedspawn = player.getBedSpawnLocation();
										bedspawn = new Location(bedspawn.getWorld(), bedspawn.getBlockX(),bedspawn.getBlockY(),bedspawn.getBlockZ(),0,0);
										if(debug){logDebug("CMD Can bedspawn=" + bedspawn);}
										location = player.getLocation();
										location = new Location(location.getWorld(), location.getBlockX(),location.getBlockY(),location.getBlockZ(),0,0);
										if(debug){logDebug("CMD Can location=" + location);}
										boolean inbed = false;
										
										if (location.equals(bedspawn)){
											inbed = true;
											}
										else{
											location.add(1, 0, 0);
											if(debug){logDebug("CMD Can location1=" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.add(0, 0, 1);
											if(debug){logDebug("CMD Can location=2" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(debug){logDebug("CMD Can location=3" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(debug){logDebug("CMD Can location=4" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(debug){logDebug("CMD Can location=5" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(debug){logDebug("CMD Can location=6" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.add(1, 0, 0);
											if(debug){logDebug("CMD Can location=7" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
											location.add(1, 0, 0);
											if(debug){logDebug("CMD Can location8=" + location);}
											if(location.equals(bedspawn)){
												inbed = true;
											}
										}
										
										if(inbed){
											oldHealth = player.getHealth();
											if(debug){logDebug("CMD Can oldHEalth=" + oldHealth);}
											if(!(oldHealth <= 1)){
												player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
												if(debug){logDebug("CMD Can damage=" + player.getHealth());}
												player.setHealth(oldHealth);
											}else{
												player.setHealth(oldHealth + 1);
												player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
												if(debug){logDebug("CMD Can damage=" + player.getHealth());}
												player.setHealth(oldHealth);
											}
											
										}
									}catch (Exception e){
										log("[Exception] " + player.getDisplayName() + " has never slept before.");
										// Failed to submit the stats
									}
								}
							}
							
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + lang.get("nocancel." + daLang + ""));
						}
						
					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + "" + lang.get("cancelunrestricted." + daLang + ""));
					}
						
				}else { //it's not night tell player
					sender.sendMessage(ChatColor.YELLOW + "" + lang.get("mustbenight." + daLang + ""));
				}
			}else { //Player doesn't have permission so let's tell them
				sender.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang + ""));
			}
			if(isCanceled){
				isCanceled = false;
			}
		}
		if(cmd.getName().equalsIgnoreCase("sleep")){
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			//World w = ((Entity) sender).getWorld();
			if(!IsNight(worlds.get(0)) && !worlds.get(0).hasStorm()){
				sender.sendMessage(ChatColorUtils.setColors("" + lang.get("nightorstorm." + daLang + "")));
				return false;
			}
			
			if(sender.hasPermission("sps.command")||sender.hasPermission("sps.op")||sender.hasPermission("sps.*")) {
				//final Player player1 = (Player) sender;
				final CommandSender daSender = sender;

				final World world = worlds.get(0);

				//Broadcast to Server
				String sleepmsg;
				if (getConfig().getBoolean("randomsleepmsgs")){
			    	int maxmsgs = getConfig().getInt("numberofsleepmsgs");
			    	int randomnumber = RandomNumber(maxmsgs);
			    	sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
			    }else{
			    	sleepmsg = getConfig().getString(ChatColor.WHITE + "<player> is sleeping");
			    }
			    	String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String dastring = "" + lang.get("sleepcommand." + daLang + "");
				dastring = dastring.replace("<player>", "");
				//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
				String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
					String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
					String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				damsg = damsg.replace("<player>", playercolor + sender.getName() + msgcolor1);
					String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				damsg = damsg.replace("dacancel", cancelcolor + lang.get("cancel." + daLang + "") + msgcolor1);
				damsg = damsg.replace("tooltip", "" + lang.get("clickcancel." + daLang + ""));
				JsonMessages.SendAllJsonMessage(damsg);
				if(sender.hasPermission("sps.hermits")||sender.hasPermission("sps.*")){
					//Thread.sleep(10000);
					
					if(!isCanceled){
						
						transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

							public void run() {
								//getLogger().info("runnable");
								//setDatime(sender, world);
								if(world.hasStorm()){
									if(daSender.hasPermission("sps.downfall")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setStorm(false);
										if(debug){logDebug("" + lang.get("setdownfall." + daLang + "") + "...");}
									}
								}
								if(world.isThundering()){
									if(daSender.hasPermission("sps.thunder")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setThundering(false);
										if(debug){logDebug("" + lang.get("setthunder." + daLang + "") + "...");}
									}
								}
								long Relative_Time = 24000 - world.getTime();
								world.setFullTime(world.getFullTime() + Relative_Time);
								if(debug){logDebug("" + lang.get("settime." + daLang + "") + "...");}
							}
							
						}, 10 * 20);
						
					}else{
						
						isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else{
					sender.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang + ""));
				}
			}else{
				sender.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang + ""));
			}
		}

		return true;
	}
	
	public  void log(String dalog){
		PluginBase.logger.info("~" + this.getName() + " " + dalog);
	}
	public  void logDebug(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + " [DEBUG] " + Ansi.SANE + dalog);
	}
	public void logWarn(String dalog){
		final Logger logger = Bukkit.getLogger();
		PluginBase.logger.warning("~" + this.getName() + " " + dalog);
	}
	public void broadcast(String message){
		getServer().broadcastMessage("" + message);
	}
	  
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	  {
	    Player p = event.getPlayer();
	    if(p.isOp() && UpdateCheck||p.hasPermission("sps.*")){	
			try {
			
				URL url = new URL("https://raw.githubusercontent.com/JoelGodOfwar/SinglePlayerSleep/master/versioncheck/1.14/version.txt");
				//TODO: change MC version for correct version checking.
				final URLConnection conn = url.openConnection();
	            conn.setConnectTimeout(5000);
	            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            final String response = reader.readLine();
	            final String localVersion = this.getDescription().getVersion();
	            if(debug){this.logDebug("response= ." + response + ".");} //TODO: Logger
	            if(debug){this.logDebug("localVersion= ." + localVersion + ".");} //TODO: Logger
	            if (!response.equalsIgnoreCase(localVersion)) {
					p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers." + daLang + "") + ChatColor.WHITE + " v" + response);
				}
			} catch (MalformedURLException e) {
				this.logDebug("MalformedURLException");
				e.printStackTrace();
			} catch (IOException e) {
				this.logDebug("IOException");
				e.printStackTrace();
			}catch (Exception e) {
				this.logDebug("Exception");
				e.printStackTrace();
			}
			
		}
	    if(p.getDisplayName().equalsIgnoreCase("JoelYahwehOfWar")){
	    	p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
	    	//p.sendMessage("seed=" + p.getWorld().getSeed());
	    }
	}

	
	public static boolean IsNight(World w)
    {
    	long time = (w.getFullTime()) % 24000;
    	return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }
	
	public int RandomNumber(int maxnum){
		Random rand = new Random();
		int min = 1;
		int max = maxnum;
		// nextInt as provided by Random is exclusive of the top value so you need to add 1 
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

}
