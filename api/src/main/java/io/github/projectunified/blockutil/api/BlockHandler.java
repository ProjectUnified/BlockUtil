package io.github.projectunified.blockutil.api;

import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.World;

import java.util.List;

public interface BlockHandler {
    BlockProcess setBlock(World world, PositionIterator iterator, BlockData blockData, boolean urgent);

    BlockProcess setBlock(World world, BlockBox blockBox, BlockData blockData, boolean urgent);

    BlockProcess setBlock(World world, PositionIterator iterator, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent);

    BlockProcess setBlock(World world, BlockBox blockBox, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent);

    BlockProcess setBlock(World world, List<Pair<Position, BlockData>> blocks, boolean urgent);

    default BlockProcess clearBlock(World world, PositionIterator iterator, boolean urgent) {
        return setBlock(world, iterator, BlockData.AIR, urgent);
    }

    default BlockProcess clearBlock(World world, BlockBox blockBox, boolean urgent) {
        return setBlock(world, blockBox, BlockData.AIR, urgent);
    }
}
