package me.hsgamer.blockutil.nms.v1_12_r1;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NMSBlockHandler implements BlockHandler {
    @Override
    public void setBlock(Block block, Material material, byte data, boolean applyPhysics, boolean doPlace) {
        net.minecraft.server.v1_12_R1.World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        Chunk chunk = ((CraftChunk) block.getChunk()).getHandle();
        int combined = material.getId() + (data << 12);
        IBlockData blockData = net.minecraft.server.v1_12_R1.Block.getByCombinedId(combined);
        if (applyPhysics) {
            world.setTypeAndData(position, blockData, 3);
        } else {
            world.setTypeAndData(position, blockData, 2);
        }
        chunk.a(position, blockData);
    }

    @Override
    public void updateLight(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        World world = ((CraftWorld) block.getWorld()).getHandle();
        world.c(EnumSkyBlock.BLOCK, position);
    }

    @Override
    public void sendChunkUpdate(Player player, org.bukkit.Chunk chunk) {
        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
