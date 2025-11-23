package io.github.projectunified.blockutil.iterator;

import io.github.projectunified.blockutil.core.box.BlockBox;
import io.github.projectunified.blockutil.core.box.Position;
import io.github.projectunified.blockutil.core.iterator.AbstractPositionIterator;
import io.github.projectunified.blockutil.core.iterator.PositionIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * The {@link PositionIterator} that combines multiple {@link PositionIterator} and randomly choose one
 */
public class RandomTypePositionIterator extends AbstractPositionIterator {
    private final List<Function<BlockBox, PositionIterator>> functions;
    private final AtomicReference<PositionIterator> current;
    private final Random random = new Random();

    /**
     * Create a new {@link RandomTypePositionIterator}
     *
     * @param box       the {@link BlockBox}
     * @param functions the functions to create {@link PositionIterator}
     */
    public RandomTypePositionIterator(BlockBox box, Collection<Function<BlockBox, PositionIterator>> functions) {
        super(box);
        this.functions = new ArrayList<>(functions);
        current = new AtomicReference<>(getRandom());
    }

    private PositionIterator getRandom() {
        if (functions.isEmpty()) {
            throw new IllegalStateException("No functions defined");
        }
        Function<BlockBox, PositionIterator> function = functions.get(random.nextInt(functions.size()));
        return function.apply(box);
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
    public Position next() {
        return current.get().next();
    }
}
