package com.playerstats;

import com.playerstats.commands.MainCommand;
import com.playerstats.listeners.PlayerListener;
import com.playerstats.listeners.EntityListener;
import com.playerstats.manager.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStats extends JavaPlugin {

    private static PlayerStats instance;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        statsManager = new StatsManager(this);
        
        getCommand("stats").setExecutor(new MainCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        
        getLogger().info("PlayerStats v1.6.0 a été activé!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.saveAllStats();
        }
        getLogger().info("PlayerStats a été désactivé!");
    }

    public static PlayerStats getInstance() {
        return instance;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}