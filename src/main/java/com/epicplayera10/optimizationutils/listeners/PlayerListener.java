package com.epicplayera10.optimizationutils.listeners;

import com.epicplayera10.optimizationutils.OptimizationUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Map<UUID, Integer> viewDistanceOverrides = OptimizationUtils.instance().dataConfiguration().viewDistanceOverrides;
        if (viewDistanceOverrides.containsKey(player.getUniqueId())) {
            // Apply the stored view distance override
            int viewDistance = viewDistanceOverrides.get(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(OptimizationUtils.instance(), () -> {
                player.setViewDistance(viewDistance);
            }, 20L); // Delay by 1 second to ensure proper application
        }
    }
}
