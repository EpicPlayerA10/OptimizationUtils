package com.epicplayera10.optimizationutils.listeners;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.epicplayera10.optimizationutils.OptimizationUtils;
import com.epicplayera10.optimizationutils.manager.CompatibilityUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.spawner.Spawner;

public class EntityListener implements Listener {

    // Avoid spamming logs
    private long lastLogTime = -1;

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        if (shouldAbortMobspawn(event.getLocation().getWorld(), "[CreatureSpawnEvent] ")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        if (event.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        if (shouldAbortMobspawn(event.getSpawnLocation().getWorld(), "[PreCreatureSpawnEvent] ")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNaturalSpawnPickChunks(PlayerNaturallySpawnCreaturesEvent event) {
        if (shouldAbortMobspawn(event.getPlayer().getWorld(), "[PlayerNaturallySpawnCreaturesEvent] ")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawnerSpawn(PreSpawnerSpawnEvent event) {
        if (!OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.throttleSpawners) {
            return;
        }

        if (shouldAbortMobspawn(event.getSpawnerLocation().getWorld(), "[PreSpawnerSpawnEvent] ")) {
            event.setCancelled(true);
            event.setShouldAbortSpawn(true);

            // If canceled, set spawner delay to 1 tick for spawners to work
            Bukkit.getScheduler().runTask(OptimizationUtils.instance(), () -> {
                Block block = event.getSpawnerLocation().getBlock();
                Spawner spawner = (Spawner) block.getState();
                spawner.setDelay(1);
            });
        }
    }

    private boolean shouldAbortMobspawn(World world, String prefix) {
        if (!OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.enabled) {
            return false;
        }

        float targetMspt = CompatibilityUtils.getTargetMspt();
        float msptThreshold = targetMspt - OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.targetMsptMargin;

        // If server is overloaded, cancel mob spawns
        if (Bukkit.getServer().getAverageTickTime() > msptThreshold) {
            if (lastLogTime == -1 || System.currentTimeMillis() - lastLogTime > 10000) { // Log every 10 seconds
                lastLogTime = System.currentTimeMillis();
                OptimizationUtils.instance().getLogger().info(prefix + "Server is overloaded, aborting mob spawn. Entities count: " + world.getEntityCount());
            }

            return true;
        }

        return false;
    }
}
