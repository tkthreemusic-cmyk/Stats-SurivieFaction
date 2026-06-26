package com.playerstats;

import com.playerstats.commands.MainCommand;
import com.playerstats.commands.AdminCommand;
import com.playerstats.listeners.PlayerListener;
import com.playerstats.listeners.EntityListener;
import com.playerstats.manager.StatsManager;
import com.playerstats.manager.ScoreboardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStats extends JavaPlugin {

    private static PlayerStats instance;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        statsManager = new StatsManager(this);
        scoreboardManager = new ScoreboardManager(this);
        
        getCommand("playerstats").setExecutor(new MainCommand(this));
        getCommand("playerstats-admin").setExecutor(new AdminCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            scoreboardManager.updateAllScoreboards();
        }, 20L * 60, 20L * 60);
        
        getLogger().info("PlayerStats has been enabled!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.saveAllStats();
        }
        getLogger().info("PlayerStats has been disabled!");
    }

    public static PlayerStats getInstance() {
        return instance;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}