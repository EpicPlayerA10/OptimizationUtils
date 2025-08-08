package com.epicplayera10.optimizationutils.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
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

    @Subcommand("reload")
    @Description("Reloads the configuration")
    public void reload(CommandSender sender) {
        OptimizationUtils.instance().reloadConfiguration();

        sender.sendMessage(Component.text("Configuration reloaded successfully.").color(NamedTextColor.GREEN));
    }
}
