package me.hsgamer.blockutil.extra.iterator.impl;

import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.blockutil.extra.iterator.api.AbstractBlockIterator;
import me.hsgamer.blockutil.extra.iterator.api.BlockIterator;
import me.hsgamer.hscore.common.CollectionUtils;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RandomTypeBlockIterator extends AbstractBlockIterator {
    private final Collection<Function<BlockBox, BlockIterator>> functions;
    private final AtomicReference<BlockIterator> current;

    public RandomTypeBlockIterator(BlockBox box, Collection<Function<BlockBox, BlockIterator>> functions) {
        super(box);
        this.functions = functions;
        current = new AtomicReference<>(getRandom());
    }

    private BlockIterator getRandom() {
        return Objects.requireNonNull(CollectionUtils.pickRandom(functions)).apply(box);
    }

    @Override
    public void reset() {
        current.set(getRandom());
    }

    @Override
    public boolean hasNext() {
        return current.get().hasNext();
    }

    @Override
    public Vector next() {
        return current.get().next();
    }
}
