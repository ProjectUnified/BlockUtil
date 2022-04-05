package me.hsgamer.blockutil.extra.iterator;

import me.hsgamer.blockutil.extra.box.BlockBox;
import me.hsgamer.blockutil.extra.iterator.api.BlockIterator;
import me.hsgamer.blockutil.extra.iterator.impl.LinearBlockIterator;
import me.hsgamer.blockutil.extra.iterator.impl.RandomBlockIterator;
import me.hsgamer.blockutil.extra.iterator.impl.RandomTypeBlockIterator;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringHashMap;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import static me.hsgamer.blockutil.extra.iterator.impl.LinearBlockIterator.*;

public final class BlockIteratorUtil {
    private static final Map<String, Function<BlockBox, BlockIterator>> TYPE_MAP = new CaseInsensitiveStringHashMap<>();

    static {
        registerType("random", RandomBlockIterator::new);
        registerType("xyz", box -> new LinearBlockIterator(box, X_COORDINATE, Y_COORDINATE, Z_COORDINATE));
        registerType("xzy", box -> new LinearBlockIterator(box, X_COORDINATE, Z_COORDINATE, Y_COORDINATE));
        registerType("yxz", box -> new LinearBlockIterator(box, Y_COORDINATE, X_COORDINATE, Z_COORDINATE));
        registerType("yzx", box -> new LinearBlockIterator(box, Y_COORDINATE, Z_COORDINATE, X_COORDINATE));
        registerType("zxy", box -> new LinearBlockIterator(box, Z_COORDINATE, X_COORDINATE, Y_COORDINATE));
        registerType("zyx", box -> new LinearBlockIterator(box, Z_COORDINATE, Y_COORDINATE, X_COORDINATE));
        registerType("default", LinearBlockIterator::new);
    }

    private BlockIteratorUtil() {
        // EMPTY
    }

    public static void registerType(String type, Function<BlockBox, BlockIterator> typeFunction) {
        TYPE_MAP.put(type, typeFunction);
    }

    public static BlockIterator random(BlockBox box) {
        return new RandomTypeBlockIterator(box, new ArrayList<>(TYPE_MAP.values()));
    }

    public static BlockIterator get(String type, BlockBox box) {
        return TYPE_MAP.getOrDefault(type, BlockIteratorUtil::random).apply(box);
    }
}
