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

        @Comment("The target MSPT in milliseconds when mobcap should be throttled.")
        public float msptThreshold = 35.0f;

        @Comment("If dynamic mobcap should also apply to spawners.")
        public boolean throttleSpawners = false;
    }

    @Comment("This feature allows the plugin to dynamically turn on or off random tick speed.")
    public DynamicRandomTickSpeed dynamicRandomTickSpeed = new DynamicRandomTickSpeed();

    public static class DynamicRandomTickSpeed extends OkaeriConfig {
        public boolean enabled = true;

        @Comment("The target MSPT in milliseconds when random tick speed should be throttled.")
        public float msptThreshold = 35.0f;
    }
}
