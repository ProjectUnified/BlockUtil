package me.hsgamer.blockutil.api;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.blockutil.abstraction.SimpleBlockHandler;
import me.hsgamer.blockutil.fawe.FaweBlockHandler;
import me.hsgamer.blockutil.folia.FoliaBlockHandler;
import me.hsgamer.blockutil.we.WeBlockHandler;
import me.hsgamer.hscore.common.CachedValue;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Map;

public final class BlockUtil {
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    private BlockUtil() {
        // EMPTY
    }

    public static BlockHandler getHandler(Plugin plugin, boolean lazyLoading) {
        final CachedValue<BlockHandler> cachedValue = CachedValue.of(() -> {
            if (XMaterial.supports(16) && Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null && BlockHandlerSettings.USE_FAWE.get()) {
                return new FaweBlockHandler();
            }

            if (XMaterial.supports(13) && Bukkit.getPluginManager().getPlugin("WorldEdit") != null && BlockHandlerSettings.USE_WE.get()) {
                return new WeBlockHandler(plugin);
            }

            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                return new FoliaBlockHandler(plugin);
            } catch (Exception ignored) {
                // EMPTY
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
