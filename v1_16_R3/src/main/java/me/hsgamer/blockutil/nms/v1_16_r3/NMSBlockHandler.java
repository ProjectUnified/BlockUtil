package me.hsgamer.blockutil.nms.v1_16_r3;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class NMSBlockHandler implements BlockHandler {
    private final IBlockData air = ((CraftBlockData) Bukkit.createBlockData(Material.AIR)).getState();

    @Override
    public void setBlock(Block block, Material material, byte data, boolean applyPhysics) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        Chunk chunk = ((CraftChunk) block.getChunk()).getHandle();
        net.minecraft.server.v1_16_R3.Block nmsBlock = CraftMagicNumbers.getBlock(material);
        IBlockData blockData = nmsBlock.getBlockData();
        chunk.setType(position, air, applyPhysics);
        chunk.setType(position, blockData, applyPhysics);
    }

    @Override
    public void updateLight(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        World world = ((CraftWorld) block.getWorld()).getHandle();
        world.getChunkProvider().getLightEngine().a(position);
    }

    @Override
    public void sendChunkUpdate(Player player, org.bukkit.Chunk chunk) {
        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
