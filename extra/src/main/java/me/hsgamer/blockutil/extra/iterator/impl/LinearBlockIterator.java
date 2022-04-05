package me.hsgamer.blockutil.extra.iterator.impl;

import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.blockutil.extra.iterator.api.BaseBlockIterator;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;

public class LinearBlockIterator extends BaseBlockIterator {
    public static final LinearCoordinate X_COORDINATE = new LinearCoordinate() {
        @Override
        public boolean hasNext(Vector current, BaseBlockIterator iterator) {
            return current.getX() < iterator.box.maxX;
        }

        @Override
        public void next(Vector next) {
            next.setX(next.getX() + 1);
        }

        @Override
        public void reset(Vector next, BaseBlockIterator iterator) {
            next.setX(iterator.box.minX);
        }
    };
    public static final LinearCoordinate Y_COORDINATE = new LinearCoordinate() {
        @Override
        public boolean hasNext(Vector current, BaseBlockIterator iterator) {
            return current.getY() < iterator.box.maxY;
        }

        @Override
        public void next(Vector next) {
            next.setY(next.getY() + 1);
        }

        @Override
        public void reset(Vector next, BaseBlockIterator iterator) {
            next.setY(iterator.box.minY);
        }
    };
    public static final LinearCoordinate Z_COORDINATE = new LinearCoordinate() {
        @Override
        public boolean hasNext(Vector current, BaseBlockIterator iterator) {
            return current.getZ() < iterator.box.maxZ;
        }

        @Override
        public void next(Vector next) {
            next.setZ(next.getZ() + 1);
        }

        @Override
        public void reset(Vector next, BaseBlockIterator iterator) {
            next.setZ(iterator.box.minZ);
        }
    };

    private final LinearCoordinate[] coordinates;

    public LinearBlockIterator(BlockBox box, LinearCoordinate... coordinates) {
        super(box);
        this.coordinates = coordinates;
    }

    public LinearBlockIterator(BlockBox box) {
        this(box, X_COORDINATE, Y_COORDINATE, Z_COORDINATE);
    }

    @Override
    public Vector initial() {
        return new Vector(box.minX, box.minY, box.minZ);
    }

    @Override
    public Vector getContinue(Vector current) throws NoSuchElementException {
        Vector next = current.clone();
        for (int i = 0; i < coordinates.length; i++) {
            LinearCoordinate coordinate = coordinates[i];
            if (coordinate.hasNext(next, this)) {
                coordinate.next(next);
                break;
            } else if (i == coordinates.length - 1) {
                throw new NoSuchElementException("No more elements");
            } else {
                coordinate.reset(next, this);
            }
        }
        return next;
    }

    @Override
    public boolean hasContinue(Vector current) {
        return current.getX() < box.maxX || current.getY() < box.maxY || current.getZ() < box.maxZ;
    }

    public interface LinearCoordinate {
        boolean hasNext(Vector current, BaseBlockIterator iterator);

        void next(Vector next);

        void reset(Vector next, BaseBlockIterator iterator);
    }
}
