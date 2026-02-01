package com.epicplayera10.optimizationutils;

import co.aikar.commands.PaperCommandManager;
import com.epicplayera10.optimizationutils.commands.OptimizationUtilsCommand;
import com.epicplayera10.optimizationutils.config.ConfigurationFactory;
import com.epicplayera10.optimizationutils.config.DataConfiguration;
import com.epicplayera10.optimizationutils.config.PluginConfiguration;
import com.epicplayera10.optimizationutils.listeners.EntityListener;
import com.epicplayera10.optimizationutils.listeners.PlayerListener;
import com.epicplayera10.optimizationutils.listeners.UpdateNotifyListener;
import com.epicplayera10.optimizationutils.manager.ThrottleUtils;
import com.epicplayera10.optimizationutils.updatechecker.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class OptimizationUtils extends JavaPlugin {

    private PluginConfiguration pluginConfiguration;
    private DataConfiguration dataConfiguration;
    private UpdateChecker updateChecker;

    private static OptimizationUtils instance;

    @Override
    public void onEnable() {
        instance = this;

        setupMetrics();

        this.pluginConfiguration = ConfigurationFactory.createPluginConfiguration(new File(this.getDataFolder(), "config.yml"));
        this.dataConfiguration = ConfigurationFactory.createDataConfiguration(new File(this.getDataFolder(), "data.yml"));

        // Restore original random tick speeds on enable if server has been stopped incorrectly
        restoreOriginalRandomTickSpeeds();

        Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
        Bukkit.getPluginManager().registerEvents(new UpdateNotifyListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        // Plugin startup logic
        registerCommands();

        // Check for updates
        this.updateChecker = new UpdateChecker();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            this.updateChecker.checkForUpdates();
        }, 0L, 24 * 60 * 60 * 20); // Check every 24h

        // Dynamic Random Tick Speed Task
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!this.pluginConfiguration().dynamicRandomTickSpeed.enabled) return;

            for (World world : Bukkit.getWorlds()) {
                if (ThrottleUtils.shouldThrottle(world, this.pluginConfiguration().dynamicRandomTickSpeed.msptThreshold, "RandomTickSpeed")) {
                    // Store original randomtickspeed if not already stored
                    int currentRandomTickSpeed = world.getGameRuleValue(GameRule.RANDOM_TICK_SPEED);

                    if (!this.dataConfiguration().originalRandomTickSpeeds.containsKey(world.getName())) {
                        if (this.pluginConfiguration().debug) {
                            this.getLogger().info("Storing original random tick speed for world " + world.getName() + ": " + currentRandomTickSpeed);
                        }
                        this.dataConfiguration().originalRandomTickSpeeds.put(world.getName(), currentRandomTickSpeed);
                        this.dataConfiguration().save();
                    } else {
                        if (currentRandomTickSpeed != 0) {
                            // Update in case it was changed manually
                            if (this.pluginConfiguration().debug) {
                                this.getLogger().info("Updating original random tick speed for world " + world.getName() + ": " + currentRandomTickSpeed);
                            }
                            this.dataConfiguration().originalRandomTickSpeeds.put(world.getName(), currentRandomTickSpeed);
                            this.dataConfiguration().save();
                        }
                    }

                    // Disable random ticks
                    world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
                } else {
                    // Restore original random tick speed if it was changed
                    if (this.dataConfiguration().originalRandomTickSpeeds.containsKey(world.getName())) {
                        if (this.pluginConfiguration().debug) {
                            this.getLogger().info("Restoring original random tick speed for world " + world.getName() + ": " + this.dataConfiguration().originalRandomTickSpeeds.get(world.getName()));
                        }
                        // Restore
                        int originalRandomTickSpeed = this.dataConfiguration().originalRandomTickSpeeds.get(world.getName());
                        world.setGameRule(GameRule.RANDOM_TICK_SPEED, originalRandomTickSpeed);

                        // Remove from map
                        this.dataConfiguration().originalRandomTickSpeeds.remove(world.getName());
                        this.dataConfiguration().save();
                    }
                }
            }
        }, 1L, 1L);
    }

    @Override
    public void onDisable() {
        // Restore original random tick speeds on disable
        restoreOriginalRandomTickSpeeds();
    }

    private void restoreOriginalRandomTickSpeeds() {
        for (var entry : this.dataConfiguration().originalRandomTickSpeeds.entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            if (world != null) {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, entry.getValue());
            }
        }

        this.dataConfiguration().originalRandomTickSpeeds.clear();
        this.dataConfiguration().save();
    }

    private void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.enableUnstableAPI("help");

        manager.registerCommand(new OptimizationUtilsCommand());
    }

    private void setupMetrics() {
        int pluginId = 26099;
        Metrics metrics = new Metrics(this, pluginId);
    }

    public static OptimizationUtils instance() {
        return instance;
    }

    public PluginConfiguration pluginConfiguration() {
        return pluginConfiguration;
    }

    public DataConfiguration dataConfiguration() {
        return dataConfiguration;
    }

    public void reloadConfiguration() {
        this.pluginConfiguration.load();
        this.dataConfiguration.load();
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
