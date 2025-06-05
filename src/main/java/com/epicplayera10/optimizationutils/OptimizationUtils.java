package com.epicplayera10.optimizationutils;

import co.aikar.commands.PaperCommandManager;
import com.epicplayera10.optimizationutils.commands.OptimizationUtilsCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class OptimizationUtils extends JavaPlugin {

    private static OptimizationUtils instance;

    @Override
    public void onEnable() {
        instance = this;

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

    public static OptimizationUtils instance() {
        return instance;
    }
}
