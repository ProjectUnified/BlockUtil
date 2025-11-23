package io.github.projectunified.blockutil.spigot.core.iterator;

import io.github.projectunified.blockutil.core.box.Position;
import io.github.projectunified.blockutil.core.iterator.PositionIterator;
import io.github.projectunified.blockutil.core.iterator.WrappedPositionIterator;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * The {@link PositionIterator} for {@link Location}
 */
public class LocationIterator extends WrappedPositionIterator<Location> {
    private final World world;

    /**
     * Create a new iterator
     *
     * @param world            the world
     * @param positionIterator the position iterator
     */
    public LocationIterator(World world, PositionIterator positionIterator) {
        super(positionIterator);
        this.world = world;
    }

    @Override
    protected Location convert(Position position) {
        return new Location(world, position.x, position.y, position.z);
    }
}
