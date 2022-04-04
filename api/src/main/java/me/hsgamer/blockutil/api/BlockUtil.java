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
import java.util.function.Supplier;

public final class BlockUtil {
    private static final BlockHandler HANDLER;
    private static final Map<Supplier<Boolean>, Supplier<BlockHandler>> HANDLERS = new LinkedHashMap<>();

    static {
        registerWithVersion("v1_18_R2");
        registerWithVersion("v1_18_R1");
        registerWithVersion("v1_17_R1");
        registerWithVersion("v1_16_R3");

        AtomicReference<BlockHandler> handler = new AtomicReference<>();
        HANDLERS.forEach((supplier, blockHandlerSupplier) -> {
            if (Boolean.TRUE.equals(supplier.get())) {
                BlockHandler blockHandler = blockHandlerSupplier.get();
                if (blockHandler != null) {
                    handler.set(blockHandler);
                }
            }
        });
        if (handler.get() == null) {
            handler.set(new FallbackBlockHandler());
        }
        HANDLER = handler.get();
    }

    private BlockUtil() {
        // EMPTY
    }

    private static void register(Supplier<Boolean> supplier, Supplier<BlockHandler> blockHandlerSupplier) {
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
        return HANDLER;
    }

    public static boolean isSurrounded(Block block) {
        final Block east = block.getRelative(BlockFace.EAST);
        final Block west = block.getRelative(BlockFace.WEST);
        final Block south = block.getRelative(BlockFace.SOUTH);
        final Block north = block.getRelative(BlockFace.NORTH);
        final Block up = block.getRelative(BlockFace.UP);
        final Block down = block.getRelative(BlockFace.DOWN);
        return !east.getType().isTransparent()
                && !west.getType().isTransparent()
                && !up.getType().isTransparent()
                && !down.getType().isTransparent()
                && !south.getType().isTransparent()
                && !north.getType().isTransparent();
    }

    public static void updateLight(Block block) {
        if (block.getType() != Material.AIR && !isSurrounded(block)) {
            getHandler().updateLight(block);
        }
    }
}
