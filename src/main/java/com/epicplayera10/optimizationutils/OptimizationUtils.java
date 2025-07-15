package com.epicplayera10.optimizationutils;

import co.aikar.commands.PaperCommandManager;
import com.epicplayera10.optimizationutils.commands.OptimizationUtilsCommand;
import com.epicplayera10.optimizationutils.config.ConfigurationFactory;
import com.epicplayera10.optimizationutils.config.PluginConfiguration;
import com.epicplayera10.optimizationutils.listeners.EntityListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class OptimizationUtils extends JavaPlugin {

    private PluginConfiguration pluginConfiguration;

    private static OptimizationUtils instance;

    @Override
    public void onEnable() {
        instance = this;

        setupMetrics();

        this.pluginConfiguration = ConfigurationFactory.createPluginConfiguration(new File(this.getDataFolder(), "config.yml"));

        Bukkit.getPluginManager().registerEvents(new EntityListener(), this);

        // Plugin startup logic
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

    public void reloadConfiguration() {
        this.pluginConfiguration.load();
    }
}
