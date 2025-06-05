package com.epicplayera10.optimizationutils.manager;

import io.papermc.paper.configuration.type.DespawnRange;
import io.papermc.paper.configuration.type.number.IntOr;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    private static Method craftWorldGetHandleMethod = null;
    private static final Field despawnRangeHorizontalLimit;
    private static final Field despawnRangeVerticalLimit;

    static {
        try {
            despawnRangeHorizontalLimit = DespawnRange.class.getDeclaredField("horizontalLimit");
            despawnRangeHorizontalLimit.setAccessible(true);

            despawnRangeVerticalLimit = DespawnRange.class.getDeclaredField("verticalLimit");
            despawnRangeVerticalLimit.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerLevel getNMSWorld(World world) {
        if (craftWorldGetHandleMethod == null) {
            try {
                craftWorldGetHandleMethod = world.getClass().getDeclaredMethod("getHandle");
                craftWorldGetHandleMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to get getHandle method from CraftWorld", e);
            }
        }

        try {
            return (ServerLevel) craftWorldGetHandleMethod.invoke(world);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static IntOr.Default getDespawnRangesHorizontalLimit(DespawnRange despawnRange) {
        try {
            return (IntOr.Default) despawnRangeHorizontalLimit.get(despawnRange);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static IntOr.Default getDespawnRangesVerticalLimit(DespawnRange despawnRange) {
        try {
            return (IntOr.Default) despawnRangeVerticalLimit.get(despawnRange);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String cbClass(String clazz) {
        return CRAFTBUKKIT_PACKAGE + "." + clazz;
    }

}
