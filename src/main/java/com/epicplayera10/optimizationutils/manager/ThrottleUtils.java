package com.epicplayera10.optimizationutils.manager;

import com.epicplayera10.optimizationutils.OptimizationUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ThrottleUtils {
    // Avoid spamming logs
    private static long lastLogTime = -1;

    public static boolean shouldAbortMobspawn(World world, float msptThreshold, String prefix) {
        if (!OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.enabled) {
            return false;
        }

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
