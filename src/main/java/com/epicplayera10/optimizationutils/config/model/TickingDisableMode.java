package com.epicplayera10.optimizationutils.config.model;

public enum TickingDisableMode {
    /**
     * Removes the mob from the server's entity tick list, so nothing about it is ticked at all.
     */
    ALL_TICKING,
    /**
     * Uses Bukkit's {@code Mob#setAware(boolean)}, so only the mob's AI is skipped.
     */
    BUKKIT_AWARE
}
