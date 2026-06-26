package com.playerstats.commands;

import com.playerstats.PlayerStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final PlayerStats plugin;

    public AdminCommand(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("playerstats.admin")) {
            sender.sendMessage(Component.text("§cYou don't have permission to use this command!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        String playerName = args[1];

        Player target = Bukkit.getPlayer(playerName);
        UUID targetUUID;
        
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            sender.sendMessage(Component.text("§cPlayer not found: " + playerName).color(NamedTextColor.RED));
            return true;
        }

        switch (action) {
            case "reset" -> handleReset(sender, targetUUID, playerName, args);
            case "set" -> handleSet(sender, targetUUID, playerName, args);
            case "add" -> handleAdd(sender, targetUUID, playerName, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleReset(CommandSender sender, UUID targetUUID, String playerName, String[] args) {
        if (args.length < 3) {
            String statType = args.length >= 2 ? args[2].toLowerCase() : "";
            if (statType.isEmpty() || statType.equals("all")) {
                plugin.getStatsManager().resetPlayerStats(targetUUID);
                sender.sendMessage(Component.text("§a§lPLAYER STATS §7» ").color(NamedTextColor.GREEN)
                    .append(Component.text("Reset all stats for " + playerName).color(NamedTextColor.WHITE)));
            } else {
                resetSpecificStat(sender, targetUUID, playerName, statType);
            }
        } else {
            resetSpecificStat(sender, targetUUID, playerName, args[2].toLowerCase());
        }
    }

    private void resetSpecificStat(CommandSender sender, UUID targetUUID, String playerName, String statType) {
        var stats = plugin.getStatsManager().getPlayerStats(targetUUID);
        
        switch (statType) {
            case "mobskilled", "mobs", "kills" -> stats.mobsKilled = 0;
            case "playerskilled", "players", "pvp" -> stats.playersKilled = 0;
            case "deaths" -> stats.deaths = 0;
            case "blocksbroken" -> stats.blocksBroken = 0;
            case "blocksplaced" -> stats.blocksPlaced = 0;
            case "itemscrafted", "items", "crafted" -> stats.itemsCrafted = 0;
            default -> {
                sender.sendMessage(Component.text("§cUnknown stat type: " + statType).color(NamedTextColor.RED));
                sender.sendMessage(Component.text("§7Valid types: mobs, players, deaths, blocksbroken, blocksplaced, items").color(NamedTextColor.GRAY));
                return;
            }
        }
        
        sender.sendMessage(Component.text("§a§lPLAYER STATS §7» ").color(NamedTextColor.GREEN)
            .append(Component.text("Reset " + statType + " for " + playerName).color(NamedTextColor.WHITE)));
    }

    private void handleSet(CommandSender sender, UUID targetUUID, String playerName, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("§cUsage: /psta set <player> <stat> <value>").color(NamedTextColor.RED));
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid number: " + args[3]).color(NamedTextColor.RED));
            return;
        }

        plugin.getStatsManager().setPlayerStat(targetUUID, args[2], value);
        sender.sendMessage(Component.text("§a§lPLAYER STATS §7» ").color(NamedTextColor.GREEN)
            .append(Component.text("Set " + args[2] + " to " + value + " for " + playerName).color(NamedTextColor.WHITE)));
    }

    private void handleAdd(CommandSender sender, UUID targetUUID, String playerName, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("§cUsage: /psta add <player> <stat> <value>").color(NamedTextColor.RED));
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("§cInvalid number: " + args[3]).color(NamedTextColor.RED));
            return;
        }

        var stats = plugin.getStatsManager().getPlayerStats(targetUUID);
        String statType = args[2].toLowerCase();
        
        switch (statType) {
            case "mobskilled", "mobs", "kills" -> stats.mobsKilled += value;
            case "playerskilled", "players", "pvp" -> stats.playersKilled += value;
            case "deaths" -> stats.deaths += value;
            case "blocksbroken" -> stats.blocksBroken += value;
            case "blocksplaced" -> stats.blocksPlaced += value;
            case "itemscrafted", "items", "crafted" -> stats.itemsCrafted += value;
            default -> {
                sender.sendMessage(Component.text("§cUnknown stat type: " + statType).color(NamedTextColor.RED));
                return;
            }
        }
        
        sender.sendMessage(Component.text("§a§lPLAYER STATS §7» ").color(NamedTextColor.GREEN)
            .append(Component.text("Added " + value + " to " + statType + " for " + playerName).color(NamedTextColor.WHITE)));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("\n§c§l§nPLAYER STATS ADMIN HELP").color(NamedTextColor.RED));
        sender.sendMessage(Component.text("§7§m-----------------------").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("§e/psta reset <player> [stat] §7- Reset player stats").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/psta set <player> <stat> <value> §7- Set stat value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§e/psta add <player> <stat> <value> §7- Add to stat value").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("§7§m-----------------------").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("§7Stats: mobs, players, deaths, blocksbroken,").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("§7         blocksplaced, itemscrafted").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("§7§m-----------------------\n").color(NamedTextColor.GRAY));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("playerstats.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reset", "set", "add"));
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 3) {
            completions.addAll(Arrays.asList("mobskilled", "playerskilled", "deaths", 
                "blocksbroken", "blocksplaced", "itemscrafted"));
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .toList();
    }
}