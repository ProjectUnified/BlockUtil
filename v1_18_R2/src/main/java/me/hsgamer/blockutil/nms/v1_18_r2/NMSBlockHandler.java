package me.hsgamer.blockutil.nms.v1_18_r2;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class NMSBlockHandler implements BlockHandler {
    private static final IBlockData air = ((CraftBlockData) Bukkit.createBlockData(Material.AIR)).getState();

    public static void setBlock(Block block, IBlockData blockData, boolean applyPhysics, boolean doPlace) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        Chunk chunk = ((CraftChunk) block.getChunk()).getHandle();
        chunk.setBlockState(position, air, false, false);
        chunk.setBlockState(position, blockData, applyPhysics, doPlace);
    }

    @Override
    public void setBlockData(Block block, Object blockData, boolean applyPhysics, boolean doPlace) throws IllegalArgumentException {
        BlockHandler.super.setBlockData(block, blockData, applyPhysics, doPlace);
        IBlockData nmsBlockData = ((CraftBlockData) blockData).getState();
        setBlock(block, nmsBlockData, applyPhysics, doPlace);
    }

    @Override
    public void setBlock(Block block, Material material, byte data, boolean applyPhysics, boolean doPlace) {
        net.minecraft.world.level.block.Block nmsBlock = CraftMagicNumbers.getBlock(material);
        IBlockData blockData = nmsBlock.n();
        setBlock(block, blockData, applyPhysics, doPlace);
    }

    @Override
    public void updateLight(Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        World world = ((CraftWorld) block.getWorld()).getHandle();
        world.K().n().a(position);
    }

    @Override
    public void sendChunkUpdate(Player player, org.bukkit.Chunk chunk) {
        Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nmsChunk, nmsChunk.q.l_(), null, null, true);
        ((CraftPlayer) player).getHandle().b.a(packet);
    }
}
