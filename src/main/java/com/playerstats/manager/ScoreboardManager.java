package com.playerstats.manager;

import com.playerstats.PlayerStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.UUID;

public class ScoreboardManager {

    private final PlayerStats plugin;
    private final String[] scoreboardTitles = {
        "§6§lMOB KILLS",
        "§c§lPLAYER KILLS",
        "§4§lDEATHS",
        "§a§lPROGRESS"
    };
    private int currentTitleIndex = 0;

    public ScoreboardManager(PlayerStats plugin) {
        this.plugin = plugin;
    }

    public void showScoreboard(Player player, String type) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String objectiveName = "ps_" + type.toLowerCase();
        
        Objective objective = board.getObjective(objectiveName);
        if (objective != null) {
            objective.unregister();
        }

        String title = getTitleForType(type);
        objective = board.registerNewObjective(objectiveName, Criteria.DUMMY, 
            Component.text(title).color(NamedTextColor.GOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        StatsManager statsManager = plugin.getStatsManager();
        List<UUID> topPlayers;
        
        switch (type.toLowerCase()) {
            case "kills" -> topPlayers = statsManager.getTopMobsKilled(10);
            case "playerkills" -> topPlayers = statsManager.getTopPlayersKilled(10);
            case "deaths" -> topPlayers = statsManager.getTopDeaths(10);
            case "progress" -> topPlayers = statsManager.getTopProgress(10);
            default -> topPlayers = statsManager.getTopMobsKilled(10);
        }

        int score = 10;
        for (int i = 0; i < topPlayers.size(); i++) {
            UUID uuid = topPlayers.get(i);
            String playerName = statsManager.getPlayerName(uuid);
            int value = getValueForPlayer(uuid, type);
            
            String entry = getRankPrefix(i + 1) + " " + playerName + ": " + value;
            objective.getScore(entry).setScore(score--);
        }

        if (topPlayers.isEmpty()) {
            objective.getScore("No data yet").setScore(10);
        }

        player.setScoreboard(board);
    }

    public void showMainMenu(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String objectiveName = "ps_main";
        
        Objective objective = board.getObjective(objectiveName);
        if (objective != null) {
            objective.unregister();
        }

        objective = board.registerNewObjective(objectiveName, Criteria.DUMMY, 
            Component.text("§6§lPLAYER STATS").color(NamedTextColor.GOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore("§7§m--------------------").setScore(15);
        objective.getScore("§e/stats kills §7- Mob Kills").setScore(14);
        objective.getScore("§e/stats playerkills §7- PvP").setScore(13);
        objective.getScore("§e/stats deaths §7- Deaths").setScore(12);
        objective.getScore("§e/stats progress §7- Progress").setScore(11);
        objective.getScore("§e/stats <player> §7- View stats").setScore(10);
        objective.getScore("§7§m--------------------").setScore(9);
        
        player.setScoreboard(board);
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboard().getObjectives().stream()
                    .anyMatch(obj -> obj.getName().startsWith("ps_"))) {
                // Keep current scoreboard updated
            }
        }
    }

    private String getTitleForType(String type) {
        return switch (type.toLowerCase()) {
            case "kills" -> "§6§lTOP MOB KILLS";
            case "playerkills" -> "§c§lTOP PLAYER KILLS";
            case "deaths" -> "§4§lMOST DEATHS";
            case "progress" -> "§a§lTOP PROGRESS";
            default -> "§6§lPLAYER STATS";
        };
    }

    private String getRankPrefix(int rank) {
        return switch (rank) {
            case 1 -> "§6✦";
            case 2 -> "§f✦";
            case 3 -> "§c✦";
            case 4, 5, 6, 7, 8, 9, 10 -> "§7✦";
            default -> "§7";
        };
    }

    private int getValueForPlayer(UUID uuid, String type) {
        StatsManager.PlayerStatsData data = plugin.getStatsManager().getPlayerStats(uuid);
        return switch (type.toLowerCase()) {
            case "kills" -> data.mobsKilled;
            case "playerkills" -> data.playersKilled;
            case "deaths" -> data.deaths;
            case "progress" -> data.blocksBroken + data.blocksPlaced + data.itemsCrafted;
            default -> 0;
        };
    }

    public String getNextScoreboardTitle() {
        currentTitleIndex = (currentTitleIndex + 1) % scoreboardTitles.length;
        return scoreboardTitles[currentTitleIndex];
    }

    public void clearScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Objective obj : board.getObjectives()) {
            if (obj.getName().startsWith("ps_")) {
                obj.unregister();
            }
        }
        player.setScoreboard(board);
    }
}