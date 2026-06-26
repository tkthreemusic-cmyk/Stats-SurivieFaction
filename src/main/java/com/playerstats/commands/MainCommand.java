package com.playerstats.commands;

import com.playerstats.PlayerStats;
import com.playerstats.manager.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final PlayerStats plugin;

    public MainCommand(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(sender);
            case "kills", "kill" -> handleKills(sender);
            case "playerkills", "pvp", "pk" -> handlePlayerKills(sender);
            case "deaths", "death" -> handleDeaths(sender);
            case "progress", "prog" -> handleProgress(sender);
            case "reload" -> handleReload(sender);
            case "reset" -> {
                if (sender.hasPermission("playerstats.admin")) {
                    handleReset(sender, args);
                } else {
                    sender.sendMessage(Component.text("§cVous n'avez pas la permission!").color(NamedTextColor.RED));
                }
            }
            default -> {
                if (sender instanceof Player player) {
                    String playerName = args[0];
                    showPlayerStats(player, playerName);
                } else {
                    sender.sendMessage("§cUnknown subcommand. Use /stats help");
                }
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text(" §6§l▬▬▬ PLAYER STATS ▬▬▬").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text(" §7Classements des statistiques des joueurs").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text(" §e/stats kills §7- Classement kills de mobs").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(" §e/stats playerkills §7- Classement PvP").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(" §e/stats deaths §7- Classement morts").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(" §e/stats progress §7- Classement progression").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(" §e/stats <joueur> §7- Voir les stats d'un joueur").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(""));
        if (sender.hasPermission("playerstats.admin")) {
            sender.sendMessage(Component.text(" §c/stats reload §7- Recharger la config").color(NamedTextColor.RED));
            sender.sendMessage(Component.text(" §c/stats reset <joueur> §7- Reset les stats").color(NamedTextColor.RED));
        }
        sender.sendMessage(Component.text(" §7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n").color(NamedTextColor.GRAY));
    }

    private void handleKills(CommandSender sender) {
        sendRankingMessage(sender, "KILLS DE MOBS", "mobsKilled", NamedTextColor.GOLD);
    }

    private void handlePlayerKills(CommandSender sender) {
        sendRankingMessage(sender, "KILLS JOUEURS (PvP)", "playersKilled", NamedTextColor.DARK_RED);
    }

    private void handleDeaths(CommandSender sender) {
        sendRankingMessage(sender, "MORTS", "deaths", NamedTextColor.RED);
    }

    private void handleProgress(CommandSender sender) {
        List<UUID> topPlayers = plugin.getStatsManager().getTopProgress(10);
        
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text(" §a§l▬▬▬ TOP 10 PROGRESS ▬▬▬").color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text(""));
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(Component.text(" §7Aucun joueur n'a de progression encore.").color(NamedTextColor.GRAY));
        } else {
            for (int i = 0; i < topPlayers.size(); i++) {
                UUID uuid = topPlayers.get(i);
                String playerName = plugin.getStatsManager().getPlayerName(uuid);
                StatsManager.PlayerStatsData data = plugin.getStatsManager().getPlayerStats(uuid);
                int progress = data.blocksBroken + data.blocksPlaced + data.itemsCrafted;
                sender.sendMessage(buildRankingLine(i + 1, playerName, progress));
            }
        }
        sender.sendMessage(Component.text(" §7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n").color(NamedTextColor.GRAY));
    }

    private void sendRankingMessage(CommandSender sender, String title, String statType, NamedTextColor titleColor) {
        List<UUID> topPlayers;
        switch (statType) {
            case "mobsKilled" -> topPlayers = plugin.getStatsManager().getTopMobsKilled(10);
            case "playersKilled" -> topPlayers = plugin.getStatsManager().getTopPlayersKilled(10);
            case "deaths" -> topPlayers = plugin.getStatsManager().getTopDeaths(10);
            default -> topPlayers = new ArrayList<>();
        }
        
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text(" §" + getColorCode(titleColor) + "▬▬▬ TOP 10 " + title + " ▬▬▬").color(titleColor));
        sender.sendMessage(Component.text(""));
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(Component.text(" §7Aucun joueur dans ce classement.").color(NamedTextColor.GRAY));
        } else {
            for (int i = 0; i < topPlayers.size(); i++) {
                UUID uuid = topPlayers.get(i);
                String playerName = plugin.getStatsManager().getPlayerName(uuid);
                int value = getStatValue(uuid, statType);
                sender.sendMessage(buildRankingLine(i + 1, playerName, value));
            }
        }
        sender.sendMessage(Component.text(" §7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n").color(NamedTextColor.GRAY));
    }

    private Component buildRankingLine(int rank, String playerName, int value) {
        String medal = getMedal(rank);
        String rankText = getRankText(rank);
        NamedTextColor valueColor = getValueColor(rank);
        
        return Component.text(" " + medal + " " + rankText + " §f" + playerName + " §7- ")
            .append(Component.text(String.valueOf(value)).color(valueColor));
    }

    private String getMedal(int rank) {
        return switch (rank) {
            case 1 -> "§6🥇";
            case 2 -> "§f🥈";
            case 3 -> "§c🥉";
            default -> "§7▪";
        };
    }

    private String getRankText(int rank) {
        String suffix;
        if (rank == 1) suffix = "er";
        else suffix = "ème";
        return "§e#" + rank + suffix;
    }

    private NamedTextColor getValueColor(int rank) {
        switch (rank) {
            case 1: return NamedTextColor.GOLD;
            case 2: return NamedTextColor.WHITE;
            case 3: return NamedTextColor.YELLOW;
            default: return NamedTextColor.GRAY;
        }
    }

    private String getColorCode(NamedTextColor color) {
        if (color == NamedTextColor.GOLD) return "6";
        if (color == NamedTextColor.DARK_RED) return "4";
        if (color == NamedTextColor.RED) return "c";
        if (color == NamedTextColor.GREEN) return "a";
        return "e";
    }

    private int getStatValue(UUID uuid, String statType) {
        StatsManager.PlayerStatsData data = plugin.getStatsManager().getPlayerStats(uuid);
        return switch (statType) {
            case "mobsKilled" -> data.mobsKilled;
            case "playersKilled" -> data.playersKilled;
            case "deaths" -> data.deaths;
            default -> 0;
        };
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("playerstats.admin")) {
            sender.sendMessage(Component.text("§cVous n'avez pas la permission!").color(NamedTextColor.RED));
            return;
        }
        plugin.reloadConfig();
        plugin.getStatsManager().saveAllStats();
        sender.sendMessage(Component.text("§a✓ ").color(NamedTextColor.GREEN)
            .append(Component.text("Configuration rechargée!").color(NamedTextColor.WHITE)));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("§cUsage: /stats reset <joueur>").color(NamedTextColor.RED));
            return;
        }
        
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        UUID targetUUID;
        
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            sender.sendMessage(Component.text("§cJoueur non trouvé: " + playerName).color(NamedTextColor.RED));
            return;
        }
        
        plugin.getStatsManager().resetPlayerStats(targetUUID);
        sender.sendMessage(Component.text("§a✓ ").color(NamedTextColor.GREEN)
            .append(Component.text("Stats de " + playerName + " ont été reset!").color(NamedTextColor.WHITE)));
    }

    private void showPlayerStats(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String displayName;
        
        if (target != null) {
            targetUUID = target.getUniqueId();
            displayName = target.getName();
        } else {
            sender.sendMessage(Component.text("§cJoueur non trouvé: ").color(NamedTextColor.RED)
                .append(Component.text(targetName).color(NamedTextColor.WHITE)));
            return;
        }

        StatsManager.PlayerStatsData stats = plugin.getStatsManager().getPlayerStats(targetUUID);
        
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text(" §6§l▬▬▬ " + displayName + " ▬▬▬").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(buildStatLine(" §e🗡️ Mobs tués:", stats.mobsKilled));
        sender.sendMessage(buildStatLine(" §c⚔️ Joueurs tués:", stats.playersKilled));
        sender.sendMessage(buildStatLine(" §4💀 Morts:", stats.deaths));
        sender.sendMessage(Component.text(" §7§m──────────────").color(NamedTextColor.GRAY));
        sender.sendMessage(buildStatLine(" §a⛏️ Blocs cassés:", stats.blocksBroken));
        sender.sendMessage(buildStatLine(" §a🧱 Blocs posés:", stats.blocksPlaced));
        sender.sendMessage(buildStatLine(" §b🔨 Objets craftés:", stats.itemsCrafted));
        sender.sendMessage(buildStatLine(" §d🐄 Animaux élevés:", stats.animalsBred));
        sender.sendMessage(Component.text(" §7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n").color(NamedTextColor.GRAY));
    }

    private Component buildStatLine(String label, int value) {
        return Component.text(label).color(NamedTextColor.YELLOW)
            .append(Component.text(" " + value).color(NamedTextColor.WHITE));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "kills", "playerkills", "deaths", "progress"));
            if (sender.hasPermission("playerstats.admin")) {
                completions.add("reload");
                completions.add("reset");
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        
        return completions;
    }
}