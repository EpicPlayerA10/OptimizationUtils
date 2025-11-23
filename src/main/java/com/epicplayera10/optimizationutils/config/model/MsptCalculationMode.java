package com.epicplayera10.optimizationutils.config.model;

public enum MsptCalculationMode {
    /**
     * Uses the average MSPT over the last 5 seconds.
     */
    AVERAGE_5S,
    /**
     * Uses the current MSPT of the last tick.
     */
    LAST_TICK
}
