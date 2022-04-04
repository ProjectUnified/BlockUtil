package me.hsgamer.blockutil.fallback;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FallbackBlockHandler implements BlockHandler {
    private final Method setDataMethod;

    public FallbackBlockHandler() {
        Method method;
        try {
            method = Block.class.getDeclaredMethod("setData", byte.class, boolean.class);
        } catch (NoSuchMethodException e) {
            method = null;
        }
        setDataMethod = method;
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
        // EMPTY
    }
}
