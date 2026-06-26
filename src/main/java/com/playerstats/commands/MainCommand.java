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
            if (sender instanceof Player player) {
                plugin.getScoreboardManager().showMainMenu(player);
                player.sendMessage(Component.text("§6§lPLAYER STATS §7» ").color(NamedTextColor.GOLD)
                    .append(Component.text("Scoreboard menu opened!").color(NamedTextColor.GRAY)));
            } else {
                sender.sendMessage("§cThis command can only be used by players!");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help" -> sendHelp(sender);
            case "kills" -> handleKills(sender, args);
            case "playerkills", "pvp" -> handlePlayerKills(sender, args);
            case "deaths" -> handleDeaths(sender, args);
            case "progress" -> handleProgress(sender, args);
            case "reload" -> handleReload(sender);
            case "toggle" -> handleToggle(sender);
            default -> {
                if (sender instanceof Player player) {
                    String playerName = args[0];
                    showPlayerStats(player, playerName);
                } else {
                    sender.sendMessage("§cUnknown subcommand. Use /playerstats help");
                }
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("\n§6§l§nPLAYER STATS HELP").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("§7§m-----------------------").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("§e/stats §7- Open scoreboard menu").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats kills §7- Top 10 mob killers").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats playerkills §7- Top 10 PvP killers").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats deaths §7- Top 10 deaths").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats progress §7- Top 10 progress").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats <player> §7- View player's stats").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats toggle §7- Toggle scoreboard").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/stats reload §7- Reload config (admin)").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§7§m-----------------------\n").color(NamedTextColor.GRAY));
    }

    private void handleKills(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        plugin.getScoreboardManager().showScoreboard(player, "kills");
        player.sendMessage(Component.text("§6§lPLAYER STATS §7» ").color(NamedTextColor.GOLD)
            .append(Component.text("Showing top mob killers!").color(NamedTextColor.GRAY)));
    }

    private void handlePlayerKills(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        plugin.getScoreboardManager().showScoreboard(player, "playerkills");
        player.sendMessage(Component.text("§6§lPLAYER STATS §7» ").color(NamedTextColor.GOLD)
            .append(Component.text("Showing top PvP killers!").color(NamedTextColor.GRAY)));
    }

    private void handleDeaths(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        plugin.getScoreboardManager().showScoreboard(player, "deaths");
        player.sendMessage(Component.text("§6§lPLAYER STATS §7» ").color(NamedTextColor.GOLD)
            .append(Component.text("Showing top deaths!").color(NamedTextColor.GRAY)));
    }

    private void handleProgress(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        plugin.getScoreboardManager().showScoreboard(player, "progress");
        player.sendMessage(Component.text("§6§lPLAYER STATS §7» ").color(NamedTextColor.GOLD)
            .append(Component.text("Showing top progress!").color(NamedTextColor.GRAY)));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("playerstats.admin")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command!").color(NamedTextColor.RED));
            return;
        }
        plugin.reloadConfig();
        plugin.getStatsManager().saveAllStats();
        sender.sendMessage(Component.text("§a§lPLAYER STATS §7» ").color(NamedTextColor.GREEN)
            .append(Component.text("Configuration reloaded!").color(NamedTextColor.GRAY)));
    }

    private void handleToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        plugin.getScoreboardManager().clearScoreboard(player);
        player.sendMessage(Component.text("§6§lPLAYER STATS §7» ").color(NamedTextColor.GOLD)
            .append(Component.text("Scoreboard cleared!").color(NamedTextColor.GRAY)));
    }

    private void showPlayerStats(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID;
        String displayName;
        
        if (target != null) {
            targetUUID = target.getUniqueId();
            displayName = target.getName();
        } else {
            sender.sendMessage(Component.text("§cPlayer not found: ").color(NamedTextColor.RED)
                .append(Component.text(targetName).color(NamedTextColor.WHITE)));
            return;
        }

        StatsManager.PlayerStatsData stats = plugin.getStatsManager().getPlayerStats(targetUUID);
        
        sender.sendMessage(Component.text("\n§6§l§n" + displayName + "'s Stats").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("§7§m-----------------------").color(NamedTextColor.GRAY));
        sender.sendMessage(buildStatLine("§eMobs Killed:", stats.mobsKilled));
        sender.sendMessage(buildStatLine("§cPlayers Killed:", stats.playersKilled));
        sender.sendMessage(buildStatLine("§4Deaths:", stats.deaths));
        sender.sendMessage(buildStatLine("§aBlocks Broken:", stats.blocksBroken));
        sender.sendMessage(buildStatLine("§aBlocks Placed:", stats.blocksPlaced));
        sender.sendMessage(buildStatLine("§bItems Crafted:", stats.itemsCrafted));
        sender.sendMessage(buildStatLine("§dAnimals Bred:", stats.animalsBred));
        sender.sendMessage(Component.text("§7§m-----------------------\n").color(NamedTextColor.GRAY));
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
            completions.addAll(Arrays.asList("help", "kills", "playerkills", "deaths", "progress", "reload", "toggle"));
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