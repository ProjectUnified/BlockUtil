package me.hsgamer.blockutil.folia;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class FoliaBlockHandler implements BlockHandler {
    private final Plugin plugin;

    public FoliaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        return null;
    }

    @Override
    public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        return null;
    }

    @Override
    public BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
        return null;
    }

    @Override
    public BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
        return null;
    }

    @Override
    public void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
        while (iterator.hasNext()) {
            Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
            XBlock.setType(block, material, false);
        }
    }

    @Override
    public void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
        setBlocksFast(world, BlockHandler.iterator(blockBox), material);
    }
}
