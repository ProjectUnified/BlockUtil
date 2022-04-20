package me.hsgamer.blockutil.abstraction;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockHandler {
    void setBlock(Block block, Material material, byte data, boolean applyPhysics, boolean doPlace);

    void updateLight(Block block);

    void sendChunkUpdate(Player player, Chunk chunk);

    default void setBlockData(Block block, Object blockData, boolean applyPhysics, boolean doPlace) throws IllegalArgumentException {
        try {
            if (!Class.forName("org.bukkit.block.data.BlockData").isAssignableFrom(blockData.getClass())) {
                throw new IllegalArgumentException("The data is not a valid BlockData");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("The data is not a valid BlockData");
        }
    }

    default void setBlock(Block block, Material material, byte data, boolean applyPhysics) {
        setBlock(block, material, data, applyPhysics, true);
    }

    default void setBlock(Block block, Material material, byte data) {
        setBlock(block, material, data, true);
    }

    default void setBlock(Block block, Material material) {
        setBlock(block, material, (byte) 0);
    }

    default void setBlock(Block block, Material material, boolean applyPhysics) {
        setBlock(block, material, (byte) 0, applyPhysics);
    }
}
