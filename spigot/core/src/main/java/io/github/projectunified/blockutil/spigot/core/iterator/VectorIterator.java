package io.github.projectunified.blockutil.spigot.core.iterator;

import io.github.projectunified.blockutil.core.box.Position;
import io.github.projectunified.blockutil.core.iterator.PositionIterator;
import io.github.projectunified.blockutil.core.iterator.WrappedPositionIterator;
import org.bukkit.util.Vector;

/**
 * The {@link PositionIterator} for {@link Vector}
 */
public class VectorIterator extends WrappedPositionIterator<Vector> {
    /**
     * Create a new iterator
     *
     * @param positionIterator the position iterator
     */
    public VectorIterator(PositionIterator positionIterator) {
        super(positionIterator);
    }

    @Override
    protected Vector convert(Position position) {
        return new Vector(position.x, position.y, position.z);
    }
}
