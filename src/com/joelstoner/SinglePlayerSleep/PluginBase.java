package com.joelstoner.SinglePlayerSleep;


import java.io.File;
import java.io.IOException;
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

	public final Logger logger = Logger.getLogger("Minecraft");
	public boolean isCanceled = false;
	public int transitionTask = 0;
	
	@Override // TODO:
	public void onEnable(){
		CheckForConfig();
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info("**************************************************************");
		this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " Has been enabled");
		this.logger.info("**************************************************************");
		getServer().getPluginManager().registerEvents(this, this);
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
						// TODO Auto-generated method stub
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
				this.log("clearing downfall..."); //Logger
			}
		}
		if(world.isThundering()){
			if(player.hasPermission("sps.thunder")){
				world.setThundering(false);
				this.log("clearing thunderstorm..."); //Logger
			}
		}
		long Relative_Time = 24000 - world.getTime();
		world.setFullTime(world.getFullTime() + Relative_Time);
		this.log("Setting time to day..."); //Logger
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
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
}
