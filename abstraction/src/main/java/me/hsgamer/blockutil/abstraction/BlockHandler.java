package me.hsgamer.blockutil.abstraction;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.hscore.bukkit.block.box.BlockBox;
import me.hsgamer.hscore.bukkit.block.iterator.VectorIterator;
import me.hsgamer.hscore.bukkit.block.iterator.impl.LinearVectorIterator;
import me.hsgamer.hscore.common.Pair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Iterator;

public interface BlockHandler {
    static Pair<World, VectorIterator> iterator(Collection<Location> locations) {
        World world = locations.stream().findAny().map(Location::getWorld).orElse(null);
        VectorIterator wrappedVectorIterator = new VectorIterator() {
            Iterator<Vector> vectorIterator = locations.stream().map(Location::toVector).iterator();

            @Override
            public void reset() {
                vectorIterator = locations.stream().map(Location::toVector).iterator();
            }

            @Override
            public boolean hasNext() {
                return vectorIterator.hasNext();
            }

            @Override
            public Vector next() {
                return vectorIterator.next();
            }
        };
        return Pair.of(world, wrappedVectorIterator);
    }

    BlockProcess setRandomBlocks(World world, VectorIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection);

    default BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setRandomBlocks(world, new LinearVectorIterator(blockBox), probabilityCollection);
    }

    default BlockProcess setRandomBlocks(Collection<Location> locations, ProbabilityCollection<XMaterial> probabilityCollection) {
        Pair<World, VectorIterator> pair = iterator(locations);
        return setRandomBlocks(pair.getKey(), pair.getValue(), probabilityCollection);
    }

    BlockProcess clearBlocks(World world, VectorIterator iterator);

    default BlockProcess clearBlocks(World world, BlockBox blockBox) {
        return clearBlocks(world, new LinearVectorIterator(blockBox));
    }

    default BlockProcess clearBlocks(Collection<Location> locations) {
        Pair<World, VectorIterator> pair = iterator(locations);
        return clearBlocks(pair.getKey(), pair.getValue());
    }

    void clearBlockFast(World world, VectorIterator iterator);

    default void clearBlocksFast(World world, BlockBox blockBox) {
        clearBlockFast(world, new LinearVectorIterator(blockBox));
    }

    default void clearBlocksFast(Collection<Location> locations) {
        Pair<World, VectorIterator> pair = iterator(locations);
        clearBlockFast(pair.getKey(), pair.getValue());
    }
}
