package io.github.projectunified.blockutil.spigot.common;

import com.lewdev.probabilitylib.ProbabilityCollection;
import io.github.projectunified.blockutil.core.box.BlockBox;
import io.github.projectunified.blockutil.core.box.Position;
import io.github.projectunified.blockutil.core.iterator.PositionIterator;
import org.bukkit.World;

import java.util.List;
import java.util.function.Supplier;

public interface BlockHandler {
    BlockProcess setBlock(World world, PositionIterator iterator, BlockData blockData, boolean urgent);

    BlockProcess setBlock(World world, BlockBox blockBox, BlockData blockData, boolean urgent);

    BlockProcess setBlock(World world, PositionIterator iterator, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent);

    BlockProcess setBlock(World world, BlockBox blockBox, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent);

    BlockProcess setBlock(World world, PositionIterator iterator, Supplier<BlockData> blockDataSupplier, boolean urgent);

    BlockProcess setBlock(World world, BlockBox blockBox, Supplier<BlockData> blockDataSupplier, boolean urgent);

    BlockProcess setBlock(World world, List<Pair<Position, BlockData>> blocks, boolean urgent);

    default BlockProcess clearBlock(World world, PositionIterator iterator, boolean urgent) {
        return setBlock(world, iterator, BlockData.AIR, urgent);
    }

    default BlockProcess clearBlock(World world, BlockBox blockBox, boolean urgent) {
        return setBlock(world, blockBox, BlockData.AIR, urgent);
    }
}
