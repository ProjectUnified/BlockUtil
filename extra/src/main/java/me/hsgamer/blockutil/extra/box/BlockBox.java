package me.hsgamer.blockutil.extra.box;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BlockBox {
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;
    public final boolean maxInclusive;

    public BlockBox(int x1, int y1, int z1, int x2, int y2, int z2, boolean maxInclusive) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2) - (maxInclusive ? 0 : 1);
        maxY = Math.max(y1, y2) - (maxInclusive ? 0 : 1);
        maxZ = Math.max(z1, z2) - (maxInclusive ? 0 : 1);
        this.maxInclusive = maxInclusive;
    }

    public BlockBox(Location loc1, Location loc2, boolean maxInclusive) {
        this(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ(), maxInclusive);
    }

    public BlockBox(Vector vec1, Vector vec2, boolean maxInclusive) {
        this(vec1.getBlockX(), vec1.getBlockY(), vec1.getBlockZ(), vec2.getBlockX(), vec2.getBlockY(), vec2.getBlockZ(), maxInclusive);
    }
}
