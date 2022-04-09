package me.hsgamer.blockutil.nms.v1_8_r3;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class NMSBlockHandler implements BlockHandler {
    @Override
    public void setBlock(Block block, Material material, byte data, boolean applyPhysics, boolean doPlace) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        Chunk chunk = ((CraftChunk) block.getChunk()).getHandle();
        chunk.tileEntities.remove(position);
        int combined = material.getId() + (data << 12);
        IBlockData blockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);
        chunk.a(position, blockData);
        if (applyPhysics) {
            net.minecraft.server.v1_8_R3.Block nmsBlock = chunk.getType(position);
            world.update(position, nmsBlock);
        }
    }

    @Override
    public void updateLight(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        World world = ((CraftWorld) block.getWorld()).getHandle();
        world.x(position);
    }

    @Override
    public void updateLight(org.bukkit.Chunk chunk) {
        Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        nmsChunk.initLighting();
    }

    @Override
    public void sendChunkUpdate(Player player, org.bukkit.Chunk chunk) {
        ((CraftPlayer) player).getHandle().chunkCoordIntPairQueue.add(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
    }
}
