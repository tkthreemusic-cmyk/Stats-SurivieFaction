package com.playerstats.manager;

import com.playerstats.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private final PlayerStats plugin;
    private final Map<UUID, PlayerStatsData> playerStats;
    private final File dataFile;

    public StatsManager(PlayerStats plugin) {
        this.plugin = plugin;
        this.playerStats = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadStats();
    }

    private void loadStats() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerStatsData data = new PlayerStatsData();
                data.mobsKilled = config.getInt(uuidStr + ".mobsKilled", 0);
                data.playersKilled = config.getInt(uuidStr + ".playersKilled", 0);
                data.deaths = config.getInt(uuidStr + ".deaths", 0);
                data.blocksBroken = config.getInt(uuidStr + ".blocksBroken", 0);
                data.blocksPlaced = config.getInt(uuidStr + ".blocksPlaced", 0);
                data.itemsCrafted = config.getInt(uuidStr + ".itemsCrafted", 0);
                data.distanceWalked = config.getDouble(uuidStr + ".distanceWalked", 0);
                data.timePlayed = config.getLong(uuidStr + ".timePlayed", 0);
                data.damageDealt = config.getDouble(uuidStr + ".damageDealt", 0);
                data.damageTaken = config.getDouble(uuidStr + ".damageTaken", 0);
                data.foodEaten = config.getInt(uuidStr + ".foodEaten", 0);
                data.animalsBred = config.getInt(uuidStr + ".animalsBred", 0);
                playerStats.put(uuid, data);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidStr);
            }
        }
    }

    public void saveAllStats() {
        FileConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerStatsData> entry : playerStats.entrySet()) {
            String path = entry.getKey().toString();
            PlayerStatsData data = entry.getValue();
            config.set(path + ".mobsKilled", data.mobsKilled);
            config.set(path + ".playersKilled", data.playersKilled);
            config.set(path + ".deaths", data.deaths);
            config.set(path + ".blocksBroken", data.blocksBroken);
            config.set(path + ".blocksPlaced", data.blocksPlaced);
            config.set(path + ".itemsCrafted", data.itemsCrafted);
            config.set(path + ".distanceWalked", data.distanceWalked);
            config.set(path + ".timePlayed", data.timePlayed);
            config.set(path + ".damageDealt", data.damageDealt);
            config.set(path + ".damageTaken", data.damageTaken);
            config.set(path + ".foodEaten", data.foodEaten);
            config.set(path + ".animalsBred", data.animalsBred);
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    public PlayerStatsData getPlayerStats(UUID uuid) {
        return playerStats.computeIfAbsent(uuid, k -> new PlayerStatsData());
    }

    public void incrementMobsKilled(UUID uuid) {
        getPlayerStats(uuid).mobsKilled++;
    }

    public void incrementPlayersKilled(UUID uuid) {
        getPlayerStats(uuid).playersKilled++;
    }

    public void incrementDeaths(UUID uuid) {
        getPlayerStats(uuid).deaths++;
    }

    public void incrementBlocksBroken(UUID uuid) {
        getPlayerStats(uuid).blocksBroken++;
    }

    public void incrementBlocksPlaced(UUID uuid) {
        getPlayerStats(uuid).blocksPlaced++;
    }

    public void incrementItemsCrafted(UUID uuid) {
        getPlayerStats(uuid).itemsCrafted++;
    }

    public void addDistanceWalked(UUID uuid, double distance) {
        getPlayerStats(uuid).distanceWalked += distance;
    }

    public void addTimePlayed(UUID uuid, long ticks) {
        getPlayerStats(uuid).timePlayed += ticks;
    }

    public void addDamageDealt(UUID uuid, double damage) {
        getPlayerStats(uuid).damageDealt += damage;
    }

    public void addDamageTaken(UUID uuid, double damage) {
        getPlayerStats(uuid).damageTaken += damage;
    }

    public void incrementFoodEaten(UUID uuid) {
        getPlayerStats(uuid).foodEaten++;
    }

    public void incrementAnimalsBred(UUID uuid) {
        getPlayerStats(uuid).animalsBred++;
    }

    public List<UUID> getTopMobsKilled(int limit) {
        return playerStats.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().mobsKilled, e1.getValue().mobsKilled))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<UUID> getTopPlayersKilled(int limit) {
        return playerStats.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().playersKilled, e1.getValue().playersKilled))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<UUID> getTopDeaths(int limit) {
        return playerStats.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().deaths, e1.getValue().deaths))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<UUID> getTopProgress(int limit) {
        return playerStats.entrySet().stream()
                .sorted((e1, e2) -> {
                    PlayerStatsData d1 = e1.getValue();
                    PlayerStatsData d2 = e2.getValue();
                    double score1 = d1.blocksBroken + d1.blocksPlaced + d1.itemsCrafted + 
                                    (d1.distanceWalked / 100) + d1.animalsBred;
                    double score2 = d2.blocksBroken + d2.blocksPlaced + d2.itemsCrafted + 
                                    (d2.distanceWalked / 100) + d2.animalsBred;
                    return Double.compare(score2, score1);
                })
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public String getPlayerName(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        return plugin.getServer().getOfflinePlayer(uuid).getName() != null ? 
               plugin.getServer().getOfflinePlayer(uuid).getName() : "Unknown";
    }

    public void resetPlayerStats(UUID uuid) {
        playerStats.put(uuid, new PlayerStatsData());
        saveAllStats();
    }

    public void setPlayerStat(UUID uuid, String stat, int value) {
        PlayerStatsData data = getPlayerStats(uuid);
        switch (stat.toLowerCase()) {
            case "mobskilled", "mobs" -> data.mobsKilled = value;
            case "playerskilled", "players", "pvp" -> data.playersKilled = value;
            case "deaths" -> data.deaths = value;
            case "blocksbroken" -> data.blocksBroken = value;
            case "blocksplaced" -> data.blocksPlaced = value;
            case "itemscrafted", "items", "crafted" -> data.itemsCrafted = value;
            default -> plugin.getLogger().warning("Unknown stat: " + stat);
        }
    }

    public static class PlayerStatsData {
        public int mobsKilled = 0;
        public int playersKilled = 0;
        public int deaths = 0;
        public int blocksBroken = 0;
        public int blocksPlaced = 0;
        public int itemsCrafted = 0;
        public double distanceWalked = 0;
        public long timePlayed = 0;
        public double damageDealt = 0;
        public double damageTaken = 0;
        public int foodEaten = 0;
        public int animalsBred = 0;
    }
}