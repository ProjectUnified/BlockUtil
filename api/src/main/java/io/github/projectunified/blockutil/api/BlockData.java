package io.github.projectunified.blockutil.api;

import org.bukkit.Material;

import java.util.Objects;

public class BlockData {
    public static final BlockData AIR = new BlockData(Material.AIR);
    public final Material material;
    public final byte data;
    public final String state;

    public BlockData(Material material, byte data, String state) {
        this.material = material;
        this.data = data;
        this.state = state;
    }

    public BlockData(Material material, byte data) {
        this(material, data, null);
    }

    public BlockData(Material material, String state) {
        this(material, (byte) 0, state);
    }

    public BlockData(Material material) {
        this(material, (byte) 0, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockData blockData = (BlockData) o;
        return data == blockData.data && material == blockData.material && Objects.equals(state, blockData.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, data, state);
    }
}
