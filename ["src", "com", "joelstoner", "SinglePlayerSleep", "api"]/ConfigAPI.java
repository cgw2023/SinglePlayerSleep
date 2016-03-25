package com.joelstoner.SinglePlayerSleep.api;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigAPI  extends JavaPlugin{

	public void CheckForConfig(){
		try{
			PluginDescriptionFile pdfFile = this.getDescription();
			if(!getDataFolder().exists()){
				getLogger().info(pdfFile.getName() + ": Data Folder doesn't exist");
				getLogger().info(pdfFile.getName() + ": Creating Data Folder");
				getDataFolder().mkdirs();
				getLogger().info(pdfFile.getName() + ": Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			getLogger().info("" + file);
			if(!file.exists()){
				getLogger().info(pdfFile.getName() + ": config.yml not found, creating!");
				saveDefaultConfig();
				FileConfiguration config = getConfig();
				
				config.options().copyDefaults(true);
				saveConfig();
			}
			}catch(Exception e){
				e.printStackTrace();
			}
	}
}
