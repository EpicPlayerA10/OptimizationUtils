package com.epicplayera10.optimizationutils.manager;

import io.papermc.paper.configuration.WorldConfiguration;
import io.papermc.paper.configuration.type.DespawnRange;
import io.papermc.paper.configuration.type.number.IntOr;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import org.bukkit.World;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.OptionalInt;

public class SimulationDistanceManager {

    /**
     * Sets the simulation distance for the given world and updates related configurations.
     */
    public static void setSimulationDistance(World world, int newSimulationDistance) {
        world.setSimulationDistance(newSimulationDistance);

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
}
