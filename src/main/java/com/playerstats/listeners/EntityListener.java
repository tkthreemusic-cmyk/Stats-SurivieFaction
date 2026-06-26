package com.playerstats.listeners;

import com.playerstats.PlayerStats;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EntityListener implements Listener {

    private final PlayerStats plugin;
    
    private static final Set<String> HOSTILE_MOBS = new HashSet<>(Arrays.asList(
        "ZOMBIE", "CREEPER", "SKELETON", "SPIDER", "CAVE_SPIDER", "ENDERMAN",
        "WITCH", "PHANTOM", "DROWNED", "HUSK", "STRAY", "BLAZE", "GHAST",
        "MAGMA_CUBE", "PIGLIN", "PIGLIN_BRUTE", "HOGLIN", "ZOGLIN", "WARDEN",
        "SHULKER", "ELDER_GUARDIAN", "GUARDIAN", "VEX", "EVOKER", "VINDICATOR",
        "RAVAGER", "ILLUSIONER", "PILLAGER", "IRON_GOLEM", "SNOW_GOLEM",
        "SLIME", "SILVERFISH", "ENDERMITE", "COD", "SALMON", "TROPICAL_FISH",
        "PUFFERFISH", "DOLPHIN", "TURTLE", "TADPOLE", "ALLAY", "MOOSHROOM",
        "CHICKEN", "COW", "PIG", "SHEEP", "RABBIT", "WOLF", "CAT", "OCELOT",
        "HORSE", "DONKEY", "MULE", "LLAMA", "TRADER_LLAMA", "PARROT", "FOX",
        "BEE", "GOAT", "FROG", "ALLAY", "CAMEL", "SNIFFER", "ARMADILLO",
        "WANDA", "CREAKING", "BREEZE", "GEOMANCER", "SMART_MOVING_ENTITY"
    ));

    public EntityListener(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        org.bukkit.entity.LivingEntity victim = event.getEntity();
        
        if (victim instanceof Player) {
            return;
        }
        
        if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            plugin.getStatsManager().incrementMobsKilled(killer.getUniqueId());
            
            double damageDealt = event.getDroppedExp();
            plugin.getStatsManager().addDamageDealt(killer.getUniqueId(), damageDealt);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            double damage = event.getFinalDamage();
            plugin.getStatsManager().addDamageDealt(attacker.getUniqueId(), damage);
        }
        
        if (event.getEntity() instanceof Player victim) {
            double damage = event.getFinalDamage();
            plugin.getStatsManager().addDamageTaken(victim.getUniqueId(), damage);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player breeder) {
            plugin.getStatsManager().incrementAnimalsBred(breeder.getUniqueId());
        }
    }
}