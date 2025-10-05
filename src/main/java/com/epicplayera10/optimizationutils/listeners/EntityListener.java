package com.epicplayera10.optimizationutils.listeners;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.epicplayera10.optimizationutils.OptimizationUtils;
import com.epicplayera10.optimizationutils.manager.ThrottleUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.spawner.Spawner;

public class EntityListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        if (ThrottleUtils.shouldThrottle(
            event.getLocation().getWorld(),
            OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.msptThreshold,
            "CreatureSpawnEvent"
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        if (event.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        if (ThrottleUtils.shouldThrottle(event.getSpawnLocation().getWorld(), OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.msptThreshold, "PreCreatureSpawnEvent")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNaturalSpawnPickChunks(PlayerNaturallySpawnCreaturesEvent event) {
        if (ThrottleUtils.shouldThrottle(event.getPlayer().getWorld(), OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.msptThreshold, "PlayerNaturallySpawnCreaturesEvent")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawnerSpawn(PreSpawnerSpawnEvent event) {
        if (!OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.throttleSpawners) {
            return;
        }

        if (ThrottleUtils.shouldThrottle(event.getSpawnerLocation().getWorld(), OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.msptThreshold, "PreSpawnerSpawnEvent")) {
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


}
