package com.epicplayera10.optimizationutils.manager;

import com.epicplayera10.optimizationutils.OptimizationUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ThrottleUtils {
    // Avoid spamming logs
    private static long lastLogTime = -1;

    public static boolean shouldThrottle(World world, float msptThreshold, String action) {
        double currentMspt = ThrottleUtils.getMspt();

        // If server is overloaded, cancel mob spawns
        if (currentMspt > msptThreshold) {
            if (lastLogTime == -1 || System.currentTimeMillis() - lastLogTime > 10000) { // Log every 10 seconds
                lastLogTime = System.currentTimeMillis();
                OptimizationUtils.instance().getLogger().info("Server is overloaded (" + currentMspt + "ms), throttling " + action + ". Entities count: " + world.getEntityCount());
            }

            return true;
        }

        return false;
    }

    /**
     * Gets the milliseconds per tick (MSPT) of the server.
     */
    public static double getMspt() {
        return switch (OptimizationUtils.instance().pluginConfiguration().msptCalculationMode) {
            case AVERAGE_5S -> Bukkit.getAverageTickTime();
            case LAST_TICK -> {
                int lastTick = Bukkit.getCurrentTick() - 1;
                if (lastTick < 0) {
                    yield 0; // Server just started, no tick data available yet
                }
                long currentMsptNanos = Bukkit.getTickTimes()[lastTick % Bukkit.getTickTimes().length];
//                System.out.println("current tick: " + Bukkit.getCurrentTick());
//                System.out.println("tick times length: " + Bukkit.getTickTimes().length);
//                System.out.println("tick times: " + Arrays.toString(Bukkit.getTickTimes()));
//                System.out.println("averageMspt: " + Bukkit.getAverageTickTime() + "ms");
//                System.out.println("currentMspt: " + currentMspt + "ms");

                // Convert from nanos to millis
                yield currentMsptNanos / 1_000_000.0D;
            }
        };
    }
}
