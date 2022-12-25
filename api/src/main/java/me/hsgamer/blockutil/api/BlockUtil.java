package me.hsgamer.blockutil.api;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.fawe.FaweBlockHandler;
import me.hsgamer.blockutil.vanilla.VanillaBlockHandler;
import me.hsgamer.blockutil.we.WeBlockHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;

public final class BlockUtil {
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    private BlockUtil() {
        // EMPTY
    }

    public static BlockHandler getHandler(Plugin plugin) {
        if (XMaterial.supports(16) && Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            return new FaweBlockHandler(plugin);
        } else if (XMaterial.supports(13) && Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            return new WeBlockHandler(plugin);
        } else {
            return new VanillaBlockHandler(plugin);
        }
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
