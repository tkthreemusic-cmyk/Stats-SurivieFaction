package com.playerstats.manager;

import com.playerstats.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private final PlayerStats plugin;
    private final Map<UUID, PlayerStatsData> playerStats;
    private final Map<UUID, String> playerNames;
    private final Map<UUID, Boolean> importedFromMinecraft;
    private final File dataFile;

    public StatsManager(PlayerStats plugin) {
        this.plugin = plugin;
        this.playerStats = new HashMap<>();
        this.playerNames = new HashMap<>();
        this.importedFromMinecraft = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadStats();
    }
    
    public void importMinecraftStats(UUID uuid) {
        if (importedFromMinecraft.getOrDefault(uuid, false)) {
            return;
        }
        
        File statsFile = findMinecraftStatsFile(uuid);
        if (statsFile == null || !statsFile.exists()) {
            return;
        }
        
        try {
            String content = Files.readString(statsFile.toPath());
            PlayerStatsData minecraftData = parseMinecraftStats(content);
            
            PlayerStatsData currentData = getPlayerStats(uuid);
            
            // Only import if our stats are empty (first time player)
            boolean isEmpty = currentData.mobsKilled == 0 && 
                              currentData.playersKilled == 0 && 
                              currentData.deaths == 0 &&
                              currentData.blocksBroken == 0 &&
                              currentData.blocksPlaced == 0 &&
                              currentData.itemsCrafted == 0;
            
            if (isEmpty) {
                currentData.mobsKilled = minecraftData.mobsKilled;
                currentData.playersKilled = minecraftData.playersKilled;
                currentData.deaths = minecraftData.deaths;
                currentData.blocksBroken = minecraftData.blocksBroken;
                currentData.blocksPlaced = minecraftData.blocksPlaced;
                currentData.itemsCrafted = minecraftData.itemsCrafted;
                
                importedFromMinecraft.put(uuid, true);
                plugin.getLogger().info("Stats importées de Minecraft pour " + getPlayerName(uuid));
            } else {
                importedFromMinecraft.put(uuid, true);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de lire les stats Minecraft pour " + uuid);
        }
    }
    
    private File findMinecraftStatsFile(UUID uuid) {
        String uuidDashless = uuid.toString().replace("-", "");
        
        // Check all world directories (main world, nether, end)
        String[] worldNames = {"world", "world_nether", "world_the_end"};
        
        for (String worldName : worldNames) {
            File statsDir = new File(worldName + "/stats");
            if (statsDir.exists()) {
                File statsFile = new File(statsDir, uuidDashless + ".json");
                if (statsFile.exists()) {
                    return statsFile;
                }
            }
        }
        
        // Also check server root for newer Minecraft versions
        File serverStatsDir = new File("stats");
        if (serverStatsDir.exists()) {
            File statsFile = new File(serverStatsDir, uuidDashless + ".json");
            if (statsFile.exists()) {
                return statsFile;
            }
        }
        
        return null;
    }
    
    private PlayerStatsData parseMinecraftStats(String json) {
        PlayerStatsData data = new PlayerStatsData();
        
        try {
            com.google.gson.JsonObject stats = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            
            // Mobs kills via "minecraft:killed"
            if (stats.has("minecraft:killed")) {
                com.google.gson.JsonObject killed = stats.getAsJsonObject("minecraft:killed");
                for (String key : killed.keySet()) {
                    // All mobs (not player)
                    if (!key.equals("minecraft:player")) {
                        data.mobsKilled += killed.get(key).getAsInt();
                    } else {
                        // Player kills = you killed other players
                        data.playersKilled += killed.get(key).getAsInt();
                    }
                }
            }
            
            // Deaths via "minecraft:killed_by"
            if (stats.has("minecraft:killed_by")) {
                com.google.gson.JsonObject killedBy = stats.getAsJsonObject("minecraft:killed_by");
                for (String key : killedBy.keySet()) {
                    if (key.equals("minecraft:player")) {
                        data.deaths += killedBy.get(key).getAsInt();
                    }
                }
            }
            
            // Blocks mined
            if (stats.has("minecraft:mined")) {
                com.google.gson.JsonObject mined = stats.getAsJsonObject("minecraft:mined");
                for (String key : mined.keySet()) {
                    data.blocksBroken += mined.get(key).getAsInt();
                }
            }
            
            // Blocks placed
            if (stats.has("minecraft:used")) {
                com.google.gson.JsonObject used = stats.getAsJsonObject("minecraft:used");
                for (String key : used.keySet()) {
                    data.blocksPlaced += used.get(key).getAsInt();
                }
            }
            
            // Items crafted
            if (stats.has("minecraft:crafted")) {
                com.google.gson.JsonObject crafted = stats.getAsJsonObject("minecraft:crafted");
                for (String key : crafted.keySet()) {
                    data.itemsCrafted += crafted.get(key).getAsInt();
                }
            }
            
            // Custom stats (Minecraft 1.17+)
            if (stats.has("minecraft:custom")) {
                com.google.gson.JsonObject custom = stats.getAsJsonObject("minecraft:custom");
                
                // Fish caught
                data.fishCaught = getIntStat(custom, "minecraft:fish_caught", 0);
                
                // Raids won
                data.raidsWon = getIntStat(custom, "minecraft:raid_win", 0);
                if (data.raidsWon == 0) {
                    data.raidsWon = getIntStat(custom, "minecraft:raid_victories", 0);
                }
                
                // Ender chests opened
                data.enderChestsOpened = getIntStat(custom, "minecraft:open_enderchest", 0);
                
                // Trades used
                data.tradesUsed = getIntStat(custom, "minecraft:traded_with_villager", 0);
                if (data.tradesUsed == 0) {
                    data.tradesUsed = getIntStat(custom, "minecraft:villager_trades", 0);
                }
                
                // Distance walked
                data.distanceWalked = getDoubleStat(custom, "minecraft:walk_one_cm", 0) + 
                                      getDoubleStat(custom, "minecraft:sprint_one_cm", 0) +
                                      getDoubleStat(custom, "minecraft:crouch_one_cm", 0);
                
                // Time played
                data.timePlayed = getLongStat(custom, "minecraft:play_one_minute", 0);
                
                // Food eaten
                data.foodEaten = getIntStat(custom, "minecraft:eat_better_slice", 0);
                if (data.foodEaten == 0) {
                    data.foodEaten = getIntStat(custom, "minecraft:consume_item", 0);
                }
                
                // Damage dealt
                data.damageDealt = getDoubleStat(custom, "minecraft:damage_dealt", 0);
                data.damageTaken = getDoubleStat(custom, "minecraft:damage_taken", 0);
            }
            
            // Animals bred
            if (stats.has("minecraft:bred")) {
                com.google.gson.JsonObject bred = stats.getAsJsonObject("minecraft:bred");
                for (String key : bred.keySet()) {
                    data.animalsBred += bred.get(key).getAsInt();
                }
            }
            
            // Advancements count
            if (stats.has("minecraft:custom")) {
                com.google.gson.JsonObject custom = stats.getAsJsonObject("minecraft:custom");
                int advancementsCount = 0;
                for (String key : custom.keySet()) {
                    if (key.startsWith("minecraft:adventure")) {
                        advancementsCount++;
                    }
                }
                data.advancements = advancementsCount;
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur parsing stats Minecraft: " + e.getMessage());
        }
        
        return data;
    }
    
    private double getDoubleStat(com.google.gson.JsonObject obj, String key, double defaultValue) {
        try {
            if (obj.has(key)) {
                return obj.get(key).getAsDouble();
            }
        } catch (Exception e) {
            // Ignore
        }
        return defaultValue;
    }
    
    private long getLongStat(com.google.gson.JsonObject obj, String key, long defaultValue) {
        try {
            if (obj.has(key)) {
                return obj.get(key).getAsLong();
            }
        } catch (Exception e) {
            // Ignore
        }
        return defaultValue;
    }
    
    private int getIntStat(com.google.gson.JsonObject obj, String key, int defaultValue) {
        try {
            if (obj.has(key)) {
                return obj.get(key).getAsInt();
            }
        } catch (Exception e) {
            // Ignore
        }
        return defaultValue;
    }
    
    public boolean hasImportedFromMinecraft(UUID uuid) {
        return importedFromMinecraft.getOrDefault(uuid, false);
    }
    
    public int importAllMinecraftStats() {
        int importedCount = 0;
        
        // Check all world directories
        String[] worldNames = {"world", "world_nether", "world_the_end"};
        
        for (String worldName : worldNames) {
            File statsDir = new File(worldName + "/stats");
            if (statsDir.exists() && statsDir.isDirectory()) {
                File[] statFiles = statsDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (statFiles != null) {
                    for (File statFile : statFiles) {
                        String uuidStr = statFile.getName().replace(".json", "");
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            if (!importedFromMinecraft.getOrDefault(uuid, false)) {
                                // Only import if we don't have stats yet
                                if (!playerStats.containsKey(uuid) || 
                                    getPlayerStats(uuid).mobsKilled == 0) {
                                    String content = Files.readString(statFile.toPath());
                                    PlayerStatsData minecraftData = parseMinecraftStats(content);
                                    
                                    PlayerStatsData currentData = getPlayerStats(uuid);
                                    currentData.mobsKilled = minecraftData.mobsKilled;
                                    currentData.playersKilled = minecraftData.playersKilled;
                                    currentData.deaths = minecraftData.deaths;
                                    currentData.blocksBroken = minecraftData.blocksBroken;
                                    currentData.blocksPlaced = minecraftData.blocksPlaced;
                                    currentData.itemsCrafted = minecraftData.itemsCrafted;
                                    currentData.fishCaught = minecraftData.fishCaught;
                                    currentData.animalsBred = minecraftData.animalsBred;
                                    currentData.raidsWon = minecraftData.raidsWon;
                                    currentData.advancements = minecraftData.advancements;
                                    currentData.enderChestsOpened = minecraftData.enderChestsOpened;
                                    currentData.tradesUsed = minecraftData.tradesUsed;
                                    currentData.distanceWalked = minecraftData.distanceWalked;
                                    currentData.timePlayed = minecraftData.timePlayed;
                                    
                                    importedFromMinecraft.put(uuid, true);
                                    importedCount++;
                                    
                                    plugin.getLogger().info("Stats importees pour " + getPlayerName(uuid));
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Erreur import stats: " + uuidStr);
                        }
                    }
                }
            }
        }
        
        // Also check server root stats folder
        File serverStatsDir = new File("stats");
        if (serverStatsDir.exists() && serverStatsDir.isDirectory()) {
            File[] statFiles = serverStatsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (statFiles != null) {
                for (File statFile : statFiles) {
                    String uuidStr = statFile.getName().replace(".json", "");
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        if (!importedFromMinecraft.getOrDefault(uuid, false)) {
                            if (!playerStats.containsKey(uuid) || 
                                getPlayerStats(uuid).mobsKilled == 0) {
                                String content = Files.readString(statFile.toPath());
                                PlayerStatsData minecraftData = parseMinecraftStats(content);
                                
                                PlayerStatsData currentData = getPlayerStats(uuid);
                                currentData.mobsKilled = minecraftData.mobsKilled;
                                currentData.playersKilled = minecraftData.playersKilled;
                                currentData.deaths = minecraftData.deaths;
                                currentData.blocksBroken = minecraftData.blocksBroken;
                                currentData.blocksPlaced = minecraftData.blocksPlaced;
                                currentData.itemsCrafted = minecraftData.itemsCrafted;
                                currentData.fishCaught = minecraftData.fishCaught;
                                currentData.animalsBred = minecraftData.animalsBred;
                                currentData.raidsWon = minecraftData.raidsWon;
                                currentData.advancements = minecraftData.advancements;
                                currentData.enderChestsOpened = minecraftData.enderChestsOpened;
                                currentData.tradesUsed = minecraftData.tradesUsed;
                                currentData.distanceWalked = minecraftData.distanceWalked;
                                currentData.timePlayed = minecraftData.timePlayed;
                                
                                importedFromMinecraft.put(uuid, true);
                                importedCount++;
                                
                                plugin.getLogger().info("Stats importees pour " + getPlayerName(uuid));
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erreur import stats: " + uuidStr);
                    }
                }
            }
        }
        
        if (importedCount > 0) {
            saveAllStats();
        }
        
        return importedCount;
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
                data.fishCaught = config.getInt(uuidStr + ".fishCaught", 0);
                data.animalsBred = config.getInt(uuidStr + ".animalsBred", 0);
                data.raidsWon = config.getInt(uuidStr + ".raidsWon", 0);
                data.advancements = config.getInt(uuidStr + ".advancements", 0);
                data.enderChestsOpened = config.getInt(uuidStr + ".enderChestsOpened", 0);
                data.tradesUsed = config.getInt(uuidStr + ".tradesUsed", 0);
                data.smoothStoneSmelted = config.getInt(uuidStr + ".smoothStoneSmelted", 0);
                data.distanceWalked = config.getDouble(uuidStr + ".distanceWalked", 0);
                data.timePlayed = config.getLong(uuidStr + ".timePlayed", 0);
                data.damageDealt = config.getDouble(uuidStr + ".damageDealt", 0);
                data.damageTaken = config.getDouble(uuidStr + ".damageTaken", 0);
                data.foodEaten = config.getInt(uuidStr + ".foodEaten", 0);
                playerStats.put(uuid, data);
                
                // Load cached player name
                String cachedName = config.getString(uuidStr + ".lastName");
                if (cachedName != null && !cachedName.isEmpty()) {
                    playerNames.put(uuid, cachedName);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidStr);
            }
        }
    }

    public void saveAllStats() {
        FileConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerStatsData> entry : playerStats.entrySet()) {
            String uuidStr = entry.getKey().toString();
            PlayerStatsData data = entry.getValue();
            config.set(uuidStr + ".mobsKilled", data.mobsKilled);
            config.set(uuidStr + ".playersKilled", data.playersKilled);
            config.set(uuidStr + ".deaths", data.deaths);
            config.set(uuidStr + ".blocksBroken", data.blocksBroken);
            config.set(uuidStr + ".blocksPlaced", data.blocksPlaced);
            config.set(uuidStr + ".itemsCrafted", data.itemsCrafted);
            config.set(uuidStr + ".fishCaught", data.fishCaught);
            config.set(uuidStr + ".animalsBred", data.animalsBred);
            config.set(uuidStr + ".raidsWon", data.raidsWon);
            config.set(uuidStr + ".advancements", data.advancements);
            config.set(uuidStr + ".enderChestsOpened", data.enderChestsOpened);
            config.set(uuidStr + ".tradesUsed", data.tradesUsed);
            config.set(uuidStr + ".smoothStoneSmelted", data.smoothStoneSmelted);
            config.set(uuidStr + ".distanceWalked", data.distanceWalked);
            config.set(uuidStr + ".timePlayed", data.timePlayed);
            config.set(uuidStr + ".damageDealt", data.damageDealt);
            config.set(uuidStr + ".damageTaken", data.damageTaken);
            config.set(uuidStr + ".foodEaten", data.foodEaten);
            
            // Save player name
            String name = playerNames.get(entry.getKey());
            if (name != null) {
                config.set(uuidStr + ".lastName", name);
            }
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
                    int score1 = e1.getValue().getTotalProgress();
                    int score2 = e2.getValue().getTotalProgress();
                    return Integer.compare(score2, score1);
                })
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    public int getPlayerProgress(UUID uuid) {
        return getPlayerStats(uuid).getTotalProgress();
    }

    public String getPlayerName(UUID uuid) {
        // Check cache first
        if (playerNames.containsKey(uuid)) {
            return playerNames.get(uuid);
        }
        
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            String name = player.getName();
            playerNames.put(uuid, name);
            return name;
        }
        
        // Try cached name from playerdata.yml first
        String name = loadPlayerNameFromFile(uuid);
        if (name != null) {
            playerNames.put(uuid, name);
            return name;
        }
        
        // Try Mojang API for offline players
        name = fetchNameFromMojang(uuid);
        if (name != null) {
            playerNames.put(uuid, name);
            return name;
        }
        
        // Fallback to server's offline player lookup
        org.bukkit.OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        name = offline.getName() != null ? offline.getName() : uuid.toString().substring(0, 8);
        playerNames.put(uuid, name);
        return name;
    }
    
    public void updatePlayerName(UUID uuid, String name) {
        playerNames.put(uuid, name);
    }
    
    private String loadPlayerNameFromFile(UUID uuid) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            String name = config.getString(uuid.toString() + ".lastName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String fetchNameFromMojang(UUID uuid) {
        try {
            java.net.URL url = new java.net.URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            if (conn.getResponseCode() == 200) {
                String response = new String(conn.getInputStream().readAllBytes());
                int lastBrace = response.lastIndexOf("\"name\":\"");
                if (lastBrace != -1) {
                    String name = response.substring(lastBrace + 8);
                    int endQuote = name.indexOf("\"");
                    if (endQuote != -1) {
                        conn.disconnect();
                        return name.substring(0, endQuote);
                    }
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
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
        public int fishCaught = 0;
        public int animalsBred = 0;
        public int raidsWon = 0;
        public int advancements = 0;
        public int enderChestsOpened = 0;
        public int tradesUsed = 0;
        public int smoothStoneSmelted = 0; // proxy pour progres general
        public double distanceWalked = 0;
        public long timePlayed = 0;
        public double damageDealt = 0;
        public double damageTaken = 0;
        public int foodEaten = 0;
        
        public int getTotalProgress() {
            return blocksBroken + blocksPlaced + itemsCrafted + fishCaught + 
                   animalsBred + raidsWon + advancements + 
                   enderChestsOpened + tradesUsed + smoothStoneSmelted;
        }
    }
}