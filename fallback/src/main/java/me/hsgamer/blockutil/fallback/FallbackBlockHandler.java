package me.hsgamer.blockutil.fallback;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FallbackBlockHandler implements BlockHandler {
    private final Method setDataMethod;
    private final Method refreshMethod;
    private final Method setBlockDataMethod;

    public FallbackBlockHandler() {
        Method method;
        try {
            method = Block.class.getDeclaredMethod("setData", byte.class, boolean.class);
        } catch (NoSuchMethodException e) {
            method = null;
        }
        setDataMethod = method;

        try {
            method = World.class.getDeclaredMethod("refreshChunk", int.class, int.class);
        } catch (NoSuchMethodException e) {
            method = null;
        }
        refreshMethod = method;

        try {
            Class<?> clazz = Class.forName("org.bukkit.block.data.BlockData");
            method = Block.class.getDeclaredMethod("setBlockData", clazz, boolean.class);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            method = null;
        }
        setBlockDataMethod = method;
    }

    @Override
    public void setBlockData(Block block, Object blockData, boolean applyPhysics, boolean doPlace) throws IllegalArgumentException {
        BlockHandler.super.setBlockData(block, blockData, applyPhysics, doPlace);
        if (setBlockDataMethod != null) {
            try {
                setBlockDataMethod.invoke(block, blockData, applyPhysics);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // IGNORED
            }
        }
    }

    @Override
    public void setBlock(Block block, Material material, byte data, boolean applyPhysics, boolean doPlace) {
        block.setType(material, applyPhysics);

        if (setDataMethod != null) {
            try {
                setDataMethod.invoke(block, data, applyPhysics);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // IGNORED
            }
        }
    }

    @Override
    public void updateLight(Block block) {
        // EMPTY
    }

    @Override
    public void sendChunkUpdate(Player player, Chunk chunk) {
        if (refreshMethod != null) {
            try {
                refreshMethod.invoke(chunk.getWorld(), chunk.getX(), chunk.getZ());
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                // IGNORED
            }
        }
    }
}
