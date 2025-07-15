package com.epicplayera10.optimizationutils.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

@Header("A config file for the plugin.")
@Header("")
public class PluginConfiguration extends OkaeriConfig {
    @Comment("This feature allows the plugin to dynamically adjust the mobcap based on server performance.")
    public DynamicMobcap dynamicMobcap = new DynamicMobcap();

    public static class DynamicMobcap extends OkaeriConfig {
        public boolean enabled = true;

        @Comment("The target margin in milliseconds for the server's average ticks per second (TPS).")
        public float targetMsptMargin = 5.0f;
    }
}
