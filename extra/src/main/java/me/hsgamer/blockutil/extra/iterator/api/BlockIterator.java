package me.hsgamer.blockutil.extra.iterator.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Iterator;

public interface BlockIterator extends Iterator<Vector> {
    void reset();

    default Location nextLocation(World world) {
        return this.next().toLocation(world);
    }
}
