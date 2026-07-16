package com.epicplayera10.optimizationutils.manager;

import io.papermc.paper.configuration.WorldConfiguration;
import io.papermc.paper.configuration.type.DespawnRange;
import io.papermc.paper.configuration.type.number.IntOr;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.bukkit.World;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.OptionalInt;

public class NMSUtils {

    /**
     * Sets the simulation distance for the given world and updates related configurations.
     */
    public static void setNMSSimulationDistance(World world, int newSimulationDistance) {
        // Set other values
        // All these calculations come from: https://paper-chan.moe/paper-optimization/?ref=paper-chan.moe#despawn-ranges-notes
        int configBasedSimulationDistance = Math.min(newSimulationDistance, 9);
        ServerLevel serverLevel = ReflectionUtils.getNMSWorld(world);
        // Set mob spawn range
        serverLevel.spigotConfig.mobSpawnRange = (byte) Math.max(3, Math.min(8, configBasedSimulationDistance - 1));

        // Set monster despawn range
        WorldConfiguration.Entities.Spawning.DespawnRangePair oldDespawnRangePair = serverLevel.paperConfig().entities.spawning.despawnRanges.get(MobCategory.MONSTER);

        IntOr.Default horizontalLimit = new IntOr.Default(OptionalInt.of((configBasedSimulationDistance - 1) * 16));
        IntOr.Default verticalLimit = ReflectionUtils.getDespawnRangesVerticalLimit(oldDespawnRangePair.hard());
        serverLevel.paperConfig().entities.spawning.despawnRanges.replace(
            MobCategory.MONSTER,
            new WorldConfiguration.Entities.Spawning.DespawnRangePair(new DespawnRange(horizontalLimit, verticalLimit, true), oldDespawnRangePair.soft())
        );
        try {
            serverLevel.paperConfig().entities.spawning.precomputeDespawnDistances();
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setNMSVillagerSensorTickRate(World world, int ticks) {
        ServerLevel serverLevel = ReflectionUtils.getNMSWorld(world);
        serverLevel.paperConfig().tickRates.sensor.put(EntityType.VILLAGER, "secondarypoisensor", ticks);
    }

    public static void setNMSVillagerBehaviorTickRate(World world, int ticks) {
        ServerLevel serverLevel = ReflectionUtils.getNMSWorld(world);
        serverLevel.paperConfig().tickRates.sensor.put(EntityType.VILLAGER, "validatenearbypoi", ticks);
    }

    /**
     * Returns the villager sensor tick rate for the given world, or null if not set.
     */
    public static Integer getNMSVillagerSensorTickRate(World world) {
        ServerLevel serverLevel = ReflectionUtils.getNMSWorld(world);
        return serverLevel.paperConfig().tickRates.sensor.get(EntityType.VILLAGER, "secondarypoisensor");
    }

    /**
     * Returns the villager behavior tick rate for the given world, or null if not set.
     */
    public static Integer getNMSVillagerBehaviorTickRate(World world) {
        ServerLevel serverLevel = ReflectionUtils.getNMSWorld(world);
        return serverLevel.paperConfig().tickRates.sensor.get(EntityType.VILLAGER, "validatenearbypoi");
    }
}
