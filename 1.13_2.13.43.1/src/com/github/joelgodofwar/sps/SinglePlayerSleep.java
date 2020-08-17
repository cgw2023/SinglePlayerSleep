package com.github.joelgodofwar.sps;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.spectralmemories.bloodmoon.BloodmoonActuator;

import com.earth2me.essentials.Essentials;
import com.github.joelgodofwar.sps.api.ChatColorUtils;
import com.github.joelgodofwar.sps.api.Metrics;
import com.github.joelgodofwar.sps.api.StrUtils;
import com.github.joelgodofwar.sps.api.UpdateChecker;
import com.github.joelgodofwar.sps.api.YmlConfiguration;
import com.github.joelgodofwar.sps.utilities.Format;
/**
 * @author JoelGodOfWar(JoelYahwehOfWar)
 * some code added by ColdCode(coldcode69)
 */

@SuppressWarnings("unused")
public class SinglePlayerSleep extends JavaPlugin implements Listener{

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
	private static long mobSpawningStartTime = 12541;//12600;
	//mobs stop spawning at: 22813
	//mobs start to burn at: 23600
	private static long mobSpawningStopTime = 23600;
	File langFile;
	FileConfiguration lang;
	String updateURL = "https://raw.githubusercontent.com/JoelGodOfwar/SinglePlayerSleep/master/versioncheck/1.13/version.txt";
	boolean UpdateAvailable =  false;
	public static boolean displaycancel;
	public HashMap<UUID, Long> sleeplimit =  new HashMap<UUID, Long>();
	public HashMap<UUID, Long> cancellimit =  new HashMap<UUID, Long>();
	YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	public boolean isBloodMoon = false;
	public String jsonColorString = "\"},{\"text\":\"<text>\",\"color\":\"<color>\"},{\"text\":\"";
	
