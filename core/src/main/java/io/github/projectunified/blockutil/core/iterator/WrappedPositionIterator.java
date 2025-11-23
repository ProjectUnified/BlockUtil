package io.github.projectunified.blockutil.core.iterator;

import io.github.projectunified.blockutil.core.box.Position;

import java.util.Iterator;

/**
 * The {@link PositionIterator} for any type
 *
 * @param <T> the type
 */
public abstract class WrappedPositionIterator<T> implements Iterator<T> {
    private final PositionIterator positionIterator;

    /**
     * Create a new iterator
     *
     * @param positionIterator the position iterator
     */
    protected WrappedPositionIterator(PositionIterator positionIterator) {
        this.positionIterator = positionIterator;
    }

    /**
     * Convert the position to the type
     *
     * @param position the position
     * @return the type
     */
    protected abstract T convert(Position position);

    @Override
    public boolean hasNext() {
        return positionIterator.hasNext();
    }

    @Override
    public T next() {
        return convert(positionIterator.next());
    }

    /**
     * Reset the iterator
     */
    public void reset() {
        positionIterator.reset();
    }
}
