package me.hsgamer.blockutil.abstraction;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class BlockHandlerSettings {
    public static final AtomicBoolean USE_FAWE = new AtomicBoolean(true);
    public static final AtomicBoolean USE_WE = new AtomicBoolean(true);
    public static final AtomicInteger BLOCKS_PER_TICK = new AtomicInteger(50);
    public static final AtomicLong BLOCK_DELAY = new AtomicLong(0);

    private BlockHandlerSettings() {
        // EMPTY
    }
}
