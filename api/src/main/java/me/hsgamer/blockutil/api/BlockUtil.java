package me.hsgamer.blockutil.api;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.blockutil.abstraction.SimpleBlockHandler;
import me.hsgamer.hscore.common.CachedValue;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class BlockUtil {
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    private static final String[] handlerClassNames = {
            "me.hsgamer.blockutil.fawe.FaweBlockHandler",
            "me.hsgamer.blockutil.we.WeBlockHandler",
            "me.hsgamer.blockutil.folia.FoliaBlockHandler"
    };

    private BlockUtil() {
        // EMPTY
    }

    private static boolean checkClassDependAvailable(Class<?> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("isAvailable");
            if (method.getReturnType().equals(boolean.class) || method.getReturnType().equals(Boolean.class)) {
                return false;
            }
            return (boolean) method.invoke(null);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            return true;
        }
    }

    private static Optional<BlockHandler> getHandler(Plugin plugin, String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
            if (!BlockHandler.class.isAssignableFrom(clazz)) {
                return Optional.empty();
            }
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }

        if (!checkClassDependAvailable(clazz)) {
            return Optional.empty();
        }

        Constructor<?> constructor;
        boolean hasPluginConstructor;
        try {
            constructor = clazz.getConstructor(Plugin.class);
            hasPluginConstructor = true;
        } catch (NoSuchMethodException e) {
            try {
                constructor = clazz.getConstructor();
                hasPluginConstructor = false;
            } catch (NoSuchMethodException ex) {
                return Optional.empty();
            }
        }

        BlockHandler blockHandler;
        try {
            if (hasPluginConstructor) {
                //noinspection unchecked
                blockHandler = (BlockHandler) constructor.newInstance(plugin);
            } else {
                //noinspection unchecked
                blockHandler = (BlockHandler) constructor.newInstance();
            }
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.of(blockHandler);
    }

    public static BlockHandler getHandler(Plugin plugin, boolean lazyLoading) {
        final CachedValue<BlockHandler> cachedValue = CachedValue.of(() -> {
            for (String className : handlerClassNames) {
                Optional<BlockHandler> optionalBlockHandler = getHandler(plugin, className);
                if (optionalBlockHandler.isPresent()) {
                    return optionalBlockHandler.get();
                }
            }
            return SimpleBlockHandler.getDefault(plugin);
        });

        if (!lazyLoading) {
            return cachedValue.get();
        }

        return new BlockHandler() {
            @Override
            public BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
                return cachedValue.get().setRandomBlocks(world, iterator, probabilityCollection);
            }

            @Override
            public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
                return cachedValue.get().setRandomBlocks(world, blockBox, probabilityCollection);
            }

            @Override
            public BlockProcess setRandomBlocks(Collection<Location> locations, ProbabilityCollection<XMaterial> probabilityCollection) {
                return cachedValue.get().setRandomBlocks(locations, probabilityCollection);
            }

            @Override
            public BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
                return cachedValue.get().setBlocks(world, iterator, material);
            }

            @Override
            public BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
                return cachedValue.get().setBlocks(world, blockBox, material);
            }

            @Override
            public BlockProcess setBlocks(Collection<Location> locations, XMaterial material) {
                return cachedValue.get().setBlocks(locations, material);
            }

            @Override
            public void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
                cachedValue.get().setBlocksFast(world, iterator, material);
            }

            @Override
            public void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
                cachedValue.get().setBlocksFast(world, blockBox, material);
            }

            @Override
            public void setBlocksFast(Collection<Location> locations, XMaterial material) {
                cachedValue.get().setBlocksFast(locations, material);
            }

            @Override
            public BlockProcess clearBlocks(World world, PositionIterator iterator) {
                return cachedValue.get().clearBlocks(world, iterator);
            }

            @Override
            public BlockProcess clearBlocks(World world, BlockBox blockBox) {
                return cachedValue.get().clearBlocks(world, blockBox);
            }

            @Override
            public BlockProcess clearBlocks(Collection<Location> locations) {
                return cachedValue.get().clearBlocks(locations);
            }

            @Override
            public void clearBlockFast(World world, PositionIterator iterator) {
                cachedValue.get().clearBlockFast(world, iterator);
            }

            @Override
            public void clearBlocksFast(World world, BlockBox blockBox) {
                cachedValue.get().clearBlocksFast(world, blockBox);
            }

            @Override
            public void clearBlocksFast(Collection<Location> locations) {
                cachedValue.get().clearBlocksFast(locations);
            }

            @Override
            public BlockProcess setBlocks(World world, Map<XMaterial, Collection<Position>> blockMap) {
                return cachedValue.get().setBlocks(world, blockMap);
            }

            @Override
            public void setBlocksFast(World world, Map<XMaterial, Collection<Position>> blockMap) {
                cachedValue.get().setBlocksFast(world, blockMap);
            }
        };
    }

    public static BlockHandler getHandler(Plugin plugin) {
        return getHandler(plugin, true);
    }

    public static boolean isSurrounded(Block block) {
        for (BlockFace face : FACES) {
            Material material = block.getRelative(face).getType();
            if (material.name().contains("AIR") || material.isTransparent()) {
                return false;
            }
        }
        return true;
    }
}
