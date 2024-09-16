package io.github.projectunified.blockutil.vanilla;

import io.github.projectunified.blockutil.api.BlockData;
import io.github.projectunified.blockutil.api.BlockProcess;
import io.github.projectunified.blockutil.api.Pair;
import io.github.projectunified.blockutil.api.Version;
import io.github.projectunified.blockutil.simple.SimpleBlockHandler;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;

public class VanillaBlockHandler implements SimpleBlockHandler {
    private static final Method SET_TYPE_AND_DATA_METHOD;

    static {
        Method method = null;
        try {
            //noinspection JavaReflectionMemberAccess
            method = Block.class.getMethod("setTypeIdAndData", int.class, byte.class, boolean.class);
        } catch (NoSuchMethodException e) {
            // IGNORED
        }
        SET_TYPE_AND_DATA_METHOD = method;
    }

    private final Plugin plugin;
    private int blocksPerTick = 1;
    private long blockDelay = 0;

    public VanillaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public VanillaBlockHandler setBlocksPerTick(int blocksPerTick) {
        this.blocksPerTick = blocksPerTick;
        return this;
    }

    public VanillaBlockHandler setBlockDelay(long blockDelay) {
        this.blockDelay = blockDelay;
        return this;
    }

    private void setBlock(Block block, BlockData blockData) {
        if (Version.isFlat()) {
            if (blockData.state == null) {
                block.setType(blockData.material, false);
            } else {
                org.bukkit.block.data.BlockData bukkitBlockData = Bukkit.createBlockData(blockData.material, blockData.state);
                block.setBlockData(bukkitBlockData, false);
            }
        } else {
            try {
                //noinspection deprecation
                SET_TYPE_AND_DATA_METHOD.invoke(block, blockData.material.getId(), blockData.data, false);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to set block", e);
            }
        }
    }

    @Override
    public BlockProcess setBlock(World world, PositionIterator iterator, Supplier<BlockData> blockDataSupplier, boolean urgent) {
        if (urgent) {
            while (iterator.hasNext()) {
                Position position = iterator.next();
                Block block = BukkitBlockAdapter.adaptAsBlock(world, position);
                BlockData blockData = blockDataSupplier.get();
                setBlock(block, blockData);
            }
            return BlockProcess.COMPLETED;
        } else {
            CompletableFuture<Void> future = new CompletableFuture<>();
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < blocksPerTick; i++) {
                        if (iterator.hasNext()) {
                            Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
                            BlockData blockData = blockDataSupplier.get();
                            setBlock(block, blockData);
                        } else {
                            cancel();
                            future.complete(null);
                            break;
                        }
                    }
                }
            }.runTaskTimer(plugin, blockDelay, blockDelay);
            return new BlockProcess() {
                @Override
                public boolean isDone() {
                    return future.isDone();
                }

                @Override
                public void cancel() {
                    future.cancel(true);
                    task.cancel();
                }
            };
        }
    }

    @Override
    public BlockProcess setBlock(World world, List<Pair<Position, BlockData>> blocks, boolean urgent) {
        if (urgent) {
            for (Pair<Position, BlockData> pair : blocks) {
                Block block = BukkitBlockAdapter.adaptAsBlock(world, pair.key);
                BlockData blockData = pair.value;
                setBlock(block, blockData);
            }
            return BlockProcess.COMPLETED;
        } else {
            Queue<Pair<Position, BlockData>> queue = new ArrayDeque<>(blocks);

            CompletableFuture<Void> future = new CompletableFuture<>();
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < blocksPerTick; i++) {
                        Pair<Position, BlockData> pair = queue.poll();
                        if (pair != null) {
                            Block block = BukkitBlockAdapter.adapt(world, pair.key).getBlock();
                            BlockData blockData = pair.value;
                            setBlock(block, blockData);
                        } else {
                            cancel();
                            future.complete(null);
                            break;
                        }
                    }
                }
            }.runTaskTimer(plugin, blockDelay, blockDelay);
            return new BlockProcess() {
                @Override
                public boolean isDone() {
                    return future.isDone();
                }

                @Override
                public void cancel() {
                    future.cancel(true);
                    task.cancel();
                }
            };
        }
    }
}
