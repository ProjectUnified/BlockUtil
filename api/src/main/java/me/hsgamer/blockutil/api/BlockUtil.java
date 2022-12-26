package me.hsgamer.blockutil.api;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockUtilSetting;
import me.hsgamer.blockutil.fawe.FaweBlockHandler;
import me.hsgamer.blockutil.vanilla.VanillaBlockHandler;
import me.hsgamer.blockutil.we.WeBlockHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class BlockUtil {
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    private static final List<BlockHandlerChecker> CHECKERS = new ArrayList<>();

    static {
        CHECKERS.add(new BlockHandlerChecker(
                plugin -> XMaterial.supports(16) && Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit") && BlockUtilSetting.USE_FAWE.get(),
                FaweBlockHandler::new
        ));
        CHECKERS.add(new BlockHandlerChecker(
                plugin -> XMaterial.supports(13) && Bukkit.getPluginManager().isPluginEnabled("WorldEdit") && BlockUtilSetting.USE_WE.get(),
                WeBlockHandler::new
        ));
    }

    private BlockUtil() {
        // EMPTY
    }

    public static BlockHandler getHandler(Plugin plugin) {
        for (BlockHandlerChecker checker : CHECKERS) {
            if (checker.predicate.test(plugin)) {
                return checker.supplier.apply(plugin);
            }
        }
        return new VanillaBlockHandler(plugin);
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

    private static class BlockHandlerChecker {
        private final Predicate<Plugin> predicate;
        private final Function<Plugin, BlockHandler> supplier;

        private BlockHandlerChecker(Predicate<Plugin> predicate, Function<Plugin, BlockHandler> supplier) {
            this.predicate = predicate;
            this.supplier = supplier;
        }
    }
}
