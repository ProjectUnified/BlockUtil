package io.github.projectunified.blockutil.core.iterator;

import io.github.projectunified.blockutil.core.box.Position;

import java.util.Iterator;

/**
 * The {@link Iterator} for {@link Position}
 */
public interface PositionIterator extends Iterator<Position> {
    /**
     * Reset the iterator
     */
    void reset();
}
