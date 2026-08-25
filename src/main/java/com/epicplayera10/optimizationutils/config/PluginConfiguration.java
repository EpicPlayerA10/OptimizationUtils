package com.epicplayera10.optimizationutils.config;

import com.epicplayera10.optimizationutils.config.model.FilterMode;
import com.epicplayera10.optimizationutils.config.model.MsptCalculationMode;
import com.epicplayera10.optimizationutils.config.model.TickingDisableMode;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

import java.util.ArrayList;
import java.util.List;

@Header("A config file for the plugin.")
@Header("")
public class PluginConfiguration extends OkaeriConfig {
    @Comment("")
    @Comment("Debug mode for the plugin.")
    public boolean debug = false;

    @Comment("")
    @Comment("The method used to calculate the MSPT for dynamic features.")
    @Comment(" - AVERAGE_5S - Uses the average MSPT over the last 5 seconds.")
    @Comment(" - LAST_TICK - Uses the current MSPT of the last tick.")
    public MsptCalculationMode msptCalculationMode = MsptCalculationMode.AVERAGE_5S;

    @Comment("")
    @Comment("This feature allows the plugin to dynamically adjust the mobcap based on server performance.")
    public DynamicMobcap dynamicMobcap = new DynamicMobcap();

    public static class DynamicMobcap extends OkaeriConfig {
        public boolean enabled = true;

        @Comment("")
        @Comment("The target MSPT in milliseconds when mobcap should be throttled.")
        public float msptThreshold = 35.0f;

        @Comment("")
        @Comment("If dynamic mobcap should also apply to spawners.")
        public boolean throttleSpawners = false;
    }

    @Comment("")
    @Comment("Stops ticking the selected mobs. They stay in the world but are not ticked by the server (no AI, no movement).")
    public DisableEntityTicking disableEntityTicking = new DisableEntityTicking();

    public static class DisableEntityTicking extends OkaeriConfig {
        public boolean enabled = false;

        @Comment("")
        @Comment("How ticking is disabled.")
        @Comment(" - ALL_TICKING - removes the mob from the server's entity tick list, so nothing about it is ticked at all.")
        @Comment("                 Nothing is saved to the mob, everything comes back on its own after a restart.")
        @Comment(" - BUKKIT_AWARE - uses Bukkit's Mob#setAware, so only the mob's AI is skipped (it still falls, burns, despawns, ...).")
        @Comment("                  Beware: this is saved to the mob (Bukkit.Aware), so mobs made unaware by other plugins or commands are restored as aware too.")
        public TickingDisableMode mode = TickingDisableMode.ALL_TICKING;

        @Comment("")
        @Comment("How the list below is interpreted.")
        @Comment(" - INCLUDE - only mobs matching the list are not ticked.")
        @Comment(" - EXCLUDE - every mob except those matching the list is not ticked.")
        public FilterMode filterMode = FilterMode.INCLUDE;

        @Comment("")
        @Comment("Mobs that should not be ticked. Accepted values:")
        @Comment(" - ALL - every mob")
        @Comment(" - type:COW - a concrete entity type (ZOMBIE, COW, VILLAGER, ...)")
        @Comment(" - bukkit_class:Monster - a Bukkit interface from org.bukkit.entity (Mob, Monster, Animals, Raider, ...), case sensitive. May break between versions.")
        @Comment("   See: https://jd.papermc.io/paper/org/bukkit/entity/package-summary.html#class-summary")
        public List<String> entities = new ArrayList<>(List.of("bukkit_class:Animals"));
    }

    @Comment("")
    @Comment("This feature allows the plugin to dynamically turn on or off random tick speed.")
    public DynamicRandomTickSpeed dynamicRandomTickSpeed = new DynamicRandomTickSpeed();

    public static class DynamicRandomTickSpeed extends OkaeriConfig {
        public boolean enabled = false;

        @Comment("")
        @Comment("The target MSPT in milliseconds when random tick speed should be throttled.")
        public float msptThreshold = 45.0f;
    }
}
