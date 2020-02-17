package com.github.joelgodofwar.sps;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.sps.api.Ansi;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.api.Metrics;
import com.github.joelgodofwar.sps.api.SendJsonMessages;
import com.github.joelgodofwar.sps.api.UpdateChecker;


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
	public boolean isDSCanceled = false;
	public int transitionTask = 0;
	public int dayskipTask = 0;
	public int transitionTaskUnrestricted = 1;
	public long pTime = 0;
	public Map<String, Long> playersCancelled = new HashMap<String, Long>();
	private URL url;
	private static long mobSpawningStartTime = 12600;
	//mobs stop spawning at: 22813
	//mobs start to burn at: 23600
	private static long mobSpawningStopTime = 23600;
	File langFile;
    FileConfiguration lang;
    String updateURL = "https://raw.githubusercontent.com/JoelGodOfwar/SinglePlayerSleep/master/versioncheck/1.13/version.txt";
    boolean UpdateAvailable =  false;
    
	
	@Override // TODO:
	public void onEnable(){
		UpdateCheck = getConfig().getBoolean("auto_update_check");
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginBase.logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		PluginBase.logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " Loading...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		if(debug){logDebug("datafolder=" + getDataFolder());}
		langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");//\
		if(debug){logDebug("langFilePath=" + langFile.getPath());}
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_TW.yml", true);
			log("lang file not found! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_TW.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		String checklangversion = lang.getString("langversion");
		if(checklangversion != null&&checklangversion == "2.13.34"){
			//Up to date do nothing
		}else{
			// outdated, update them then
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_TW.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_TW.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
		}
		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				log("Data Folder doesn't exist");
				log("Creating Data Folder");
				getDataFolder().mkdirs();
				log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			log("" + file);
			if(!file.exists()){
				log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/** end config check */
		// Check config.yml if it is up to date.
		String oldConfig = getDataFolder() + File.separator + "config.yml";
		boolean needConfigUpdate = false;
		try {
			needConfigUpdate = fileContains(oldConfig, "notifymustbenight");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(needConfigUpdate == false){
			YamlConfiguration sleepmsgs = new YamlConfiguration();
			String[] strSleepMsgs = new String[getConfig().getInt("numberofsleepmsgs") + 1];
			for (int i = 1; i < (getConfig().getInt("numberofsleepmsgs") + 1); i++) {
				log("sleepmsg" + i + "=" + getConfig().getString("sleepmsg" + i));
				strSleepMsgs[i] = "" + getConfig().getString("sleepmsg" + i);
			}
			sleepmsgs.set("numberofsleepmsgs", getConfig().getInt("numberofsleepmsgs"));
			for (int i = 1; i < (getConfig().getInt("numberofsleepmsgs") + 1); i++) {
				sleepmsgs.set("sleepmsg" + i, strSleepMsgs[i]);
			}
			try {
				sleepmsgs.save(getDataFolder() + File.separator + "sleepmsgs_backup.yml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log("" + Ansi.GREEN + "Your sleepmsgs have been backed up to " + getDataFolder() + File.separator + "sleepmsgs_backup.yml");
			saveResource("config.yml", true);
			log("" + Ansi.GREEN + "config.yml has been updated");
		}else{
			//log("" + Ansi.GREEN + "not found");
		}
		// End config.yml check.
		/** Update Checker */
		if(UpdateCheck){
			try {
		        Bukkit.getConsoleSender().sendMessage("Checking for updates...");
		        UpdateChecker updater = new UpdateChecker(this, 68139);
				if(updater.checkForUpdates()) {
		        	UpdateAvailable = true;
		        	Bukkit.getConsoleSender().sendMessage(Ansi.YELLOW + this.getName() + Ansi.MAGENTA + " " + lang.get("newvers") + Ansi.RESET);
		        	Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
		        }else{
		        	UpdateAvailable = false;
		        }
		    }catch(Exception e) {
		        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
		    }
		}
		/** end update checker */
		getServer().getPluginManager().registerEvents(this, this);
		
		consoleInfo(Ansi.BOLD + "ENABLED" + Ansi.RESET);
		log("MC v" + Bukkit.getVersion() + " debug=" + debug + " in " + this.getDataFolder() + "/config.yml");
		
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("unrestrictedsleep=" + getConfig().getBoolean("unrestrictedsleep"));
			logDebug("waketime=" + getConfig().getString("waketime"));
			logDebug("sleepdelay=" + getConfig().getString("sleepdelay"));
			logDebug("dayskipdelay=" + getConfig().getString("dayskipdelay"));
			logDebug("unrestricteddayskipper=" + getConfig().getBoolean("unrestricteddayskipper"));
			logDebug("dayskipperitemrequired=" + getConfig().getBoolean("dayskipperitemrequired"));
			logDebug("cancelcolor=" + getConfig().getString("cancelcolor"));
			logDebug("sleepmsgcolor=" + getConfig().getString("sleepmsgcolor"));
			logDebug("playernamecolor=" + getConfig().getString("playernamecolor"));
			logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast"));
			logDebug("sleeplimit=" + getConfig().getInt("sleeplimit"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
			logDebug("randomsleepmsgs=" + getConfig().getBoolean("randomsleepmsgs"));
			logDebug("numberofsleepmsgs=" + getConfig().getString("numberofsleepmsgs"));
		}
		try {
			//PluginBase plugin = this;
			Metrics metrics  = new Metrics(this);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
		        @Override
		        public Map<String, Integer> call() throws Exception {
		            Map<String, Integer> valueMap = new HashMap<>();
		            int varTotal = myPlugins();
		            if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
		    		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
		    		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
		    		if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
		    		if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
		            return valueMap;
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("auto_update_check").toUpperCase();
		        }
		    }));
			// add to site
			metrics.addCustomChart(new Metrics.SimplePie("unrestrictedsleep", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("unrestrictedsleep").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_waketime", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("waketime").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_sleepdelay", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getInt("sleepdelay");
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("cancelbroadcast", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("cancelbroadcast").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("debug").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("lang").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("numberofsleepmsgs", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getInt("numberofsleepmsgs");
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("dayskipdelay", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getInt("dayskipdelay");
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("unrestricteddayskipper", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getBoolean("unrestricteddayskipper");
		        }
		    }));
		}catch (Exception e){
			// Failed to submit the stats
		}
		
	}
	
	@Override // TODO:
	public void onDisable() {
		consoleInfo(Ansi.BOLD + "DISABLED" + Ansi.RESET);
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginBase.logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		PluginBase.logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " is " + state);
		PluginBase.logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
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
		int sleepdelay = getConfig().getInt("sleepdelay", 10);
		int dayskipdelay = getConfig().getInt("dayskipdelay", 10);

		if(getServer().getPluginManager().getPlugin("EssentialsX") != null){
		log("perm essentials.sleepingignored=" + player.hasPermission("essentials.sleepingignored"));
			if(player.hasPermission("essentials.sleepingignored") && !player.isOp()){
				player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + " you have the permission (" + ChatColor.GOLD + 
						"essentials.sleepingignored" + ChatColor.YELLOW + 
						") which is conflicting with SinglePlaySleep. Please ask for it to be removed. " + ChatColor.RED + "WARNING! ");
				logWarn("Player " + player.getName() + "has the permission " + Ansi.RED + "essentials.sleepingignored" + Ansi.RESET + " which is known to conflict with SinglePlayerSleep.");
				return;
			}
		}
		/* Check if it's Day for DaySkipper */
		if(IsDay(player.getWorld())){
			if(debug){logDebug(" DS it is Day");}
			/* OK it's day check if it's a Black bed. */
			
			/* OK it is a Black bed, now check if they have the DaySkipper item. */
			/*String daMainHand = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
			log("daMainHand=" + daMainHand);
			String daOffHand = player.getInventory().getItemInOffHand().getItemMeta().getDisplayName();
			log("daOffHand=" + daOffHand);*/
			ItemStack[] inv = player.getInventory().getContents();
			if(debug){logDebug("passed itemstack");}
			boolean itmDaySkipper = false;
			if(debug){logDebug("itemdayskipper initilized");}
			for(ItemStack item:inv){
				
				if(!(item == null)){
					if(debug){logDebug("item=" + item.getType().name());}
					if(item.getItemMeta().getDisplayName().equalsIgnoreCase("DaySkipper")){
				   		itmDaySkipper = true;
				   		if(debug){logDebug("found the item");}
				   		break;
				   	}
				}
			}
			if(debug){logDebug("inventory iterator finished.");}
			if(!getConfig().getBoolean("dayskipperitemrequired", true)){itmDaySkipper = true;}
			if(itmDaySkipper){ //daMainHand.contentEquals("DaySkipper")||daOffHand.contentEquals("DaySkipper")||
				if(debug){logDebug(" DS item DaySkipper is in inventory.");}
				
				Block block = event.getBed();
				if (((Bed)block.getBlockData()).getMaterial().equals(Material.BLACK_BED)){
						if(debug){logDebug(" DS the bed is Black");}
					/* OK they have the DaySkipper item, now check for the permission*/
					if(player.hasPermission("sps.dayskipper")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
						if(debug){logDebug(" DS Has perm or is op. ...");}
						/* OK they have the perm, now lets notify the server and schedule the runnable */
						String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
						String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				    		if(debug){logDebug(" DS ... msgcolor=" + msgcolor);}
				    	String sleepmsg = "" + lang.get("dayskipmsg." + daLang,"<player> wants to sleep the day away...");
				    	damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
				    	String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				    		if(debug){logDebug(" DS ... playercolor=" + playercolor);}
				    	damsg = damsg.replace("<player>", playercolor + player.getDisplayName() + msgcolor);
						String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
							if(debug){logDebug(" DS ... cancelcolor=" + cancelcolor);}
						damsg = damsg.replace("dacancel", cancelcolor + lang.get("dayskipcancel") + msgcolor);
						//change cancel color based on config
						damsg = damsg.replace("tooltip", "" + lang.get("dayskipclickcancel"));
							if(debug){logDebug(" DS string processed. ...");}
					
						SendJsonMessages.SendAllJsonMessage(damsg);
						//player.sendTitle("", ChatColor.YELLOW + "" + lang.get("noperm"), 0, 150, 5);
						//Bukkit.broadcastMessage(damsg);
							if(debug){logDebug(" DS SendAllJsonMessage. ...");}
						//player.sendMessage("The item in your main hand is named: " + daName);
							if(!isDSCanceled){
								if(debug){logDebug(" DS !isDSCanceled. ...");}
								dayskipTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				
									public void run() {
										setDStime(player, world);
										if(debug){logDebug(" DS setDStime has run. ...");}
									}
									
								}, dayskipdelay * 20);
								
							}else{
								
								isDSCanceled = false;
							}
						return;
					}else{
						player.sendMessage(ChatColor.YELLOW + "" + lang.get("noperm"));
						//player.sendTitle("", ChatColor.YELLOW + "" + lang.get("noperm"), 0, 0, 5);
					}
				}else{
					player.sendMessage(ChatColor.YELLOW + "" + lang.get("dayskipblackbed"));/* NOT A BLACK BED */
				}
			}
		}
		//Check it's night or if storm
		if (IsNight(player.getWorld())||player.getWorld().isThundering()) {
			
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
					String sleeplimit = "" + lang.get("sleeplimit").toString();
					player.sendMessage(ChatColor.YELLOW + sleeplimit);			
				} else {
					
					//Save the time the player last tried to sleep, skip if player has unrestricted sleep since it will always be successful
					if (!player.hasPermission("sps.unrestricted")) {
						pTime = (int) time;
					}
					
					//Check if players can sleep without the ability for others to cancel it
					if (getConfig().getBoolean("unrestrictedsleep")) {
						String dastring = "" + lang.get("issleep");
						dastring = dastring.replace("<player>", player.getDisplayName());
						this.broadcast(dastring);
						transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
							public void run() {
								setDatime(player, world);
							}						
						}, sleepdelay * 20);
					} else {
						
						//Don't show cancel option if player has unrestricted sleep perm
						if (player.hasPermission("sps.unrestricted")) {
							
							//Broadcast "player is sleeping"
							String dastring = "" + lang.get("issleep");
							dastring = dastring.replace("<player>", player.getDisplayName());
							this.broadcast(dastring);
							
							transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								public void run() {
									setDatime(player, world);
								}						
							}, sleepdelay * 20);
							
						} else {
							
							if(player.hasPermission("sps.hermits")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
								if(debug){logDebug(" PIS Has perm or is op. ...");}
								
								//Broadcast to Server
								String dastring = "" + lang.get("issleep");
									
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
							    	sleepmsg = (ChatColor.WHITE + "<player> is sleeping");
							    }
							    //sleepmsg = "<player> Has passed Go, Collected their $200, and checked in at Old Kent Road!";
							    	String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
							    	if(debug){logDebug(" PIS ... msgcolor=" + msgcolor);}
							    	if(sleepmsg.length() > 54){
							    		sleepmsg = addChar(sleepmsg, msgcolor, 55);
							    	}
							    damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
							    //damsg = damsg.replace(" is sleeping [", dastring + " [");
							    	String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
							    	if(debug){logDebug(" PIS ... playercolor=" + playercolor);}
								damsg = damsg.replace("<player>", playercolor + player.getDisplayName() + msgcolor);
									String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
									if(debug){logDebug(" PIS ... cancelcolor=" + cancelcolor);}
								damsg = damsg.replace("dacancel", cancelcolor + lang.get("cancel") + msgcolor);
								//change cancel color based on config
								damsg = damsg.replace("tooltip", "" + lang.get("clickcancel"));
								if(debug){logDebug(" PIS string processed. ...");}
								
								SendJsonMessages.SendAllJsonMessage(damsg);
								if(debug){logDebug(" PIS SendAllJsonMessage. ...");}

								//Thread.sleep(10000);
								if(!isCanceled){
									if(debug){logDebug(" PIS !isCanceled. ...");}
									transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					
										public void run() {
											setDatime(player, world);
											if(debug){logDebug(" PIS setDatime has run. ...");}
										}
										
									}, sleepdelay * 20);
									
								}else{
									
									isCanceled = false;
								}
								//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
							}else{ //Player doesn't have permission so tell them
								player.sendMessage(ChatColor.YELLOW + "" + lang.get("noperm"));
							}
						}
					}
				}
		}else{ //It is not Night or Storming so tell the player
			if(getConfig().getBoolean("notifymustbenight")){
				player.sendMessage(ChatColorUtils.setColors("" + lang.get("nightorstorm")));
				if(debug){logDebug(" it was not night and player was notified. ...");}
			}
			
			//if (player.getBedSpawnLocation().equals(null)) {
				Block bed = event.getBed();
				player.setBedSpawnLocation(event.getBed().getLocation());
				player.sendMessage("" + lang.get("respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
				if(debug){logDebug(" bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");}
			//}
		}
	}

	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(player.hasPermission("sps.downfall")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
				world.setStorm(false);
				if(debug){logDebug("" + lang.get("setdownfall") + "...");}
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
				world.setThundering(false);
				if(debug){logDebug("" + lang.get("setthunder") + "...");}
			}
		}
		String waketime = getConfig().getString("waketime", "NORMAL");
		long timeoffset = 0;
		if(waketime.equalsIgnoreCase("early")||waketime.equalsIgnoreCase("23000")){
			timeoffset = 1000;
		}else{timeoffset = 0;}
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		world.setFullTime(world.getFullTime() + Relative_Time);
		if(debug){logDebug("" + lang.get("settime") + "...");}
	}
	
	public void setDStime(Player player, World world){
		int timeoffset = 10000;
		long Relative_Time = (24000 - world.getTime()) - timeoffset;
		world.setFullTime(world.getFullTime() + Relative_Time);
		if(debug){logDebug("" + lang.get("dayskipsettime") + "...");}
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	    if (cmd.getName().equalsIgnoreCase("SPS")){
	      if (args.length == 0){
	    	sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	    	sender.sendMessage(ChatColor.YELLOW + " " + lang.get("touse"));//Sleep in a bed to use.");
	    	sender.sendMessage(ChatColor.WHITE + " ");
	    	sender.sendMessage(ChatColor.WHITE + " /Sleep - " + lang.get("sleephelp"));//subject to server admin approval");
	    	sender.sendMessage(ChatColor.WHITE + " /Cancel - " + lang.get("cancelhelp"));//Cancels SinglePlayerSleep");
	    	sender.sendMessage(ChatColor.WHITE + " ");
	        if(sender.isOp()||sender.hasPermission("sps.op")){
	        	sender.sendMessage(ChatColor.GOLD + " OP Commands");
	        	sender.sendMessage(ChatColor.GOLD + " /SPS update - " + lang.get("spsupdate"));//Check for update.");
	        	sender.sendMessage(ChatColor.GOLD + " /SPS reload - " + lang.get("spsreload"));//Reload config file.");
	        	//sender.sendMessage(ChatColor.GOLD + " /SPS check true/false - " + lang.get("spscheck"));//set auto-update-check to true or false.");
	        }
	        sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	        return true;
	      }
	      /*if(args[0].equalsIgnoreCase("check")){
	    	  if(args.length< 1){
					return false;
			  }
	    	  if(sender.isOp()||sender.hasPermission("sps.op")||sender.hasPermission("sps.*")){
	    		  if(!args[1].equalsIgnoreCase("true") & !args[1].equalsIgnoreCase("false")){
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " §c" + lang.get("boolean") + ": /sps check True/False");
	    		  }else if(args[1].contains("true") || args[1].contains("false")){
	    			    FileConfiguration config = getConfig();
						config.set("auto-update-check", "" + args[1]);
						
						saveConfig();
						ConfigAPI.Reloadconfig(this, null);
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("checkset") + " " + args[1]);
						if(args[1].contains("false")){
							sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("nocheck"));
						}else if(args[1].contains("true")){
							sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("yescheck"));
						}
						reloadConfig();
						return true;
					}
	    	  }
	    	  
	      }*/
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
				
					URL url = new URL(updateURL);
					final URLConnection conn = url.openConnection();
		            conn.setConnectTimeout(5000);
		            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		            final String response = reader.readLine();
		            final String localVersion = this.getDescription().getVersion();
		            if(debug){this.logDebug("response= ." + response + ".");} //TODO: Logger
		            if(debug){this.logDebug("localVersion= ." + localVersion + ".");} //TODO: Logger
		            if (!response.equalsIgnoreCase(localVersion)) {
		            	sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers"));
					}else{
						sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.GREEN + " " + lang.get("curvers"));
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
				sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("notop"));
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
				/* Check if it's Day */
				if(IsDay(worlds.get(0))){
					if(debug){logDebug("CMD Can It is Day");}
					if (!getConfig().getBoolean("unrestricteddayskipper")) {
						if(debug){logDebug("CMD Can !unrestricted DaySkipper");}
						if (Bukkit.getScheduler().isCurrentlyRunning((dayskipTask)) || Bukkit.getScheduler().isQueued((dayskipTask))) {
							if(debug){logDebug("CMD Can DS runnable is scheduled");}
							long time = System.currentTimeMillis() / 1000;
							//Set default timer
							long timer = 0;
							long pTimeCancel = 0;
							if (playersCancelled.get(sender.getName()) != null) {
								pTimeCancel = playersCancelled.get(sender.getName());
								if(debug){logDebug("CMD Can DS playerscancelled is not null");}
							}
							//check if player has already tried cancelling to prevent spam
							if (getConfig().getInt("sleeplimit") > 0) {
								timer = time - pTimeCancel;
								if(debug){logDebug("CMD Can DS timer is: " + timer);}
							}
							//Tell the player why they can't sleep
							if (timer < getConfig().getInt("sleeplimit")) {	    
								String sleeplimit = "" + lang.get("sleeplimit").toString();
								sender.sendMessage(ChatColor.YELLOW + sleeplimit);
								if(debug){logDebug("CMD Can DS tell player why they cant sleep");}
							} else {
								if(debug){logDebug("CMD Can DS sleeplimit not reached");}
								//Set the time this player cancelled to prevent spam
								playersCancelled.put(sender.getName().toString(), time);
								if(debug){logDebug("CMD Can DS added to playersCancelled");}
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(dayskipTask);
								if(debug){logDebug("CMD Can DS task cancelled");}
								//Broadcast to Server
								if(debug){logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast", false));}
								if (!(getConfig().getBoolean("cancelbroadcast", false) == false)) {
									if(debug){logDebug("CMD Can DS is it here?");}
									String damsg = "{\"text\":\"cancelmsg\"}";
									String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									damsg = damsg.replace("cancelmsg", lang.get("dayskipcanceled").toString());
									damsg = damsg.replace("<player>", playercolor + sender.getName() + msgcolor1);
									SendJsonMessages.SendAllJsonMessage(damsg);
									if(debug){logDebug("CMD Can DS broadcast sent");}
								}else if (getConfig().getBoolean("cancelbroadcast", false) == false){
									if(debug){logDebug("CMD Can DS broadcast = false");}
								}
								isCanceled = true;
								return true;
							}
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + lang.get("nocancel"));
						}
						
					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + "" + lang.get("cancelunrestricted"));
					}
						
				}else { //it's not night tell player
					//sender.sendMessage(ChatColor.YELLOW + "" + lang.get("mustbeday"));
				}
				
				//Check it's night
				if (IsNight(worlds.get(0))||worlds.get(0).hasStorm()) {
					if(debug){if(IsNight(worlds.get(0))){logDebug("CMD Can It is night");}}
					if(debug){if(worlds.get(0).hasStorm()){logDebug("CMD Can it is storming");}}
					
					//Prevent cancelling if unrestricted sleep is enabled
					if (!getConfig().getBoolean("unrestrictedsleep")) {
						if(debug){logDebug("CMD Can !unrestricted sleep");}
						
						//Check if this is an unrestricted sleep or not
						if (Bukkit.getScheduler().isCurrentlyRunning((transitionTask)) || Bukkit.getScheduler().isQueued((transitionTask))) {
							if(debug){logDebug("CMD Can sleep runnable is scheduled");}
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
								String sleeplimit = "" + lang.get("sleeplimit").toString();
								sender.sendMessage(ChatColor.YELLOW + sleeplimit);
								if(debug){logDebug("CMD Can tell player why they cant sleep");}
							} else {
								if(debug){logDebug("CMD Can sleeplimit not reached");}
								//Set the time this player cancelled to prevent spam
								playersCancelled.put(sender.getName().toString(), time);
								if(debug){logDebug("CMD Can added to playersCancelled");}
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(transitionTask);
								if(debug){logDebug("CMD Can task cancelled");}
								//Broadcast to Server
								
								if (!(getConfig().getBoolean("cancelbroadcast", false) == false)) {
									if(debug){logDebug("CMD Can is it here?");}
									String damsg = "{\"text\":\"<player> canceled sleeping.\"}";
									String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									damsg = damsg.replace("<player> canceled sleeping.", lang.get("canceledsleep").toString());
									damsg = damsg.replace("<player>", playercolor + sender.getName() + msgcolor1);
									SendJsonMessages.SendAllJsonMessage(damsg);
									if(debug){logDebug("CMD Can broadcast sent");}
								}else if (getConfig().getBoolean("cancelbroadcast", false) == false){
									if(debug){logDebug("CMD Can broadcast = false");}
								}
								isCanceled = true;
								//
								double oldHealth;
								Player player = null;
								Location location;
								Location bedspawn;
								
								//Sleep canceled so kick players from beds.
								for (Player p: Bukkit.getOnlinePlayers()){
									player = p;// ((CraftPlayer)p);
									if(debug){logDebug("CMD Can cycling player " + player.getDisplayName());}
									
									//if(debug){logDebug("CMD Can cancel player=" + player.getDisplayName());}
									
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
									return true;
								}
							}
							
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + lang.get("nocancel"));
						}
						
					} else { //unrestricted sleep is on tell the player
						sender.sendMessage(ChatColor.YELLOW + "" + lang.get("cancelunrestricted"));
					}
						
				}else { //it's not night tell player
					if(getConfig().getBoolean("notifymustbenight")){
						sender.sendMessage(ChatColor.YELLOW + "" + lang.get("mustbenight"));
					}
				}
			}else { //Player doesn't have permission so let's tell them
				sender.sendMessage(ChatColor.RED + "" + lang.get("noperm"));
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
				sender.sendMessage(ChatColorUtils.setColors("" + lang.get("nightorstorm")));
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
				String dastring = "" + lang.get("sleepcommand");
				dastring = dastring.replace("<player>", "");
				//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
				String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
					String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
					String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				damsg = damsg.replace("<player>", playercolor + sender.getName() + msgcolor1);
					String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				damsg = damsg.replace("dacancel", cancelcolor + lang.get("cancel") + msgcolor1);
				damsg = damsg.replace("tooltip", "" + lang.get("clickcancel"));
				SendJsonMessages.SendAllJsonMessage(damsg);
				if(sender.hasPermission("sps.hermits")||sender.hasPermission("sps.*")){
					//Thread.sleep(10000);
					
					if(!isCanceled){
						int sleepdelay = getConfig().getInt("sleepdelay", 10);
						transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

							public void run() {
								//getLogger().info("runnable");
								//setDatime(sender, world);
								if(world.hasStorm()){
									if(daSender.hasPermission("sps.downfall")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setStorm(false);
										if(debug){logDebug("" + lang.get("setdownfall") + "...");}
									}
								}
								if(world.isThundering()){
									if(daSender.hasPermission("sps.thunder")||daSender.hasPermission("sps.op")||daSender.hasPermission("sps.*")){
										world.setThundering(false);
										if(debug){logDebug("" + lang.get("setthunder") + "...");}
									}
								}
								long Relative_Time = 24000 - world.getTime();
								world.setFullTime(world.getFullTime() + Relative_Time);
								if(debug){logDebug("" + lang.get("settime") + "...");}
							}
							
						}, sleepdelay * 20);
						
					}else{
						
						isCanceled = false;
					}
					//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
				}else{
					sender.sendMessage(ChatColor.RED + "" + lang.get("noperm"));
				}
			}else{
				sender.sendMessage(ChatColor.RED + "" + lang.get("noperm"));
			}
		}
		if(cmd.getName().equalsIgnoreCase("dayskip")){
			//Player player = (Player) sender;
			List<World> worlds = Bukkit.getWorlds();
			//World w = ((Entity) sender).getWorld();
			if(!IsDay(worlds.get(0))){
				sender.sendMessage(ChatColorUtils.setColors("" + lang.get("mustbeday")));
				return false;
			}
			if(sender.hasPermission("sps.dayskipcommand")||sender.hasPermission("sps.op")||sender.hasPermission("sps.*")){
				if(debug){logDebug(" DS Has perm or is op. ...");}
				/* OK they have the perm, now lets notify the server and schedule the runnable */
				String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
				String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
		    		if(debug){logDebug(" DS ... msgcolor=" + msgcolor);}
		    	String sleepmsg = "" + lang.get("dayskipmsg." + daLang,"<player> wants to sleep the day away...<command>");
		    	damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
		    	String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
		    		if(debug){logDebug(" DS ... playercolor=" + playercolor);}
		    	damsg = damsg.replace("<player>", playercolor + sender.getName() + msgcolor);
				String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
					if(debug){logDebug(" DS ... cancelcolor=" + cancelcolor);}
				damsg = damsg.replace("dacancel", cancelcolor + lang.get("dayskipcancel") + msgcolor);
				//change cancel color based on config
				damsg = damsg.replace("tooltip", "" + lang.get("dayskipclickcancel"));
					if(debug){logDebug(" DS string processed. ...");}
			
					SendJsonMessages.SendAllJsonMessage(damsg);
					if(debug){logDebug(" DS SendAllJsonMessage. ...");}
					if(!isDSCanceled){
						final World world = worlds.get(0);
						int dayskipdelay = getConfig().getInt("dayskipdelay", 10);
						if(debug){logDebug(" DS !isDSCanceled. ...");}
						dayskipTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		
							public void run() {
								int timeoffset = 10000;
								long Relative_Time = (24000 - world.getTime()) - timeoffset;
								world.setFullTime(world.getFullTime() + Relative_Time);
								if(debug){logDebug("" + lang.get("dayskipsettime") + "...");}
							}
							
						}, dayskipdelay * 20);
						
					}else{
						
						isDSCanceled = false;
					}
			}
		}

		return true;
	}
	
	public  void log(String dalog){
		PluginBase.logger.info(Ansi.YELLOW + "" + this.getName() + " " + this.getDescription().getVersion() + Ansi.RESET + " " + dalog + Ansi.RESET);
	}
	public  void logDebug(String dalog){
		log(Ansi.MAGENTA + Ansi.BOLD + " [DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(Ansi.RED + Ansi.BOLD + " [WARNING] " + Ansi.RESET + dalog);
	}
	public void broadcast(String message){
		String damsg = "{\"text\":\"broadcastString\"}";
		String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
		damsg = damsg.replace("broadcastString", message);
		SendJsonMessages.SendAllJsonMessage(damsg);
		
		//getServer().broadcastMessage("" + message);
	}
	  
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	  {
	    Player p = event.getPlayer();
	    //if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){	
	    /** Notify Ops */
	    if(UpdateAvailable&&(p.isOp()||p.hasPermission("sps.showUpdateAvailable"))){
	    	p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers") + 
	    			" \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
	    }
			/**try {
			
				URL url = new URL(updateURL);
				//TODO: change MC version for correct version checking.
				final URLConnection conn = url.openConnection();
	            conn.setConnectTimeout(5000);
	            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            final String response = reader.readLine();
	            final String localVersion = this.getDescription().getVersion();
	            if(debug){this.logDebug("response= ." + response + ".");} //TODO: Logger
	            if(debug){this.logDebug("localVersion= ." + localVersion + ".");} //TODO: Logger
	            if (!response.equalsIgnoreCase(localVersion)) {
	            	logWarn(Ansi.YELLOW + this.getName() + Ansi.RED + " " + lang.get("newvers") + Ansi.WHITE + " v" + response + Ansi.RESET);
	            	if(p.isOp() && UpdateCheck||p.hasPermission("sps.showUpdateAvailable")){
	            		p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers") + ChatColor.WHITE + " v" + response);
	            	}
				}
			} catch (MalformedURLException e) {
				this.logDebug(this.getName() + " caught a MalformedURLException, and is still working");
				e.printStackTrace();
			} catch (IOException e) {
				this.logDebug(this.getName() + " caught an IOException, and is still working");
				e.printStackTrace();
			}catch (Exception e) {
				this.logDebug(this.getName() + " caught an Exception, and is still working");
				e.printStackTrace();
			}*/
			
		//}
	    if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
	    	p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
	    	//p.sendMessage("seed=" + p.getWorld().getSeed());
	    }
	}

	
	public static boolean IsNight(World w){
    	long time = (w.getFullTime()) % 24000;
    	return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }
	
	public static boolean IsDay(World w){
    	long time = (w.getFullTime()) % 24000;
    	return time > 0 && time < 12300;
    	//return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }
	
	public int RandomNumber(int maxnum){
		Random rand = new Random();
		int min = 1;
		int max = maxnum;
		// nextInt as provided by Random is exclusive of the top value so you need to add 1 
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	
	@SuppressWarnings("resource")
	public boolean fileContains(String filePath, String searchQuery) throws IOException
    {
        searchQuery = searchQuery.trim();
        BufferedReader br = null;

        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.contains(searchQuery))
                {
                	//log(Ansi.GREEN + "findstring found");
                	return true;
                }
                else
                {
                }
            }
        }
        finally
        {
            try
            {
                if (br != null)
                    br.close();
            }
            catch (Exception e)
            {
                System.err.println("Exception while closing bufferedreader " + e.toString());
            }
        }
        //log(Ansi.GREEN + "findstring failed");
        return false;
    }
	public String addChar(String str, String ch, int position) {
	    StringBuilder sb = new StringBuilder(str);
	    sb.insert(position, ch);
	    return sb.toString();
	}
	public int myPlugins(){
		//Plugin[] daPlugins = getServer().getPluginManager().getPlugins();
		int dacount = 1;
		if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){dacount++;}
		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){dacount++;}
		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){dacount++;}
		return dacount;
	}
	
}
