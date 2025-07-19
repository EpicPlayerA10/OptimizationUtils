package com.epicplayera10.optimizationutils.listeners;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.epicplayera10.optimizationutils.OptimizationUtils;
import com.epicplayera10.optimizationutils.manager.CompatibilityUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityListener implements Listener {

    // Avoid spamming logs
    private long lastLogTime = -1;

    @EventHandler
    public void onMobSpawn(PreCreatureSpawnEvent event) {
        if (event.getReason() == CreatureSpawnEvent.SpawnReason.NATURAL && shouldAbortMobspawn(event.getSpawnLocation().getWorld(), "[C] ")) {
            event.setShouldAbortSpawn(true);
        }
    }

    @EventHandler
    public void onNaturalSpawnPickChunks(PlayerNaturallySpawnCreaturesEvent event) {
        if (shouldAbortMobspawn(event.getPlayer().getWorld(), "[N] ")) {
            event.setCancelled(true);
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
