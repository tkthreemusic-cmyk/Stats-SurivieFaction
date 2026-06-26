package com.playerstats.listeners;

import com.playerstats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final PlayerStats plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Double> lastHealth = new HashMap<>();

    public PlayerListener(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Save player name when they join
        plugin.getStatsManager().updatePlayerName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        plugin.getStatsManager().incrementBlocksBroken(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        plugin.getStatsManager().incrementBlocksPlaced(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraft(CraftItemEvent event) {
        if (event.isCancelled()) return;
        if (event.getWhoClicked() instanceof Player player) {
            plugin.getStatsManager().incrementItemsCrafted(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.getStatsManager().incrementDeaths(event.getEntity().getUniqueId());
        
        if (event.getEntity().getKiller() != null) {
            plugin.getStatsManager().incrementPlayersKilled(event.getEntity().getKiller().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            double distance = event.getFrom().distance(event.getTo());
            plugin.getStatsManager().addDistanceWalked(event.getPlayer().getUniqueId(), distance);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getStatsManager().saveAllStats();
        lastLocations.remove(event.getPlayer().getUniqueId());
        lastHealth.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        ItemStack item = event.getItem();
        if (item != null && isEdible(item)) {
            plugin.getStatsManager().incrementFoodEaten(event.getPlayer().getUniqueId());
        }
    }

    private boolean isEdible(ItemStack item) {
        return item.getType().isEdible();
    }
}