package me.hsgamer.blockutil.extra.iterator.api;

import me.hsgamer.blockutil.extra.box.BlockBox;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseBlockIterator extends AbstractBlockIterator {
    private final AtomicReference<Vector> current;

    protected BaseBlockIterator(BlockBox box) {
        super(box);
        this.current = new AtomicReference<>();
    }

    public void reset() {
        this.current.set(null);
    }

    public Vector getCurrent() {
        return this.current.get();
    }

    public abstract Vector initial();

    public abstract Vector getContinue(Vector current) throws NoSuchElementException;

    public abstract boolean hasContinue(Vector current);

    @Override
    public boolean hasNext() {
        Vector vector = this.current.get();
        return vector == null || hasContinue(vector);
    }

    @Override
    public Vector next() {
        Vector vector = getCurrent();
        if (vector == null) {
            vector = initial();
        } else {
            vector = getContinue(vector);
        }
        this.current.set(vector);
        return vector;
    }
}
