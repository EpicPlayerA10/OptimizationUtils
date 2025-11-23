package com.epicplayera10.optimizationutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.epicplayera10.optimizationutils.OptimizationUtils;
import com.epicplayera10.optimizationutils.manager.SimulationDistanceManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandAlias("optimizationutils|ou|opt")
@CommandPermission("optimizationutils.admin")
public class OptimizationUtilsCommand extends BaseCommand {
    @HelpCommand
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("analyzechunks")
    @Description("Analyze loaded chunks for entity counts")
    public void analyzeChunks(Player player) {
        player.sendMessage("Analyzing chunks...");

        World world = player.getWorld();
        Bukkit.getScheduler().runTaskAsynchronously(OptimizationUtils.instance(), () -> {
            Map<Chunk, Integer> chunkEntities = new HashMap<>();

            for (Chunk chunk : world.getLoadedChunks()) {
                chunkEntities.put(chunk, chunk.getEntities().length);
            }

            // Sort by value descending
            chunkEntities.entrySet().stream()
                .sorted(Map.Entry.<Chunk, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Chunk chunk = entry.getKey();
                    int entities = entry.getValue();
                    player.sendMessage(entities + " -> Chunk " + chunk.getX() + " " + chunk.getZ());
                });
        });
    }

    @Subcommand("setsimulationdistance")
    @Syntax("<new simulation distance>")
    @Description("Sets simulation distance for all worlds while respecting despawn ranges")
    public void setSimulationDistance(CommandSender sender, int newSimulationDistance) {
        for (World world : Bukkit.getWorlds()) {
            world.setSimulationDistance(newSimulationDistance);
            try {
                SimulationDistanceManager.setNMSSimulationDistance(world, newSimulationDistance);
            } catch (NoClassDefFoundError e) {
                OptimizationUtils.instance().getLogger().warning("Cannot update related configuration using NMS because your server version is not supported.");
            }
        }

        sender.sendMessage(Component.text("Successfully set simulation distance to " + newSimulationDistance + " for all worlds.").color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Make sure that \"/paper mobcaps\" will go to the max mobcap, or else use \"/ou setspawnlimit\" to lower mobcap.").color(NamedTextColor.YELLOW));
    }

    @Subcommand("setspawnlimit")
    @Syntax("<spawn category> <limit>")
    @Description("Sets mobcap for all worlds")
    public void setSpawnLimit(CommandSender sender, SpawnCategory spawnCategory, int limit) {
        for (World world : Bukkit.getWorlds()) {
            world.setSpawnLimit(spawnCategory, limit);
        }

        sender.sendMessage(Component.text("Successfully set spawn limit for " + spawnCategory.name() + " to " + limit + " for all worlds.").color(NamedTextColor.GREEN));
    }

    @Subcommand("setticksperspawn")
    @Syntax("<spawn category> <ticks>")
    @Description("Sets ticks per spawn for all worlds. This is the mob spawn frequency, how fast the server makes a check for spawning mobs.")
    public void setTicksPerSpawn(CommandSender sender, SpawnCategory spawnCategory, int ticks) {
        for (World world : Bukkit.getWorlds()) {
            world.setTicksPerSpawns(spawnCategory, ticks);
        }

        sender.sendMessage(Component.text("Successfully set ticks per spawn for " + spawnCategory.name() + " to " + ticks + " for all worlds.").color(NamedTextColor.GREEN));
    }

    @Subcommand("killoutofrange")
    @Description("Kills entities that are out of range of players in the world")
    public void killOutOfRange(Player player, EntityType entityType, int range) {
        World world = player.getWorld();

        Bukkit.getScheduler().runTask(OptimizationUtils.instance(), () -> {
            List<Entity> toRemove = world.getEntities().stream()
                .filter(entity -> entity.getType() == entityType)
                .collect(Collectors.toCollection(ArrayList::new));

            for (Player onlinePlayer : world.getPlayers()) {
                toRemove.removeIf(entity -> entity.getLocation().distance(onlinePlayer.getLocation()) <= range);
            }

            for (Entity entity : toRemove) {
                entity.remove();
            }

            player.sendMessage(Component.text("Killed " + toRemove.size() + " entities out of range.").color(NamedTextColor.GREEN));
        });
    }

    @Subcommand("killanimalsoutofrange")
    @Description("Kills animals that are out of range of players in the world")
    @Syntax("<range>")
    public void killAnimalsOutOfRange(Player player, int range) {
        World world = player.getWorld();

        Bukkit.getScheduler().runTask(OptimizationUtils.instance(), () -> {
            Collection<Animals> toRemove = world.getEntitiesByClass(Animals.class);

            for (Player onlinePlayer : world.getPlayers()) {
                toRemove.removeIf(entity -> entity.getLocation().distance(onlinePlayer.getLocation()) <= range);
            }

            for (Animals animal : toRemove) {
                animal.remove();
            }

            player.sendMessage(Component.text("Killed " + toRemove.size() + " animals out of range.").color(NamedTextColor.GREEN));
        });
    }

    @Subcommand("setviewdistance")
    @Syntax("<new view distance> [player]")
    @Description("Sets view distance for all worlds")
    public void setViewDistance(CommandSender sender, int newViewDistance, @Optional OnlinePlayer target) {
        if (target != null) {
            target.player.setViewDistance(newViewDistance);
            sender.sendMessage(Component.text("Successfully set view distance to " + newViewDistance + " for " + target.player.getName()).color(NamedTextColor.GREEN));
        } else {
            for (World world : Bukkit.getWorlds()) {
                world.setViewDistance(newViewDistance);
            }
            sender.sendMessage(Component.text("Successfully set view distance to " + newViewDistance + " for all worlds.").color(NamedTextColor.GREEN));
        }
    }

    @Subcommand("reload")
    @Description("Reloads the configuration")
    public void reload(CommandSender sender) {
        OptimizationUtils.instance().reloadConfiguration();

        sender.sendMessage(Component.text("Configuration reloaded successfully.").color(NamedTextColor.GREEN));
    }

    @Subcommand("info")
    @Description("Displays server and plugin information.")
    public void info(CommandSender sender) {
        Component message = Component.text("=== OptimizationUtils Info ===").color(NamedTextColor.GREEN)
                .append(Component.newline())
                .append(Component.newline());

        // View Distance - per world
        message = message.append(Component.text("View Distance:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        for (World world : Bukkit.getWorlds()) {
            int viewDistance = world.getViewDistance();
            message = message.append(Component.text("  " + world.getName() + ": " + viewDistance).color(NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        message = message.append(Component.newline());

        // Simulation Distance - per world
        message = message.append(Component.text("Simulation Distance:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        for (World world : Bukkit.getWorlds()) {
            int simulationDistance = world.getSimulationDistance();
            message = message.append(Component.text("  " + world.getName() + ": " + simulationDistance).color(NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        message = message.append(Component.newline());

        // Player View Distance - grouping
        Map<Integer, Long> viewDistanceGroups = Bukkit.getOnlinePlayers().stream()
                .collect(Collectors.groupingBy(
                        player -> player.getViewDistance() != 0 ? player.getViewDistance() : player.getWorld().getViewDistance(),
                        Collectors.counting()
                ));

        message = message.append(Component.text("Player View Distance:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        if (viewDistanceGroups.isEmpty()) {
            message = message.append(Component.text("  No players online").color(NamedTextColor.GRAY))
                    .append(Component.newline());
        } else {
            List<Map.Entry<Integer, Long>> sortedViewDistanceGroups = viewDistanceGroups.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                    .collect(Collectors.toList());
            for (Map.Entry<Integer, Long> entry : sortedViewDistanceGroups) {
                message = message.append(Component.text("  " + entry.getKey() + " view distance - " + entry.getValue() + " players").color(NamedTextColor.WHITE))
                        .append(Component.newline());
            }
        }
        message = message.append(Component.newline());

        // Player Simulation Distance - grouping
        Map<Integer, Long> simulationDistanceGroups = Bukkit.getOnlinePlayers().stream()
                .collect(Collectors.groupingBy(
                        player -> player.getSimulationDistance() != 0 ? player.getSimulationDistance() : player.getWorld().getSimulationDistance(),
                        Collectors.counting()
                ));

        message = message.append(Component.text("Player Simulation Distance:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        if (simulationDistanceGroups.isEmpty()) {
            message = message.append(Component.text("  No players online").color(NamedTextColor.GRAY))
                    .append(Component.newline());
        } else {
            List<Map.Entry<Integer, Long>> sortedSimulationDistanceGroups = simulationDistanceGroups.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                    .collect(Collectors.toList());
            for (Map.Entry<Integer, Long> entry : sortedSimulationDistanceGroups) {
                message = message.append(Component.text("  " + entry.getKey() + " simulation distance - " + entry.getValue() + " players").color(NamedTextColor.WHITE))
                        .append(Component.newline());
            }
        }
        message = message.append(Component.newline());

        // Entity Count - sum and per world
        int totalEntities = 0;
        message = message.append(Component.text("Entity Count:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        for (World world : Bukkit.getWorlds()) {
            int entityCount = world.getEntityCount();
            totalEntities += entityCount;
            message = message.append(Component.text("  " + world.getName() + ": " + entityCount).color(NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        message = message.append(Component.text("Total Entities: " + totalEntities).color(NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.newline());

        // Loaded Chunks - sum and per world
        int totalChunks = 0;
        message = message.append(Component.text("Loaded Chunks:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        for (World world : Bukkit.getWorlds()) {
            int chunkCount = world.getLoadedChunks().length;
            totalChunks += chunkCount;
            message = message.append(Component.text("  " + world.getName() + ": " + chunkCount).color(NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        message = message.append(Component.text("Total Loaded Chunks: " + totalChunks).color(NamedTextColor.YELLOW))
                .append(Component.newline())
                .append(Component.newline());

        // Random Tick Speed - per world
        message = message.append(Component.text("Random Tick Speed:").color(NamedTextColor.AQUA))
                .append(Component.newline());
        for (World world : Bukkit.getWorlds()) {
            int randomTickSpeed = world.getGameRuleValue(org.bukkit.GameRule.RANDOM_TICK_SPEED);
            message = message.append(Component.text("  " + world.getName() + ": " + randomTickSpeed).color(NamedTextColor.WHITE))
                    .append(Component.newline());
        }
        message = message.append(Component.newline());

        // Plugin Configuration Status
        message = message.append(Component.text("Plugin Configuration:").color(NamedTextColor.AQUA))
                .append(Component.newline());

        message = message.append(Component.text("  MSPT Calculation Mode: " + OptimizationUtils.instance().pluginConfiguration().msptCalculationMode).color(NamedTextColor.GRAY))
                .append(Component.newline());

        String dynamicMobcapStatus = OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.enabled
            ? "Enabled (threshold: " + OptimizationUtils.instance().pluginConfiguration().dynamicMobcap.msptThreshold + "ms)"
            : "Disabled";
        message = message.append(Component.text("  Dynamic Mobcap: " + dynamicMobcapStatus).color(NamedTextColor.GRAY))
                .append(Component.newline());

        String dynamicRandomTickStatus = OptimizationUtils.instance().pluginConfiguration().dynamicRandomTickSpeed.enabled
            ? "Enabled (threshold: " + OptimizationUtils.instance().pluginConfiguration().dynamicRandomTickSpeed.msptThreshold + "ms)"
            : "Disabled";
        message = message.append(Component.text("  Dynamic Random Tick Speed: " + dynamicRandomTickStatus).color(NamedTextColor.GRAY));

        sender.sendMessage(message);
    }
}
