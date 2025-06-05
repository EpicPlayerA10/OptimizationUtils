package com.epicplayera10.optimizationutils;

import co.aikar.commands.PaperCommandManager;
import com.epicplayera10.optimizationutils.commands.OptimizationUtilsCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class OptimizationUtils extends JavaPlugin {

    private static OptimizationUtils instance;

    @Override
    public void onEnable() {
        instance = this;

        setupMetrics();

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
}
