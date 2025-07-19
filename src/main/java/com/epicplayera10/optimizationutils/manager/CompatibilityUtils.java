package com.epicplayera10.optimizationutils.manager;

import org.bukkit.Bukkit;

public class CompatibilityUtils {
    public static float getTargetMspt() {
        try {
            return 1000f / Bukkit.getServer().getServerTickManager().getTickRate();
        } catch (NoSuchMethodError e) {
            // Fallback for older versions that don't have getTickRate
            return 50f; // Default tick rate is 20 TPS, so 1000ms / 20 = 50ms per tick
        }
    }
}
