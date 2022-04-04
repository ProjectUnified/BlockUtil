package me.hsgamer.blockutil.api;

import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.fallback.FallbackBlockHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class BlockUtil {
    private static final AtomicReference<BlockHandler> HANDLER_REFERENCE = new AtomicReference<>();
    private static final Map<BooleanSupplier, Supplier<BlockHandler>> HANDLERS = new LinkedHashMap<>();
    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    static {
        registerWithVersion("v1_18_R2");
        registerWithVersion("v1_18_R1");
        registerWithVersion("v1_17_R1");
        registerWithVersion("v1_16_R3");
        registerWithVersion("v1_15_R1");
        registerWithVersion("v1_14_R1");
        registerWithVersion("v1_13_R2");
        registerWithVersion("v1_12_R1");
    }

    private BlockUtil() {
        // EMPTY
    }

    public static void register(BooleanSupplier supplier, Supplier<BlockHandler> blockHandlerSupplier) {
        HANDLER_REFERENCE.set(null);
        HANDLERS.put(supplier, blockHandlerSupplier);
    }

    private static void registerWithVersion(String version) {
        register(() -> Bukkit.getServer().getClass().getPackage().getName().toLowerCase(Locale.ROOT).contains(version.toLowerCase(Locale.ROOT)), getVersionHandlerSupplier(version));
    }

    private static Supplier<BlockHandler> getHandlerSupplier(String className) {
        return () -> {
            try {
                return (BlockHandler) Class.forName(className).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        };
    }

    private static Supplier<BlockHandler> getVersionHandlerSupplier(String version) {
        return getHandlerSupplier("me.hsgamer.blockutil.nms." + version.toLowerCase(Locale.ROOT) + ".NMSBlockHandler");
    }

    public static BlockHandler getHandler() {
        BlockHandler handler = HANDLER_REFERENCE.get();
        if (handler == null) {
            for (Map.Entry<BooleanSupplier, Supplier<BlockHandler>> entry : HANDLERS.entrySet()) {
                BooleanSupplier supplier = entry.getKey();
                Supplier<BlockHandler> blockHandlerSupplier = entry.getValue();
                if (supplier.getAsBoolean()) {
                    BlockHandler blockHandler = blockHandlerSupplier.get();
                    if (blockHandler != null) {
                        handler = blockHandler;
                        break;
                    }
                }
            }
            if (handler == null) {
                handler = new FallbackBlockHandler();
            }
            HANDLER_REFERENCE.set(handler);
        }
        return handler;
    }

    public static boolean isSurrounded(Block block) {
        for (BlockFace face : FACES) {
            Material material = block.getRelative(face).getType();
            if (material.name().contains("AIR") || material.isTransparent()) {
                return false;
            }
        }
        return true;
    }

    public static void updateLight(Block block) {
        if (!isSurrounded(block)) {
            getHandler().updateLight(block);
        }
    }
}
