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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public void onEnable() {
		langFile = new File(getDataFolder(), "lang.yml");
		if(!langFile.exists()){                                  // checks if the yaml does not exists
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
		consoleInfo("enabled");
	}
	
	public void onDisable() {
		consoleInfo("disabled");		
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginBase.logger.info("**************************************************************");
		PluginBase.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " Has been " + state);
		PluginBase.logger.info("**************************************************************");
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
	public void PlayerIsSleeping(PlayerBedEnterEvent event) throws InterruptedException {
		final Player player = event.getPlayer();
		final World world = player.getWorld();

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
				transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						setDatime(player, world);
					}						
				}, 10 * 20);
			} else {
				//Broadcast to Server
				TextComponent message2 = new TextComponent(nameColor() + player.getDisplayName() + ChatColor.RESET + " " + lang.get("issleep." + daLang + ""));
				TextComponent message = new TextComponent(ChatColorUtils.setColors(getConfig().getString("cancelcolor")) + " [" + lang.get("cancel." + daLang + "") + "]");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cancel"));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("" + lang.get("clickcancel." + daLang + "")).create()));
				
				//Don't show cancel option if player has unrestricted sleep perm
				if (player.hasPermission("sps.unrestricted")) {
					
					//Broadcast "player is sleeping"
					this.broadcast(message2);
					
					transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							setDatime(player, world);
						}						
					}, 10 * 20);
					
				} else {
					if (player.hasPermission("sps.hermits") || player.hasPermission("sps.op")) {
						
					    //Add "[cancel]" link
						message2.addExtra(message);
						
						//Broadcast "player is sleeping [cancel]"
						this.broadcast(message2);
						
						if (!isCanceled) {					
							transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								public void run() {
									setDatime(player, world);
								}						
							}, 10 * 20);
						} else {					
							isCanceled = false;
						}
					} else if (!player.hasPermission("sps.hermits") || player.hasPermission("sps.op")) {
						player.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang + ""));
					}
				}
			}
		}
	}

	public void setDatime(Player player, World world) {
		if (world.hasStorm()) {
			if (player.hasPermission("sps.downfall")||player.hasPermission("sps.op")) {
				world.setStorm(false);
				if (debug) {
					log("" + lang.get("setdownfall." + daLang + "") + "...");
				}
			}
		}
		if (world.isThundering()) {
			if(player.hasPermission("sps.thunder")||player.hasPermission("sps.op")){
				world.setThundering(false);
				if (debug) {
					log("" + lang.get("setthunder." + daLang + "") + "...");
				}
			}
		}
		long Relative_Time = 24000 - world.getTime();
		world.setFullTime(world.getFullTime() + Relative_Time);
		if (debug) {
			log("" + lang.get("settime." + daLang + "") + "...");
		}
	}
	public void setDatimeS(CommandSender sender, World world) {
		if (world.hasStorm()) {
			if (sender.hasPermission("sps.downfall") || sender.hasPermission("sps.op")) {
				world.setStorm(false);
				if (debug) {
					log("" + lang.get("setdownfall." + daLang + "") + "...");
				}
			}
		}
		if (world.isThundering()) {
			if( sender.hasPermission("sps.thunder")|| sender.hasPermission("sps.op")){
				world.setThundering(false);
				if (debug) {
					log("" + lang.get("setthunder." + daLang + "") + "...");
				}
			}
		}
		long Relative_Time = 24000 - world.getTime();
		world.setFullTime(world.getFullTime() + Relative_Time);
		if (debug) {
			log("" + lang.get("settime." + daLang + "") + "...");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    if (cmd.getName().equalsIgnoreCase("SPS")) {
	      if (args.length == 0) {
	    	sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	    	sender.sendMessage(ChatColor.GREEN + " " + lang.get("touse." + daLang + ""));
	    	sender.sendMessage(ChatColor.WHITE + " ");
	    	sender.sendMessage(ChatColor.WHITE + " /Sleep - " + lang.get("sleephelp." + daLang + ""));
	    	sender.sendMessage(ChatColor.WHITE + " /Cancel - " + lang.get("cancelhelp." + daLang + ""));
	    	sender.sendMessage(ChatColor.WHITE + " ");
	        if (sender.isOp()||sender.hasPermission("sps.op")) {
	        	sender.sendMessage(ChatColor.GOLD + " OP Commands");
	        	sender.sendMessage(ChatColor.GOLD + " /SPS update - " + lang.get("spsupdate." + daLang + ""));
	        	sender.sendMessage(ChatColor.GOLD + " /SPS reload - " + lang.get("spsreload." + daLang + ""));
	        	sender.sendMessage(ChatColor.GOLD + " /SPS check true/false - " + lang.get("spscheck." + daLang + ""));
	        }
	        sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "SinglePlayerSleep" + ChatColor.GREEN + "]===============[]");
	        return true;
	      }
	      if (args[0].equalsIgnoreCase("check")) {
	    	  if (args.length< 1) {
					return false;
			  }
	    	  if (sender.isOp()||sender.hasPermission("sps.op")) {
	    		  if (!args[1].equalsIgnoreCase("true") & !args[1].equalsIgnoreCase("false")) {
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " §c" + lang.get("boolean." + daLang + "") + ": /sps check True/False");
	    		  } else if (args[1].contains("true") || args[1].contains("false")) {
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("checkset." + daLang + "") + " " + args[1]);
						if (args[1].contains("false")) {
							sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("nocheck." + daLang + ""));
						} else if (args[1].contains("true")) {
							sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("yescheck." + daLang + ""));
						}
						reloadConfig();
						return true;
					}
	    	  }
	    	  
	      }
		  if (args[0].equalsIgnoreCase("reload")) {
			  if (sender.isOp() || sender.hasPermission("sps.op") || !(sender instanceof Player)) {
				  this.reloadConfig();
				  PluginBase plugin = this;
				  getServer().getPluginManager().disablePlugin(plugin);
                  getServer().getPluginManager().enablePlugin(plugin);
			  } else {
				  sender.sendMessage(ChatColor.RED + " " + lang.get("noperm." + daLang));
			  }
	      }
		  if (args[0].equalsIgnoreCase("update")) {

			//Player must be OP and auto-update-check must be true
			if (sender.isOp() || sender.hasPermission("sps.update") || sender.hasPermission("sps.op")) {	

				//Check for updates
				updateCheck(sender, null);

			} else {
				if (!sender.isOp() || !sender.hasPermission("sps.op")) {
					sender.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang));
				} else {
					sender.sendMessage(ChatColor.RED + "" + lang.get("notop." + daLang));
				}
			}
		  }
	    }

		if (cmd.getName().equalsIgnoreCase("cancel")) {
			List<World> worlds = Bukkit.getWorlds();
			if(sender.isOp() || sender.hasPermission("sps.cancel") || sender.hasPermission("sps.op")) {
				
				//Check it's night
				if (IsNight(worlds.get(0))) {
	
					//Prevent cancelling if unrestricted sleep is enabled
					if (!getConfig().getBoolean("unrestrictedsleep")) {
	
						//Check if this is an unrestricted sleep or not
						if (Bukkit.getScheduler().isCurrentlyRunning((transitionTask)) || Bukkit.getScheduler().isQueued((transitionTask))) {
							long time = System.currentTimeMillis() / 1000;
	
							//Set default timer
							long timer = 0;
	
							long pTimeCancel = 0;
							if (playersCancelled.get(sender.getName()) != null) {
								pTimeCancel = playersCancelled.get(sender.getName());
							}
	
							//check if player has already tried cancelling to prevent spam
							if (getConfig().getInt("sleeplimit") > 0) {
								timer = time - pTimeCancel;
							}
							
							//Tell the player why they can't sleep
							if (timer < getConfig().getInt("sleeplimit")) {	    
								String sleeplimit = "" + lang.get("sleeplimit." + daLang + "").toString();
								sender.sendMessage(ChatColor.YELLOW + sleeplimit);			
							} else {
								//Set the time this player cancelled to prevent spam
								playersCancelled.put(sender.getName().toString(), time);
	
								Bukkit.getScheduler().cancelTask(transitionTask);
	
								//Broadcast to Server
								if (cancelbroadcast) {
									this.broadcast(nameColor() + sender.getName() + ChatColor.RESET + " " + lang.get("canceledsleep." + daLang + ""));
								}
								isCanceled = true;
							}
						} else {
							sender.sendMessage(ChatColor.YELLOW + "" + lang.get("nocancel." + daLang + ""));
						}
					} else {
						sender.sendMessage(ChatColor.YELLOW + "" + lang.get("cancelunrestricted." + daLang + ""));
					}
				} else {
					sender.sendMessage(ChatColor.YELLOW + "" + lang.get("mustbenight." + daLang + ""));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang + ""));
			}
			if (isCanceled) {
				isCanceled = false;
			}
		}
		if (cmd.getName().equalsIgnoreCase("sleep")) {
			List<World> worlds = Bukkit.getWorlds();
			if (!IsNight(worlds.get(0))) {
				sender.sendMessage(ChatColor.YELLOW + "" + lang.get("mustbenight." + daLang + ""));
				return false;
			}

			final CommandSender daSender = sender;
			final World world = worlds.get(0);

			log(sender.getName() + " is sleeping.");
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
				sender.sendMessage(ChatColor.YELLOW + sleeplimit);
				return false;
			} else {

				//Save the time the player last tried to sleep, skip if player has unrestricted sleep since it will always be successful
				if (!sender.hasPermission("sps.unrestricted")) {
					pTime = (int) time;
				}
				
				//Check if players can sleep without the ability for others to cancel it
				if (getConfig().getBoolean("unrestrictedsleep")) {
					transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							setDatimeS(sender, world);
						}						
					}, 10 * 20);
					return true;
				} else {
					//Broadcast to Server
					TextComponent message2 = new TextComponent(nameColor() + sender.getName() + ChatColor.RESET + " " + lang.get("issleep." + daLang + ""));
					TextComponent message = new TextComponent(ChatColorUtils.setColors(getConfig().getString("cancelcolor")) + " [" + lang.get("cancel." + daLang + "") + "]");
					message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cancel"));
					message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("" + lang.get("clickcancel." + daLang + "")).create()));
					
					//Don't show cancel option if player has unrestricted sleep perm
					if (sender.hasPermission("sps.unrestricted")) {
						
						//Broadcast "player is sleeping"
						this.broadcast(message2);
						
						transitionTaskUnrestricted = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
							public void run() {
								setDatimeS(sender, world);
							}						
						}, 10 * 20);
						return true;
					} else {
						if (sender.hasPermission("sps.hermits") || sender.hasPermission("sps.op")) {
							
						    //Add "[cancel]" link
							message2.addExtra(message);
							
							//Broadcast "player is sleeping [cancel]"
							this.broadcast(message2);
							
							if (!isCanceled) {				
								transitionTask = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									public void run() {
										setDatimeS(sender, world);
									}						
								}, 10 * 20);
								return true;
							} else {					
								isCanceled = false;
								return false;
							}
						} else if (!sender.hasPermission("sps.hermits") || sender.hasPermission("sps.op")) {
							sender.sendMessage(ChatColor.RED + "" + lang.get("noperm." + daLang + ""));
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	
	public void log(String dalog) {
		PluginBase.logger.info(this.getName() + " " + dalog);
	}
	
	public void broadcast(String message) {
		getServer().broadcastMessage("" + message);
	}
	public void broadcast(TextComponent message) {
		Bukkit.getServer().spigot().broadcast(message);
	}
	  
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
	    Player p = event.getPlayer();
	    if(p.isOp() || p.hasPermission("sps.update") || p.hasPermission("sps.op") && UpdateCheck){
	    	
	    	//Check for updates
	    	updateCheck(null, p);
		}
	}

	public static boolean IsNight(World w) {
    	long time = (w.getFullTime()) % 24000;
    	return time >= mobSpawningStartTime && time < mobSpawningStopTime;
    }
	
	public void updateCheck(CommandSender sender, Player p) {
		try {				
			URL url = new URL("https://raw.githubusercontent.com/coldcode69/SinglePlayerSleep/master/version.txt");
			final URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final String localVersion = this.getDescription().getVersion();
            if(debug){this.log("response= ." + response + ".");}
            if(debug){this.log("localVersion= ." + localVersion + ".");}
            if (sender != null) {
	            if (!response.equalsIgnoreCase(localVersion)) {
	            	sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers." + daLang + ""));
				}else{
					sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.GREEN + " " + lang.get("curvers." + daLang + ""));
				}
            } else if (p != null) {
	            if (!response.equalsIgnoreCase(localVersion)) {
					p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newvers." + daLang + ""));
				}
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
