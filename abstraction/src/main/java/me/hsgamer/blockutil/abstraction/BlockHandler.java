package me.hsgamer.blockutil.abstraction;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.Iterator;

public interface BlockHandler {
    static Pair<World, PositionIterator> iterator(Collection<Location> locations) {
        World world = locations.stream().findAny().map(Location::getWorld).orElse(null);
        PositionIterator wrappedPositionIterator = new PositionIterator() {
            Iterator<Position> positionIterator = locations.stream().map(BukkitBlockAdapter::adapt).iterator();

            @Override
            public void reset() {
                positionIterator = locations.stream().map(BukkitBlockAdapter::adapt).iterator();
            }

            @Override
            public boolean hasNext() {
                return positionIterator.hasNext();
            }

            @Override
            public Position next() {
                return positionIterator.next();
            }
        };
        return Pair.of(world, wrappedPositionIterator);
    }

    BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection);

    BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection);

    default BlockProcess setRandomBlocks(Collection<Location> locations, ProbabilityCollection<XMaterial> probabilityCollection) {
        Pair<World, PositionIterator> pair = iterator(locations);
        return setRandomBlocks(pair.getKey(), pair.getValue(), probabilityCollection);
    }

    BlockProcess clearBlocks(World world, PositionIterator iterator);

    BlockProcess clearBlocks(World world, BlockBox blockBox);

    default BlockProcess clearBlocks(Collection<Location> locations) {
        Pair<World, PositionIterator> pair = iterator(locations);
        return clearBlocks(pair.getKey(), pair.getValue());
    }

    void clearBlockFast(World world, PositionIterator iterator);

    void clearBlocksFast(World world, BlockBox blockBox);

    default void clearBlocksFast(Collection<Location> locations) {
        Pair<World, PositionIterator> pair = iterator(locations);
        clearBlockFast(pair.getKey(), pair.getValue());
    }
}
