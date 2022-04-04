package me.hsgamer.blockutil.abstraction;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockHandler {
    void setBlock(Block block, Material material, byte data, boolean applyPhysics);

    void updateLight(Block block);

    void sendChunkUpdate(Player player, Chunk chunk);

    default void setBlock(Block block, Material material, byte data) {
        setBlock(block, material, data, true);
    }

    default void setBlock(Block block, Material material, boolean applyPhysics) {
        setBlock(block, material, (byte) 0, applyPhysics);
    }

    default void setBlock(Block block, Material material) {
        setBlock(block, material, true);
    }

    default void setChunkUpdate(Chunk chunk) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(chunk.getWorld())) {
                sendChunkUpdate(player, chunk);
            }
        }
    }
}