	@Override // TODO:
	public void onEnable(){
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		displaycancel = getConfig().getBoolean("display_cancel", true);
		log("displaycancel=" + displaycancel);
		config = new YmlConfiguration();
		oldconfig = new YamlConfiguration();
		
		PluginDescriptionFile pdfFile = this.getDescription();
		SinglePlayerSleep.logger.info("**************************************");
		SinglePlayerSleep.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " Loading...");
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
		if(!langFile.exists()){									// checks if the yaml does not exist
			langFile.getParentFile().mkdirs();					// creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "es_MX.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pl_PL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_TW.yml", true);
			log("lang file not found! copied cs_CZ.yml, de_DE.yml, en_US.yml, es_MX.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pl_PL.yml, pt_BR.yml, and zh_TW.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
		}
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		String checklangversion = lang.getString("langversion");
		if(checklangversion != null&&checklangversion.contains("2.13.41")){
			//Up to date do nothing
		}else{
			// outdated, update them then
			if(debug){logDebug("checklangversion='" + checklangversion + "'");}
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "es_MX.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pl_PL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_TW.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, es_MX.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pl_PL.yml, pt_BR.yml, and zh_TW.yml to " + getDataFolder() + "" + File.separatorChar + "lang");
		}
		File oldlangFile = new File(getDataFolder() + "" + File.separatorChar + "lang.yml");
		if(oldlangFile.exists()){
			oldlangFile.delete();
			log("Old lang.yml file deleted.");
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
		String oldConfig = new File(getDataFolder(), "config.yml").getPath().toString();//getDataFolder() + File.separator + "config.yml";
		boolean needConfigUpdate = false;
		try {
			needConfigUpdate = fileContains(oldConfig, "allow_rain_sleep");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(needConfigUpdate == false){
			try {
				copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml",getDataFolder() + "" + File.separatorChar + "old_config.yml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e2) {
				logWarn("Could not load config.yml");
				e2.printStackTrace();
			}
			saveResource("config.yml", true);
			try {
				config.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				logWarn("Could not load config.yml");
				e1.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "old_config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			config.set("auto_update_check", oldconfig.get("auto_update_check", true));
			config.set("debug", oldconfig.get("debug", false));
			config.set("lang", oldconfig.get("lang", "en_US"));
			config.set("clearrain_enabled", oldconfig.get("clearrain_enabled", false));
			config.set("unrestrictedsleep", oldconfig.get("unrestrictedsleep", false));
			config.set("waketime", oldconfig.get("waketime", "NORMAL"));
			config.set("sleepdelay", oldconfig.get("sleepdelay", 10));
			config.set("enabledayskipper", oldconfig.get("enabledayskipper", false));
			config.set("dayskipdelay", oldconfig.get("dayskipdelay", 10));
			config.set("unrestricteddayskipper", oldconfig.get("unrestricteddayskipper", false));
			config.set("dayskipperitemrequired", oldconfig.get("dayskipperitemrequired", true));
			config.set("cancelcolor", oldconfig.get("cancelcolor", "RED"));
			config.set("sleepmsgcolor", oldconfig.get("sleepmsgcolor", "STRIKETHROUGHYELLOW"));
			config.set("playernamecolor", oldconfig.get("playernamecolor", "WHITE"));
			config.set("display_cancel", oldconfig.get("display_cancel", true));
			config.set("cancelbroadcast", oldconfig.get("cancelbroadcast", true));
			config.set("sleeplimit", oldconfig.get("sleeplimit", 60));
			config.set("cancellimit", oldconfig.get("cancellimit", 60));
			config.set("notifymustbenight", oldconfig.get("notifymustbenight", false));
			config.set("randomsleepmsgs", oldconfig.get("randomsleepmsgs", true));
			config.set("numberofsleepmsgs", oldconfig.get("numberofsleepmsgs", 4));
			for (int i = 1; i < (getConfig().getInt("numberofsleepmsgs") + 1); i++) {
				config.set("sleepmsg" + i, oldconfig.get("sleepmsg" + i, "<player> is sleeping"));
			}
			try {
				config.save(new File(getDataFolder(), "config.yml"));
			} catch (IOException e) {
				logWarn("Could not save old settings to config.yml");
				e.printStackTrace();
			}
			log("config.yml has been updated");
		}else{
			//log("" + "not found");
		}
		// End config.yml check.
		/** Update Checker */
		if(UpdateCheck){
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				UpdateChecker updater = new UpdateChecker(this, 68139);
				if(updater.checkForUpdates()) {
					UpdateAvailable = true;
					Bukkit.getConsoleSender().sendMessage(this.getName() + " " + lang.get("newvers"));
					Bukkit.getConsoleSender().sendMessage(UpdateChecker.getResourceUrl());
				}else{
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
			}
		}
		/** end update checker */
		
		File  file = new File(getDataFolder(), "permissions.yml");
		log("" + file);
		if(!file.exists()){
			log("permissions.yml not found, creating! This is a sample only!");
			saveResource("permissions.yml", true);
		}
		getServer().getPluginManager().registerEvents(this, this);
		
		consoleInfo("ENABLED");
		log("This server is running " + Bukkit.getName() + " version " + Bukkit.getVersion() + " (Implementing API version " + Bukkit.getBukkitVersion() + ")");
		log("vardebug=" + debug + " debug=" + getConfig().get("debug","error") + " in " + this.getDataFolder() + "/config.yml");
		log("jarfilename=" + this.getFile().getAbsoluteFile());
		
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
			logDebug("unrestrictedsleep=" + getConfig().getBoolean("unrestrictedsleep"));
			logDebug("waketime=" + getConfig().getString("waketime"));
			logDebug("sleepdelay=" + getConfig().getString("sleepdelay"));
			logDebug("enabledayskipper=" + getConfig().getString("enabledayskipper"));
			logDebug("dayskipdelay=" + getConfig().getString("dayskipdelay"));
			logDebug("unrestricteddayskipper=" + getConfig().getBoolean("unrestricteddayskipper"));
			logDebug("dayskipperitemrequired=" + getConfig().getBoolean("dayskipperitemrequired"));
			logDebug("cancelcolor=" + getConfig().getString("cancelcolor"));
			logDebug("sleepmsgcolor=" + getConfig().getString("sleepmsgcolor"));
			logDebug("playernamecolor=" + getConfig().getString("playernamecolor"));
			logDebug("display_cancel=" + getConfig().getBoolean("display_cancel"));
			logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast"));
			logDebug("sleeplimit=" + getConfig().getInt("sleeplimit"));
			logDebug("cancellimit=" + getConfig().getInt("cancellimit"));
			logDebug("notifymustbenight=" + getConfig().getInt("notifymustbenight"));
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
					if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}
					if(getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null){valueMap.put("VillagerWorkstationHighlights", 1);}
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
			metrics.addCustomChart(new Metrics.SimplePie("enabledayskipper", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getBoolean("enabledayskipper");
				}
			}));
		}catch (Exception e){
			// Failed to submit the stats
		}
		
	}
	
	@Override // TODO:
	public void onDisable() {
		consoleInfo("DISABLED");
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		SinglePlayerSleep.logger.info("**************************************");
		SinglePlayerSleep.logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " is " + state);
		SinglePlayerSleep.logger.info("**************************************");
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
	
	
	/**
	 * @param event
	 * @throws InterruptedException
	 */
	@EventHandler
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException{
		if(debug){logDebug(ChatColor.RED + "** Start PlayerBedEnterEvent **");}
		List<World> worlds = Bukkit.getWorlds();
		//boolean debug = getConfig().getBoolean("debug");
		final Player player = event.getPlayer();
		if(debug){logDebug("PIS player set. ...");}
		final World world = player.getWorld();
		if(debug){logDebug(" PIS world set. ...");}
		int sleepdelay = getConfig().getInt("sleepdelay", 10);
		int dayskipdelay = getConfig().getInt("dayskipdelay", 10);
		event.getBedEnterResult();
		/** Debug info */
		if(debug){logDebug(ChatColor.RED + "**************************************************");}
		if(debug){logDebug(ChatColor.RED + "DEBUG LOG SHOULD CONTAIN THIS");}
		if(debug){logDebug("PIS 12786-23031 = Night, worldTime=" + world.getFullTime());}
		if(debug){logDebug("PIS isNight=" + IsNight(player.getWorld()) + " , isDay=" + IsDay(player.getWorld()));}
		if(debug){logDebug("PIS isOP=" + player.isOp());}
		if(debug){logDebug("PIS sps.Hermits=" + player.hasPermission("sps.hermits"));}
		if(debug){logDebug("PIS sps.unrestricted=" + player.hasPermission("sps.unrestricted"));}
		if(debug){logDebug("PIS sps.op=" + player.hasPermission("sps.op"));}
		if(debug){logDebug("PIS unrestrictedsleep=" + getConfig().getBoolean("unrestrictedsleep"));}
		if(debug){logDebug("PIS BedEnterResult=" + event.getBedEnterResult().toString());}
		if(debug){logDebug("PIS isRaining=" + event.getPlayer().getWorld().hasStorm());}
		if(debug){logDebug("PIS isThunderstorm=" + event.getPlayer().getWorld().isThundering());}
		//if(debug){logDebug("PIS isBloodMoon=" + isBloodMoon());}
		if(debug){logDebug(ChatColor.RED + "**************************************************");}

		if(getServer().getPluginManager().getPlugin("EssentialsX") != null||getServer().getPluginManager().getPlugin("Essentials") != null){
			if(debug){logDebug("perm essentials.sleepingignored=" + player.hasPermission("essentials.sleepingignored"));}
			if(player.hasPermission("essentials.sleepingignored") && !player.isOp()){
				player.sendMessage(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + " you have the permission (" + ChatColor.GOLD + 
						"essentials.sleepingignored" + ChatColor.YELLOW + 
						") which is conflicting with SinglePlaySleep. Please ask for it to be removed. " + ChatColor.RED + "WARNING! ");
				logWarn("Player " + player.getName() + "has the permission " + "essentials.sleepingignored" + " which is known to conflict with SinglePlayerSleep.");
				return;
			}
		}
		if(getConfig().getBoolean("enabledayskipper", false)){
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
							String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
							String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
								if(debug){logDebug(" DS ... msgcolor=" + msgcolor);}
							String sleepmsg = "" + lang.get("dayskipmsg." + daLang,"<player> wants to sleep the day away...");
							damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
							/** nickname parser */
							String nickName = getNickname(player);
							String playercolor = "";
							if(!nickName.contains("§")){
								//logWarn("nickName ! contain SS");
								playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
							}else{
								nickName = StrUtils.parseRGBNameColors(nickName);
							}
							/** end nickname parser */
								if(debug){logDebug(" DS ... playercolor=" + playercolor);}
							damsg = damsg.replace("<player>", playercolor + nickName + msgcolor);
							String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
								if(debug){logDebug(" DS ... cancelcolor=" + cancelcolor);}
							damsg = damsg.replace("dacancel", cancelcolor + lang.get("dayskipcancel") + msgcolor);
							//change cancel color based on config
							damsg = damsg.replace("tooltip", "" + lang.get("dayskipclickcancel"));
								if(debug){logDebug(" DS string processed. ...");}
								if(debug){logDebug(" DS damsg=" + damsg);}
								sendJson(damsg);//sendJson(ComponentSerializer.parse(damsg));
							//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
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
			}else{
				if(debug){logDebug("PIS isDay=false");}
			}
		}else{
			if(debug){logDebug("PIS enabledayskipper=false");}
		}
		//if(debug){logDebug("PIS getBedEnterResult=" + event.getBedEnterResult().toString());}
		if(!isBloodmoonInprogress(player.getWorld())){//isBloodmoonInprogress//isBloodMoon
			if(event.getBedEnterResult() == BedEnterResult.OK){
				//Check it's night or if storm
				if (IsNight(player.getWorld())||player.getWorld().isThundering()) {
					//Set default timer for when the player has never slept before
					long timer = 0;
					if(debug){logDebug("PIS IN... " + player.getName() + " is sleeping.");}
					long time = System.currentTimeMillis() / 1000;
					if(sleeplimit.get(player.getUniqueId()) == null){
						if(debug){logDebug("PIS sleeplimit UUID=null");}
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							sleeplimit.put(player.getUniqueId(), time);
							if(debug){logDebug("PIS IN... " + player.getDisplayName() + " added to playersSlept");}
						}
					}else{
						if(debug){logDebug("PIS sleeplimit UUID !null");}
						// Player is on the list.
						timer = sleeplimit.get(player.getUniqueId());
						if(debug){logDebug("time=" + time);}
						if(debug){logDebug("timer=" + timer);}
						if(debug){logDebug("time - timer=" +  (time - timer));}
						if(debug){logDebug("sleeplimit=" + getConfig().getLong("sleeplimit", 60));}
						// if !time - timer > limit
						if(!((time - timer) > getConfig().getLong("sleeplimit", 60))){
							long length = getConfig().getLong("sleeplimit", 60) - (time - timer) ;
							String sleeplimit = "" + lang.get("sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							if(debug){logDebug("PIS IN... sleeplimit: " + sleeplimit);}
							//player.sendMessage("You can not do that for " + length + " seconds");
							event.setCancelled(true);
							return;
						}else if((time - timer) > getConfig().getLong("sleeplimit", 60)){
							if(debug){logDebug("time - timer > sleeplimit");}
							sleeplimit.replace(player.getUniqueId(), time);
						}
					}
					
					/** /check if player has already tried sleeping to prevent spam
					if (getConfig().getInt("sleeplimit") > 0) {
						timer = time - pTime;
					}
					
						//Tell the player why they can't sleep
						if (timer < getConfig().getInt("sleeplimit")) {				
							String sleeplimit = "" + lang.get("sleeplimit").toString();
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							log("PIS IN... sleeplimit: " + sleeplimit);
						} else {
							
							//Save the time the player last tried to sleep, skip if player has unrestricted sleep since it will always be successful
							if (!player.hasPermission("sps.unrestricted")) {
								pTime = (int) time;
							}//  */
							
							//Check if players can sleep without the ability for others to cancel it
							if (getConfig().getBoolean("unrestrictedsleep")) {
								if(debug){logDebug("PIS unrestrictedsleep=true");}
								String dastring = "" + lang.get("issleep");
								dastring = dastring.replace("<player>", getNickname(player));
								this.broadcast(dastring, world);
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
									dastring = dastring.replace("<player>", getNickname(player));
									this.broadcast(dastring, world);
									
									transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										public void run() {
											setDatime(player, world);
										}						
									}, sleepdelay * 20);
									
								} else {
									if(!isCanceled&&!event.isCancelled()){
										if(player.hasPermission("sps.hermits")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
											if(debug){logDebug(" PIS Has perm or is op. ...");}
											
											//Broadcast to Server
											String dastring = "" + lang.get("issleep");
												
											dastring = dastring.replace("<player>", "");
											//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
											//String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}},{\"text\":\"]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
											String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
											String sleepmsg;
											if (getConfig().getBoolean("randomsleepmsgs")){
												int maxmsgs = getConfig().getInt("numberofsleepmsgs");
												int randomnumber = RandomNumber(maxmsgs);
												sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
												sleepmsg = sleepmsg.replace("<colon>", ":");
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

											/** nickname parser */
											String nickName = getNickname(player);
											String playercolor = "";
											if(!nickName.contains("§")){
												//logWarn("nickName ! contain SS");
												playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
											}else{
												nickName = StrUtils.parseRGBNameColors(nickName);
											}
											/** end nickname parser */
											
												//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
												if(debug){logDebug(" PIS ... playercolor=" + playercolor);}
											damsg = damsg.replace("<player>", playercolor + nickName + msgcolor);
												String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
												if(debug){logDebug(" PIS ... cancelcolor=" + cancelcolor);}
											damsg = damsg.replace("dacancel", cancelcolor + lang.get("cancel") + msgcolor);
											//change cancel color based on config
											damsg = damsg.replace("tooltip", "" + lang.get("clickcancel"));
											if(debug){logDebug(" PIS string processed. ...");}
											//String oldString = cancelcolor + lang.get("cancel") + msgcolor;
											//damsg = damsg.replace(oldString, "").replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");
											sendJson(damsg);//sendJson(ComponentSerializer.parse(damsg), cancelcolor + lang.get("cancel") + msgcolor);
											
											//SendJsonMessages.SendAllJsonMessage(damsg, cancelcolor + lang.get("cancel") + msgcolor, world);
											if(debug){logDebug(" PIS SendAllJsonMessage. ...");}
			
											//Thread.sleep(10000);
											if(!isCanceled&&!event.isCancelled()){
												if(debug){logDebug(" PIS !isCanceled. ...");}
												transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								
													public void run() {
														setDatime(player, world);
														if(debug){logDebug(" PIS setDatime has run. ...");}
													}
													
												}, sleepdelay * 20);
												
											}else{
												if(isCanceled){if(debug){logDebug("PIS isCanceled=" + isCanceled);}}
												if(event.isCancelled()){if(debug){logDebug("PIS event.isCanceled=" + event.isCancelled());}}
												isCanceled = false;
											}
											//player.sendMessage(ChatColor.RED + "isCanceled=" + isCanceled);
										}else{ //Player doesn't have permission so tell them
											player.sendMessage(ChatColor.YELLOW + "" + lang.get("noperm"));
										}
									}else{
										isCanceled = false;
										if(isCanceled){if(debug){logDebug("PIS isCanceled=" + isCanceled);}}
										if(event.isCancelled()){if(debug){logDebug("PIS event.isCanceled=" + event.isCancelled());}}
									}
								}
							}
						//}//
				}else{ //It is not Night or Storming so tell the player
					if(getConfig().getBoolean("notifymustbenight")){
						player.sendMessage(ChatColorUtils.setColors("" + lang.get("nightorstorm")));
						if(debug){logDebug(" it was not night and player was notified. ...");}
					}
					//if(debug){logDebug("getBedSpawnLocation=" + player.getBedSpawnLocation());}
					//if(debug){logDebug("getBed=" + event.getBed().getLocation());}
					//player.getBedSpawnLocation().equals(event.getBed().getLocation()
					String sv = serverVersion();
					if(!(Integer.parseInt(sv) >= 15)){
						Block bed = event.getBed();
						Location bedSpawn = player.getBedSpawnLocation();
						if(bedSpawn != null){
							boolean isSameBed = checkradius(bedSpawn, event.getBed().getLocation(), 5);
							if (!isSameBed||player.getBedSpawnLocation().equals(null)) {
								if(player.getBedSpawnLocation().equals(null)){
									if(debug){logDebug("bedspawn=null");}
								}else if(!isSameBed){
									if(debug){logDebug("bedspawn!=bed");}
								}
								player.setBedSpawnLocation(event.getBed().getLocation());
								player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + lang.get("respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
								if(debug){logDebug(" bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");}
							}
						}else{
							player.setBedSpawnLocation(event.getBed().getLocation());
							player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + lang.get("respawnpointmsg").toString().replace("<x>", "" + bed.getX()).replace("<z>", "" + bed.getZ()));
							if(debug){logDebug(" bedspawn was set for player " + ChatColor.GREEN + player.getDisplayName() + ChatColor.RESET + " ...");}
						}
					}else{
						if(debug){logDebug("Server is 1.15+");}
					}
				}
			}
		}else{
			player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + lang.get("bloodmoon", "You can not sleep during a bloodmoon.").toString());
			event.setCancelled(true);
		}
		if(debug){logDebug(ChatColor.RED + "** End PlayerBedEnterEvent **");}
		isCanceled =  false;
	}
	
	public boolean checkradius(Location player, Location event, int radius){
		double distance = player.distance(event);
		if(distance <= radius) {
			if(debug){logDebug("truedistance=" + distance);}
			return true;
			//shulker.teleport(block.getLocation());
		}
		if(debug){logDebug("falsedistance=" + distance);}
		return false;
	}
	
	public String serverVersion(){
		String v = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		String v2 = v.split("_")[1];
		return v2;
	}
	
	public void setDatime(Player player, World world){
		if(world.hasStorm()){
			if(player.hasPermission("sps.downfall")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
				world.setStorm(false);
				if(debug){logDebug("" + lang.get("setdownfall") + "...");}
			}else{
				if(debug){logDebug("" + getNickname(player) + " Does not have permission sps.downfall ...");}
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")||player.hasPermission("sps.op")||player.hasPermission("sps.*")){
				world.setThundering(false);
				if(debug){logDebug("" + lang.get("setthunder") + "...");}
			}else{
				if(debug){logDebug("" + getNickname(player) + " Does not have permission sps.thunder ...");}
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
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("SPS")){
			if (args.length == 0){
				sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
				sender.sendMessage(ChatColor.YELLOW + " " + lang.get("touse"));//Sleep in a bed to use.");
				sender.sendMessage(ChatColor.WHITE + " ");
				sender.sendMessage(ChatColor.WHITE + " /Sleep - " + lang.get("sleephelp"));//subject to server admin approval");
				sender.sendMessage(ChatColor.WHITE + " /spscancel - " + lang.get("cancelhelp"));//Cancels SinglePlayerSleep");
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
					SinglePlayerSleep plugin = this;
					getServer().getPluginManager().disablePlugin(plugin);
					getServer().getPluginManager().enablePlugin(plugin);
					reloadConfig();
					sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("reloaded"));
				}else if(!sender.hasPermission("sps.op")){
					sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("noperm"));
				}
			}
			if(args[0].equalsIgnoreCase("update")){
				// Player must be OP and auto-update-check must be true
			//if(sender.isOp() && UpdateCheck||sender.hasPermission("sps.op") && UpdateCheck||sender.hasPermission("sps.*") && UpdateCheck){	
			if(UpdateCheck&&UpdateAvailable&&(sender.isOp()||sender.hasPermission("sps.op")||sender.hasPermission("sps.*")||sender.hasPermission("sps.showUpdateAvailable"))){
				/** Notify Ops */
				//if(UpdateAvailable&&(p.isOp()||p.hasPermission("sps.showUpdateAvailable"))){
					sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers") + 
							" \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
				//}
			}else{
				sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("notop"));
			}
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("spsbloodmoon")){
			if (sender instanceof ConsoleCommandSender) {
				isBloodMoon = !isBloodMoon;
				if(debug){logDebug("isBloodMoon=" + isBloodMoon);}
				return true;
			}else{
				sender.sendMessage("Console only command.");
				return false;
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("spscancel")){ //cmd.getName().equalsIgnoreCase("cancel")
			if(debug){logDebug("CMD Can command cancel selected");}
			World world;
			Player player;
			List<World> worlds = Bukkit.getWorlds();
			
			if(sender.hasPermission("sps.cancel") || sender.hasPermission("sps.op")) {
				if(sender.hasPermission("sps.cancel")){if(debug){logDebug("CMD Can " + sender.getName() + " has sps.cancel");}}
				if(sender.hasPermission("sps.op")){if(debug){logDebug("CMD Can " + sender.getName() + " has sps.op");}}
				// TODO:
				if(sender instanceof Player){
					player = (Player) sender;
					world = player.getWorld();
					
					//Set default timer for when the player has never slept before
					long timer = 0;
					if(debug){logDebug("CMD Can... " + player.getName() + " is sleeping.");}
					long time = System.currentTimeMillis() / 1000;
					if(cancellimit.get(player.getUniqueId()) == null){
						if(debug){logDebug("null - player is not in cancellimit");}
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							cancellimit.put(player.getUniqueId(), time);
							if(debug){logDebug("CMD Can " + player.getDisplayName() + " added to playersCancelled");}
						}
					}else{
						if(debug){logDebug("not null - player is in cancellimit");}
						// Player is on the list.
						timer = cancellimit.get(player.getUniqueId());
						if(debug){logDebug("time=" + time);}
						if(debug){logDebug("timer=" + timer);}
						if(debug){logDebug("time - timer=" +  (time - timer));}
						if(debug){logDebug("cancellimit=" + getConfig().getLong("cancellimit", 60));}
						// if !time - timer > limit
						if(!((time - timer) > getConfig().getLong("cancellimit", 60))){
							long length = getConfig().getLong("cancellimit", 60) - (time - timer) ;
							String sleeplimit = "" + lang.get("sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							if(debug){logDebug("CMD Can... cancellimit: " + sleeplimit);}
							//player.sendMessage("You can not do that for " + length + " seconds");
							return false;
						}else if((time - timer) > getConfig().getLong("cancellimit", 60)){
							if(debug){logDebug("time - timer > cancellimit");}
							cancellimit.replace(player.getUniqueId(), time);
						}
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}
				
				/* Check if it's Day */
				if(IsDay(world)){
					if(debug){logDebug("CMD Can It is Day");}
					if (!getConfig().getBoolean("unrestricteddayskipper")) {
						if(debug){logDebug("CMD Can !unrestricted DaySkipper");}
						if (Bukkit.getScheduler().isCurrentlyRunning((dayskipTask)) || Bukkit.getScheduler().isQueued((dayskipTask))) {
							if(debug){logDebug("CMD Can DS runnable is scheduled");}
							/**
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
							} else {/ */
								if(debug){logDebug("CMD Can DS sleeplimit not reached");}
								//Set the time this player cancelled to prevent spam
								//playersCancelled.put(sender.getName().toString(), time);
								//if(debug){logDebug("CMD Can DS added to playersCancelled");}
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(dayskipTask);
								if(debug){logDebug("CMD Can DS task cancelled");}
								//Broadcast to Server
								if(debug){logDebug("cancelbroadcast=" + getConfig().getBoolean("cancelbroadcast", false));}
								if (!(getConfig().getBoolean("cancelbroadcast", false) == false)) {
									if(debug){logDebug("CMD Can DS is it here?");}
									String damsg = "[\"\",{\"text\":\"cancelmsg\"}]";
									//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									damsg = damsg.replace("cancelmsg", lang.get("dayskipcanceled").toString());
									/** nickname parser */
									String nickName = getNickname(sender);
									String playercolor = "";
									if(!nickName.contains("§")){
										//logWarn("nickName ! contain SS");
										playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									}else{
										nickName = StrUtils.parseRGBNameColors(nickName);
									}
									/** end nickname parser */
									damsg = damsg.replace("<player>", playercolor + nickName + msgcolor1);
									if(debug){logDebug("CMD Can DS damsg=" + damsg);}
									sendJson(damsg);//sendJson(ComponentSerializer.parse(damsg));
									//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
									if(debug){logDebug("CMD Can DS broadcast sent");}
								}else if (getConfig().getBoolean("cancelbroadcast", false) == false){
									if(debug){logDebug("CMD Can DS broadcast = false");}
								}
								isCanceled = true;
								return true;
							//}//
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
				if (IsNight(world)||world.hasStorm()) {
					if(debug){if(IsNight(worlds.get(0))){logDebug("CMD Can It is night");}}
					if(debug){if(worlds.get(0).hasStorm()){logDebug("CMD Can it is storming");}}
					//Bukkit.getServer().getWorld("");
					//Prevent cancelling if unrestricted sleep is enabled
					if (!getConfig().getBoolean("unrestrictedsleep")) {
						if(debug){logDebug("CMD Can !unrestricted sleep");}
						
						//Check if this is an unrestricted sleep or not
						if (Bukkit.getScheduler().isCurrentlyRunning((transitionTask)) || Bukkit.getScheduler().isQueued((transitionTask))) {
							if(debug){logDebug("CMD Can sleep runnable is scheduled");}
							
							
							/** /
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
							} else {/ */
								if(debug){logDebug("CMD Can sleeplimit not reached");}
								//Set the time this player cancelled to prevent spam
								//playersCancelled.put(sender.getName().toString(), time);
								
								//cancel the runnable task
								Bukkit.getScheduler().cancelTask(transitionTask);
								isCanceled = false;
								if(debug){logDebug("CMD Can task cancelled");}
								//Broadcast to Server
								
								if (!(getConfig().getBoolean("cancelbroadcast", false) == false)) {
									if(debug){logDebug("CMD Can is it here?");}
									String damsg = "[\"\",{\"text\":\"<player> canceled sleeping.\"}]";
									//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
									damsg = damsg.replace("<player> canceled sleeping.", lang.get("canceledsleep").toString());
									/** nickname parser */
									String nickName = getNickname(sender);
									String playercolor = "";
									if(!nickName.contains("§")){
										//logWarn("nickName ! contain SS");
										playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
									}else{
										nickName = StrUtils.parseRGBNameColors(nickName);
									}
									/** end nickname parser */
									damsg = damsg.replace("<player>", playercolor + nickName + msgcolor1);
									if(debug){logDebug("CMD Can damsg=" + damsg);}
									sendJson(damsg);//sendJson(ComponentSerializer.parse(damsg));
									//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
									if(debug){logDebug("CMD Can broadcast sent");}
								}else if (getConfig().getBoolean("cancelbroadcast", false) == false){
									if(debug){logDebug("CMD Can broadcast = false");}
								}
								isCanceled = true;
								//
								double oldHealth;
								GameMode oldGamemode;
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
											if(bedspawn.distance(player.getLocation()) < 2){
												if(debug){logDebug("CMD Can distance < 2 - inbed=true");}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location1=" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=2" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=3" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=4" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=5" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=6" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=7" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location8=" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location9=" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=10" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=11" + location);}
												inbed = true;
											}
											location.add(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=12" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=13" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=14" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=15" + location);}
												inbed = true;
											}
											location.subtract(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=16" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=17" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=18" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=19" + location);}
												inbed = true;
											}
											location.subtract(0, 0, 1);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=20" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=21" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location22=" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location=23" + location);}
												inbed = true;
											}
											location.add(1, 0, 0);
											if(location.equals(bedspawn)&&inbed != true){
												if(debug){logDebug("CMD Can location24=" + location);}
												inbed = true;
											}
											
										}
										
										if(inbed){
											oldGamemode = player.getGameMode();
											oldHealth = player.getHealth();
											if(debug){logDebug("CMD Can oldHEalth=" + oldHealth);}
											if(debug){logDebug("CMD Can GameMode=" + oldGamemode.toString());}
											if(oldGamemode != GameMode.SURVIVAL){
												player.setGameMode(GameMode.SURVIVAL);
												if(debug){logDebug("CMD Can GameMode set to SURVIVAL");}
												//log("survival");
											}
											if(!(oldHealth <= 1)){
												player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
												if(debug){logDebug("CMD Can damage=" + player.getHealth());}
												player.setHealth(oldHealth);
												//player.wakeup(true);
											}else{
												player.setHealth(oldHealth + 1);
												player.damage(1);//.getHandle().a(true,DamageSource.CACTUS);
												if(debug){logDebug("CMD Can damage=" + player.getHealth());}
												player.setHealth(oldHealth);
												//player.wakeup(true);
											}
											player.setGameMode(oldGamemode);
											if(debug){logDebug("CMD Can GameMode set to " + oldGamemode.toString());}
											//if(player.isSleeping()){
												//player.wakeup(true);
											//}
										}
									}catch (Exception e){
										logWarn("[Exception] " + player.getDisplayName() + " has never slept before.");
										// Failed to submit the stats
									}
									
								}
								if(isCanceled){
									if(debug){logDebug("CMD Can... isCanceled set to false");}
									isCanceled = false;
								}
								return true;
							//}//
							
						} else { //tell player they can't cancel sleep
							sender.sendMessage(ChatColor.YELLOW + "" + lang.get("nocancel"));
							if(debug){logDebug("CMD Can sleep runnable is NOT scheduled");}
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
				World world;
				Player player;
				// TODO:
				if(sender instanceof Player){
					player = (Player) sender;
					world = player.getWorld();
					//Set default timer for when the player has never slept before
					long timer = 0;
					if(debug){logDebug("SC " + player.getName() + " is sleeping.");}
					long time = System.currentTimeMillis() / 1000;
					if(sleeplimit.get(player.getUniqueId()) == null){
						if(debug){logDebug("SC null - player not in sleeplimit");}
						// Check if player has sps.unrestricted
						if (!player.hasPermission("sps.unrestricted")) {
							// Set player's time in HashMap
							sleeplimit.put(player.getUniqueId(), time);
							if(debug){logDebug("SC " + player.getDisplayName() + " added to playersSlept");}
						}
					}else{
						if(debug){logDebug("SC not null - player in sleeplimit");}
						// Player is on the list.
						timer = sleeplimit.get(player.getUniqueId());
						if(debug){logDebug("SC time=" + time);}
						if(debug){logDebug("SC timer=" + timer);}
						if(debug){logDebug("SC time - timer=" +  (time - timer));}
						if(debug){logDebug("SC sleeplimit=" + getConfig().getLong("sleeplimit", 60));}
						// if !time - timer > limit
						if(!((time - timer) > getConfig().getLong("sleeplimit", 60))){
							long length = getConfig().getLong("sleeplimit", 60) - (time - timer) ;
							String sleeplimit = "" + lang.get("sleeplimit").toString().replace("<length>", "" + length);
							player.sendMessage(ChatColor.YELLOW + sleeplimit);
							if(debug){logDebug("SC sleeplimit: " + sleeplimit);}
							//player.sendMessage("You can not do that for " + length + " seconds");
							
							return false;
						}else if((time - timer) > getConfig().getLong("sleeplimit", 60)){
							if(debug){logDebug("SC time - timer > sleeplimit");}
							sleeplimit.replace(player.getUniqueId(), time);
						}
					}
					if(!isBloodmoonInprogress(player.getWorld())){//isBloodmoonInprogress(player.getWorld())//isBloodMoon
						if(debug){logDebug("SC isbloodmoon=false");}
					}else{
						player.sendMessage(ChatColor.YELLOW + "SPS: " + ChatColor.RESET + lang.get("bloodmoon", "You can not sleep during a bloodmoon.").toString());
						return false;
					}
				}else{
					world = Bukkit.getWorlds().get(0);
				}

				//Broadcast to Server
				String sleepmsg;
				if (getConfig().getBoolean("randomsleepmsgs")){
					int maxmsgs = getConfig().getInt("numberofsleepmsgs");
					int randomnumber = RandomNumber(maxmsgs);
					sleepmsg = getConfig().getString("sleepmsg" + randomnumber, ChatColor.WHITE + "<player> is sleeping");
					sleepmsg = sleepmsg.replace("<colon>", ":");
				}else{
					sleepmsg = getConfig().getString(ChatColor.WHITE + "<player> is sleeping");
				}
					String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String dastring = "" + lang.get("sleepcommand");
				dastring = dastring.replace("<player>", "");
				//String damsg = "[\"\",{\"text\":\"player\"},{\"text\":\" is sleeping [\"},{\"text\":\"dacancel\",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/cancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"tooltip\"}]}}},{\"text\":\"]\",\"color\":\"none\",\"bold\":false}]";
				String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
					String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
				//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				/** nickname parser */
				String nickName = getNickname(sender);
				String playercolor = "";
				if(!nickName.contains("§")){
					//logWarn("nickName ! contain SS");
					playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
				}else{
					nickName = StrUtils.parseRGBNameColors(nickName);
				}
				/** end nickname parser */
				damsg = damsg.replace("<player>", playercolor + nickName + msgcolor1);
					String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				damsg = damsg.replace("dacancel", cancelcolor + lang.get("cancel") + msgcolor1);
				damsg = damsg.replace("tooltip", "" + lang.get("clickcancel"));
				sendJson(damsg);//sendJson(ComponentSerializer.parse(damsg));
				//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
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
			if(getConfig().getBoolean("enabledayskipper", false)){
				World world;
				if(sender instanceof Player){
					Player player = (Player) sender;
					world = player.getWorld();
				}else{
					world = Bukkit.getWorlds().get(0);
				}
				List<World> worlds = Bukkit.getWorlds();
				//World w = ((Entity) sender).getWorld();
				if(!IsDay(worlds.get(0))){
					sender.sendMessage(ChatColorUtils.setColors("" + lang.get("mustbeday")));
					return false;
				}
				if(sender.hasPermission("sps.dayskipcommand")||sender.hasPermission("sps.op")||sender.hasPermission("sps.*")){
					if(debug){logDebug(" DS Has perm or is op. ...");}
					/* OK they have the perm, now lets notify the server and schedule the runnable */
					String damsg = "[\"\",{\"text\":\"sleepmsg [\"},{\"text\":\"dacancel]\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"tooltip\"}}]";
					String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
						if(debug){logDebug(" DS ... msgcolor=" + msgcolor);}
					String sleepmsg = "" + lang.get("dayskipmsg." + daLang,"<player> wants to sleep the day away...<command>");
					damsg = damsg.replace("sleepmsg", msgcolor + sleepmsg);
					//String playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
					/** nickname parser */
					String nickName = getNickname(sender);
					String playercolor = "";
					if(!nickName.contains("§")){
						//logWarn("nickName ! contain SS");
						playercolor = ChatColorUtils.setColorsByName(getConfig().getString("playernamecolor"));
					}else{
						nickName = StrUtils.parseRGBNameColors(nickName);
					}
					/** end nickname parser */
						if(debug){logDebug(" DS ... playercolor=" + playercolor);}
					damsg = damsg.replace("<player>", playercolor + nickName + msgcolor);
					String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
						if(debug){logDebug(" DS ... cancelcolor=" + cancelcolor);}
					damsg = damsg.replace("dacancel", cancelcolor + lang.get("dayskipcancel") + msgcolor);
					//change cancel color based on config
					damsg = damsg.replace("tooltip", "" + lang.get("dayskipclickcancel"));
						if(debug){logDebug(" DS string processed. ...");}
						sendJson(damsg);//sendJson(ComponentSerializer.parse(damsg));
						//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
						if(debug){logDebug(" DS SendAllJsonMessage. ...");}
						if(!isDSCanceled){
							//final World world = worlds.get(0);
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
		}
		
		if (cmd.getName().equalsIgnoreCase("clearrain")){
			if(config.getBoolean("clearrain_enabled", false)){
				if(sender instanceof Player){
					Player player = (Player) sender;
					World world = player.getWorld();
					if (!IsNight(player.getWorld())&&player.getWorld().hasStorm()) {
						world.setStorm(false);
						player.sendMessage("Rain stopped.");
					}else{
						sender.sendMessage("Must not be Night, and a rainstorm must be present");
					}
				}else{
					sender.sendMessage("Must be a player to use this command.");
				}
			}else{
				sender.sendMessage("clearrain is not enabled.");
			}
		}

		return true;
	}
	
	public  void log(String dalog){
		SinglePlayerSleep.logger.info("" + this.getName() + " " + this.getDescription().getVersion() + " " + dalog);
	}
	public  void logDebug(String dalog){
		log(" [DEBUG] " + dalog);
	}
	public void logWarn(String dalog){
		log(" [WARNING] " + dalog);
	}
	public void broadcast(String message, World world){
		String damsg = "{\"text\":\"broadcastString\"}";
		String msgcolor1 = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
		damsg = damsg.replace("broadcastString", message);
		sendJson(damsg);
		//SendJsonMessages.SendAllJsonMessage(damsg, "", world);
		
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
	public boolean fileContains(String filePath, String searchQuery) throws IOException{
		searchQuery = searchQuery.trim();
		BufferedReader br = null;

		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String line;
			while ((line = br.readLine()) != null){
				if (line.contains(searchQuery)){
					//log("findstring found");
					return true;
				}else{
				}
			}
		}
		finally{
			try{
				if (br != null)
					br.close();
			}
			catch (Exception e){
				System.err.println("Exception while closing bufferedreader " + e.toString());
			}
		}
		//log("findstring failed");
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
	
	public static void copyFile_Java7(String origin, String destination) throws IOException {
		Path FROM = Paths.get(origin);
		Path TO = Paths.get(destination);
		//overwrite the destination file if it exists, and copy
		// the file attributes, including the rwx permissions
		CopyOption[] options = new CopyOption[]{
			StandardCopyOption.REPLACE_EXISTING,
			StandardCopyOption.COPY_ATTRIBUTES
		}; 
		Files.copy(FROM, TO, options);
	}
	
	public boolean isBloodmoonInprogress(World world){
		if(getServer().getPluginManager().getPlugin("BloodMoon") != null){
			BloodmoonActuator getactuator = BloodmoonActuator.GetActuator(world);
			if(getactuator != null){
				return getactuator.isInProgress();
			}else{return false;}
		}
		return false;
	}
	
	public  String getNickname(Player player){
		if(getServer().getPluginManager().getPlugin("VentureChat") != null){
			MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(player);
			String nick = mcp.getNickname();
			if(nick != null){
				if(debug){logDebug("mcp.getNickname()=" + mcp.getNickname());}
				if(debug){logDebug("ChatColor.translateAlternateColorCodes('&', nick)=" + ChatColor.translateAlternateColorCodes('&', nick));}
				//ChatColor.translateAlternateColorCodes('&', nick);
				//nick = nick.replaceAll("§", "&");
				nick = Format.color(nick);
				if(debug){logDebug("Format.FormatStringAll(nick)=" + nick);}
				return nick;
			}
			return player.getDisplayName();
		}else if(getServer().getPluginManager().getPlugin("Essentials") != null){
			Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
			//User user = ess.getUserMap().getUser(player.getName());
			//if(debug){logDebug("Essnetials Nick=" + ess.getUserMap().getUser(player.getName()).getNickname());}
			String nick = ess.getUserMap().getUser(player.getName()).getNickname();
			if(nick != null){
				if(debug){logDebug("Essnetials Nick=" + nick);}
				return ChatColor.translateAlternateColorCodes('&', nick);
			}
			if(debug){logDebug("Essnetials Nick=null using DisplayName");}
			return player.getDisplayName();
		}else{
			if(debug){logDebug("player.getDisplayName()=" + player.getDisplayName());}
			return player.getDisplayName();
		}
	}
	
	public String getNickname(CommandSender sender){
		if(sender instanceof Player){
			return getNickname((Player)sender);
		}else{
			return "Console";
		}
	}
	
	public void sendJson(String string, String... OldString){
		String string2;
		String[] string3 = OldString;
		for (Player player: Bukkit.getOnlinePlayers()){
			if(player.hasPermission("sps.cancel")&&displaycancel){
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + 
				        " " + string);
				//player.spigot().sendMessage(string);
				if(debug){logDebug("SAJM - string=" + string);}
				if(debug){logDebug("SAJM - perm & display - Broadcast");}
			}else{
				if(debug){logDebug("SAJM - string3.toString()=" + string3.toString());}
				string2 = string.toString();
				if(debug){logDebug("SAJM - string2=" + string2);}
				string2 = string2.replace(" [\"", " \"").replace("]\"", "\"").replace(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spscancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to cancel sleep\"}", "");
				if(debug){logDebug("SAJM - string2=" + string2);}
				string2 = string2.replace(string3.toString(), "");
				String msgcolor = ChatColorUtils.setColorsByName(getConfig().getString("sleepmsgcolor", "YELLOW"));
				String cancelcolor = ChatColorUtils.setColorsByName(getConfig().getString("cancelcolor"));
				string2 = string2.replace(cancelcolor + lang.get("cancel") + msgcolor, "");
				if(debug){logDebug("SAJM - string2=" + string2);}
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + 
				        " " + string2);
				if(debug){logDebug("SAJM - !perm & display - Broadcast");}
				//player.sendRawMessage(string2);
				//player.spigot().sendMessage(ComponentSerializer.parse(string2));
			}
		}
	}
}
