package com.joelstoner.SinglePlayerSleep.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.joelstoner.SinglePlayerSleep.PluginBase;

public class ConfigAPI  {

	public static  void CheckForConfig(Plugin plugin){
		try{
			PluginDescriptionFile pdfFile = plugin.getDescription();
			if(!plugin.getDataFolder().exists()){
				plugin.getLogger().info(pdfFile.getName() + ": Data Folder doesn't exist");
				plugin.getLogger().info(pdfFile.getName() + ": Creating Data Folder");
				plugin.getDataFolder().mkdirs();
				plugin.getLogger().info(pdfFile.getName() + ": Data Folder Created at " + plugin.getDataFolder());
			}
			File  file = new File(plugin.getDataFolder(), "config.yml");
			plugin.getLogger().info("" + file);
			if(!file.exists()){
				plugin.getLogger().info(pdfFile.getName() + ": config.yml not found, creating!");
				plugin.saveDefaultConfig();
				FileConfiguration config = plugin.getConfig();
				
				config.options().copyDefaults(true);
				plugin.saveConfig();
			}
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	public static void Reloadconfig(Plugin plugin){
		// Load config.
		FileConfiguration config = plugin.getConfig();
		String daString = config.getString("debug").replace("'", "") + ",";
		
		if(daString.contains("true")){
			PluginBase.debug = true;
			log("debug=true", plugin);
		}else{
			PluginBase.debug = false;
			log("debug=false", plugin);
		}
		String daString2 = config.getString("auto-update-check").replace("'", "") + ",";
		if(daString2.contains("true")){
			PluginBase.UpdateCheck = true;
		}else{
			PluginBase.UpdateCheck = false;
		}
		String daString3 = config.getString("cancelbroadcast").replace("'", "") + ",";
		if(daString3.contains("true")){
			PluginBase.cancelbroadcast = true;
		}else{
			PluginBase.cancelbroadcast = false;
		}
		String daString4 = config.getString("lang", "en_US").replace("'", "");
		PluginBase.daLang = daString4;
		if(PluginBase.debug){log("UpdateCheck = " + PluginBase.UpdateCheck, plugin);} //TODO: Logger
	}
	public static  void log(String dalog, Plugin plugin){
		PluginBase.logger.info(plugin.getName() + " " + dalog);
	}
	
	/*
     * this copy(); method copies the specified file from your jar
     *     to your /plugins/<pluginName>/ folder
     */
    public static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
