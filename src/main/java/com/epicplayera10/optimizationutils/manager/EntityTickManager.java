package com.epicplayera10.optimizationutils.manager;

import com.epicplayera10.optimizationutils.OptimizationUtils;
import com.epicplayera10.optimizationutils.config.PluginConfiguration;
import com.epicplayera10.optimizationutils.config.model.FilterMode;
import com.epicplayera10.optimizationutils.config.model.TickingDisableMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTickList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Stops the configured mobs from being ticked, without removing them from the world.
 */
public final class EntityTickManager {

    private static final Field ENTITY_TICK_LIST_FIELD;

    /**
     * Marks mobs frozen by {@link TickingDisableMode#BUKKIT_AWARE}, so they can be restored later even
     * after a restart or a crash. Not needed for {@link TickingDisableMode#ALL_TICKING}, which changes
     * nothing that is saved to the mob.
     */
    private static NamespacedKey frozenKey = null;

    static {
        try {
            ENTITY_TICK_LIST_FIELD = ServerLevel.class.getDeclaredField("entityTickList");
            ENTITY_TICK_LIST_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to get entityTickList field from ServerLevel", e);
        }
    }

    /**
     * Cache for {@code bukkit_class:} entries, so the lookup does not run for every mob every second.
     */
    private static final Map<String, Class<?>> BUKKIT_CLASSES = new HashMap<>();
    private static final Set<String> WARNED = new HashSet<>();

    private static BukkitTask task = null;
    private static TickingDisableMode runningMode = null;

    private EntityTickManager() {
    }

    public static boolean isRunning() {
        return task != null;
    }

    /**
     * Starts or stops unticking depending on the current configuration.
     */
    public static void sync() {
        PluginConfiguration.DisableEntityTicking config = config();

        // Restart when the mode changed, so the previous mode is undone first
        if (task != null && runningMode != config.mode) {
            disable();
        }

        if (config.enabled) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * Starts unticking the configured mobs. Newly spawned mobs are unticked every second.
     */
    private static void enable() {
        if (task != null) return;

        runningMode = config().mode;
        task = Bukkit.getScheduler().runTaskTimer(OptimizationUtils.instance(), EntityTickManager::freezeAll, 1L, 20L);
    }

    /**
     * Stops unticking and gives every affected mob its ticking back.
     */
    public static void disable() {
        if (task == null) return;

        task.cancel();
        task = null;
        runningMode = null;

        for (World world : Bukkit.getWorlds()) {
            EntityTickList tickList = getTickList(world);

            for (Mob mob : world.getEntitiesByClass(Mob.class)) {
                unfreeze(mob, tickList);
            }
        }
    }

    /**
     * Called when a mob is added to a world. Freezes it right away when the feature is on,
     * or restores it when it was left frozen by a previous run.
     */
    public static void onMobAddedToWorld(Mob mob) {
        if (isRunning()) {
            if (matches(mob, config())) freeze(mob);
        } else if (isMarkedFrozen(mob)) {
            // Heal mobs left frozen by a crash or a config change
            unfreezeAware(mob);
        }
    }

    /**
     * Stops ticking every matching mob. Returns how many mobs were affected.
     */
    public static int freezeAll() {
        PluginConfiguration.DisableEntityTicking config = config();
        int count = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Mob mob : world.getEntitiesByClass(Mob.class)) {
                if (!matches(mob, config)) continue;

                freeze(mob);
                count++;
            }
        }

        return count;
    }

    private static void freeze(Mob mob) {
        if (runningMode == TickingDisableMode.BUKKIT_AWARE) {
            if (isMarkedFrozen(mob)) return;

            mob.setAware(false);
            mob.getPersistentDataContainer().set(frozenKey(), PersistentDataType.BYTE, (byte) 1);
        } else {
            // Clear a marker left behind by a previous BUKKIT_AWARE run
            unfreezeAware(mob);

            getTickList(mob.getWorld()).remove(ReflectionUtils.getNMSEntity(mob));
        }
    }

    private static void unfreeze(Mob mob, EntityTickList tickList) {
        unfreezeAware(mob);

        if (mob.getChunk().getLoadLevel() == Chunk.LoadLevel.ENTITY_TICKING) {
            // Adding a mob that is already in the list is a no-op
            tickList.add(ReflectionUtils.getNMSEntity(mob));
        }
    }

    /**
     * Makes the mob aware again, but only when we are the ones who made it unaware.
     */
    private static void unfreezeAware(Mob mob) {
        if (!isMarkedFrozen(mob)) return;

        mob.setAware(true);
        mob.getPersistentDataContainer().remove(frozenKey());
    }

    private static boolean isMarkedFrozen(Mob mob) {
        return mob.getPersistentDataContainer().has(frozenKey(), PersistentDataType.BYTE);
    }

    private static boolean matches(Mob mob, PluginConfiguration.DisableEntityTicking config) {
        boolean listed = false;
        for (String entry : config.entities) {
            if (matchesEntry(mob, entry.trim())) {
                listed = true;
                break;
            }
        }

        return config.filterMode == FilterMode.INCLUDE ? listed : !listed;
    }

    /**
     * Matches a single config entry: {@code ALL}, {@code type:COW} or {@code bukkit_class:Mob}.
     */
    private static boolean matchesEntry(Mob mob, String entry) {
        if (entry.equalsIgnoreCase("ALL")) return true;

        int separator = entry.indexOf(':');
        if (separator < 0) {
            warnOnce("Ignoring \"" + entry + "\" in disableEntityTicking.entities, it has no type prefix (ALL, type:, bukkit_class:)");
            return false;
        }

        String type = entry.substring(0, separator).trim();
        String value = entry.substring(separator + 1).trim();

        return switch (type.toLowerCase(Locale.ROOT)) {
            case "type" -> value.equalsIgnoreCase(mob.getType().name());
            case "bukkit_class" -> bukkitClass(value).isInstance(mob);
            default -> {
                warnOnce("Unknown type prefix \"" + type + "\" in disableEntityTicking.entities");
                yield false;
            }
        };
    }

    /**
     * Resolves a class from {@code org.bukkit.entity} by its simple name (case sensitive, like {@code Mob} or {@code Monster}).
     * Returns {@link Void} when it does not exist, so it never matches a mob.
     */
    private static Class<?> bukkitClass(String name) {
        return BUKKIT_CLASSES.computeIfAbsent(name, key -> {
            try {
                return Class.forName("org.bukkit.entity." + key);
            } catch (ClassNotFoundException e) {
                warnOnce("Unknown bukkit class \"" + key + "\" in disableEntityTicking.entities");
                return Void.class;
            }
        });
    }

    private static void warnOnce(String message) {
        if (WARNED.add(message)) {
            OptimizationUtils.instance().getLogger().warning(message);
        }
    }

    private static EntityTickList getTickList(World world) {
        try {
            return (EntityTickList) ENTITY_TICK_LIST_FIELD.get(ReflectionUtils.getNMSWorld(world));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static NamespacedKey frozenKey() {
        if (frozenKey == null) {
            frozenKey = new NamespacedKey(OptimizationUtils.instance(), "frozen");
        }

        return frozenKey;
    }

    private static PluginConfiguration.DisableEntityTicking config() {
        return OptimizationUtils.instance().pluginConfiguration().disableEntityTicking;
    }
}
