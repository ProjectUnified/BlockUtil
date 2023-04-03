package me.hsgamer.blockutil.abstraction;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.function.Supplier;

public interface SimpleBlockHandler extends BlockHandler {
    BlockProcess setBlocks(World world, PositionIterator iterator, Supplier<XMaterial> materialSupplier);

    @Override
    default BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setBlocks(world, iterator, probabilityCollection::get);
    }

    @Override
    default BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setRandomBlocks(world, BlockHandler.iterator(blockBox), probabilityCollection);
    }

    @Override
    default BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
        return setBlocks(world, iterator, () -> material);
    }

    @Override
    default BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
        return setBlocks(world, BlockHandler.iterator(blockBox), material);
    }

    @Override
    default void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
        while (iterator.hasNext()) {
            Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
            XBlock.setType(block, material, false);
        }
    }

    @Override
    default void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
        setBlocksFast(world, BlockHandler.iterator(blockBox), material);
    }
}
