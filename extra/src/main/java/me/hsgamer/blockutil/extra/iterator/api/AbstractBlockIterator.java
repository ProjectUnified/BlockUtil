package me.hsgamer.blockutil.extra.iterator.api;

import me.hsgamer.blockutil.extra.box.BlockBox;

public abstract class AbstractBlockIterator implements BlockIterator {
    public final BlockBox box;

    protected AbstractBlockIterator(BlockBox box) {
        this.box = box;
    }
}
