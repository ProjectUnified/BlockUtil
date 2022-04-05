package me.hsgamer.blockutil.extra.iterator.impl;

import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.blockutil.extra.iterator.api.BaseBlockIterator;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class RandomBlockIterator extends BaseBlockIterator {
    private final LinkedList<Vector> queue;

    public RandomBlockIterator(BlockBox box) {
        super(box);
        queue = new LinkedList<>();
    }

    @Override
    public void reset() {
        super.reset();
        queue.clear();
    }

    @Override
    public Vector initial() {
        for (int x = box.minX; x <= box.maxX; x++) {
            for (int y = box.minY; y <= box.maxY; y++) {
                for (int z = box.minZ; z <= box.maxZ; z++) {
                    queue.add(new Vector(x, y, z));
                }
            }
        }
        Collections.shuffle(queue);
        return queue.poll();
    }

    @Override
    public Vector getContinue(Vector current) throws NoSuchElementException {
        Vector vector = queue.poll();
        if (vector == null) {
            throw new NoSuchElementException();
        }
        return vector;
    }

    @Override
    public boolean hasContinue(Vector current) {
        return !queue.isEmpty();
    }
}
