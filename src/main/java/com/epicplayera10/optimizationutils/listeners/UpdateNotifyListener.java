package com.epicplayera10.optimizationutils.listeners;

import com.epicplayera10.optimizationutils.OptimizationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("optimizationutils.updatechecker")) {
            return;
        }

        if (OptimizationUtils.instance().getUpdateChecker() == null || !OptimizationUtils.instance().getUpdateChecker().isUpdateAvailable()) {
            return;
        }

        // Send notification to player
        OptimizationUtils.instance().getServer().getScheduler().runTaskLater(OptimizationUtils.instance(), () -> {
            event.getPlayer().sendMessage(Component.text("==========================================", NamedTextColor.YELLOW));
            event.getPlayer().sendMessage(Component.text("A new version of OptimizationUtils is available!", NamedTextColor.YELLOW, TextDecoration.BOLD));
            event.getPlayer().sendMessage(Component.text("Current version: ", NamedTextColor.GRAY)
                    .append(Component.text(OptimizationUtils.instance().getDescription().getVersion(), NamedTextColor.RED)));
            event.getPlayer().sendMessage(Component.text("Latest version: ", NamedTextColor.GRAY)
                    .append(Component.text(OptimizationUtils.instance().getUpdateChecker().getLatestVersion(), NamedTextColor.GREEN)));
            event.getPlayer().sendMessage(Component.text("Download: ", NamedTextColor.GRAY)
                    .append(Component.text("https://modrinth.com/plugin/optimizationutils", NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/optimizationutils"))));
            event.getPlayer().sendMessage(Component.text("==========================================", NamedTextColor.YELLOW));
        }, 40L); // 2 seconds delay
    }
}