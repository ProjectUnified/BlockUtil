package io.github.projectunified.blockutil.simple;

import com.lewdev.probabilitylib.ProbabilityCollection;
import io.github.projectunified.blockutil.api.BlockData;
import io.github.projectunified.blockutil.api.BlockHandler;
import io.github.projectunified.blockutil.api.BlockProcess;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.BasePositionIterator;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.World;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public interface SimpleBlockHandler extends BlockHandler {
    static PositionIterator iterator(BlockBox blockBox) {
        return new BasePositionIterator(blockBox) {
            @Override
            public Position initial() {
                return new Position(blockBox.minX, blockBox.minY, blockBox.minZ);
            }

            @Override
            public Position getContinue(Position current) throws NoSuchElementException {
                if (current.x < blockBox.maxX) {
                    return new Position(current.x + 1, current.y, current.z);
                } else if (current.y < blockBox.maxY) {
                    return new Position(blockBox.minX, current.y + 1, current.z);
                } else if (current.z < blockBox.maxZ) {
                    return new Position(blockBox.minX, blockBox.minY, current.z + 1);
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public boolean hasContinue(Position current) {
                return current.x < blockBox.maxX || current.y < blockBox.maxY || current.z < blockBox.maxZ;
            }
        };
    }

    @Override
    default BlockProcess setBlock(World world, PositionIterator iterator, BlockData blockData, boolean urgent) {
        return setBlock(world, iterator, () -> blockData, urgent);
    }

    @Override
    default BlockProcess setBlock(World world, BlockBox blockBox, BlockData blockData, boolean urgent) {
        return setBlock(world, iterator(blockBox), () -> blockData, urgent);
    }

    @Override
    default BlockProcess setBlock(World world, PositionIterator iterator, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent) {
        return setBlock(world, iterator, probabilityCollection::get, urgent);
    }

    @Override
    default BlockProcess setBlock(World world, BlockBox blockBox, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent) {
        return setBlock(world, iterator(blockBox), probabilityCollection::get, urgent);
    }

    @Override
    default BlockProcess setBlock(World world, BlockBox blockBox, Supplier<BlockData> blockDataSupplier, boolean urgent) {
        return setBlock(world, iterator(blockBox), blockDataSupplier, urgent);
    }
}
