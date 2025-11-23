package io.github.projectunified.blockutil.core.iterator;

import io.github.projectunified.blockutil.core.box.BlockBox;

/**
 * The abstract {@link PositionIterator} for {@link BlockBox}
 */
public abstract class AbstractPositionIterator implements PositionIterator {
    /**
     * The box
     */
    public final BlockBox box;

    /**
     * Create a new {@link AbstractPositionIterator}
     *
     * @param box the box
     */
    protected AbstractPositionIterator(BlockBox box) {
        this.box = box;
    }
}
