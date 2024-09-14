package io.github.projectunified.blockutil.folia;

import io.github.projectunified.blockutil.api.BlockData;
import io.github.projectunified.blockutil.api.BlockProcess;
import io.github.projectunified.blockutil.api.Pair;
import io.github.projectunified.blockutil.simple.SimpleBlockHandler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class FoliaBlockHandler implements SimpleBlockHandler {
    private final Plugin plugin;
    private int blocksPerTick = 1;
    private long blockDelay = 0;

    public FoliaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isAvailable() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public FoliaBlockHandler setBlocksPerTick(int blocksPerTick) {
        this.blocksPerTick = blocksPerTick;
        return this;
    }

    public FoliaBlockHandler setBlockDelay(long blockDelay) {
        this.blockDelay = blockDelay;
        return this;
    }

    private void setBlock(Block block, BlockData blockData) {
        if (blockData.state == null) {
            block.setType(blockData.material, false);
        } else {
            org.bukkit.block.data.BlockData bukkitBlockData = Bukkit.createBlockData(blockData.material, blockData.state);
            block.setBlockData(bukkitBlockData, false);
        }
    }

    private BlockProcess setBlock(World world, Supplier<Map<ChunkIndex, Queue<Pair<Position, BlockData>>>> chunkIndexSupplier, boolean urgent) {
        if (urgent) {
            for (Map.Entry<ChunkIndex, Queue<Pair<Position, BlockData>>> entry : chunkIndexSupplier.get().entrySet()) {
                ChunkIndex chunkIndex = entry.getKey();
                Queue<Pair<Position, BlockData>> queue = entry.getValue();
                Bukkit.getRegionScheduler().execute(plugin, world, chunkIndex.x, chunkIndex.z, () -> {
                    while (true) {
                        Pair<Position, BlockData> positionBlockDataPair = queue.poll();
                        if (positionBlockDataPair == null) {
                            break;
                        }
                        Block block = BukkitBlockAdapter.adapt(world, positionBlockDataPair.key).getBlock();
                        BlockData blockData = positionBlockDataPair.value;
                        setBlock(block, blockData);
                    }
                });
            }
            return BlockProcess.COMPLETED;
        } else {
            Set<ScheduledTask> chunkTasks = ConcurrentHashMap.newKeySet();

            ScheduledTask scheduleChunkTask = Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                for (Map.Entry<ChunkIndex, Queue<Pair<Position, BlockData>>> entry : chunkIndexSupplier.get().entrySet()) {
                    ChunkIndex chunkIndex = entry.getKey();
                    Queue<Pair<Position, BlockData>> queue = entry.getValue();
                    ScheduledTask task = Bukkit.getRegionScheduler().runAtFixedRate(plugin, world, chunkIndex.x, chunkIndex.z, s -> {
                        for (int i = 0; i < blocksPerTick; i++) {
                            Pair<Position, BlockData> positionBlockDataPair = queue.poll();
                            if (positionBlockDataPair == null) {
                                s.cancel();
                                break;
                            }
                            Block block = BukkitBlockAdapter.adapt(world, positionBlockDataPair.key).getBlock();
                            BlockData blockData = positionBlockDataPair.value;
                            setBlock(block, blockData);
                        }
                    }, Math.max(1, blockDelay), Math.max(1, blockDelay));
                    chunkTasks.add(task);
                }
            });

            return new BlockProcess() {
                @Override
                public boolean isDone() {
                    return (scheduleChunkTask.isCancelled() || scheduleChunkTask.getExecutionState() == ScheduledTask.ExecutionState.FINISHED)
                            && chunkTasks.stream().allMatch(scheduledTask -> scheduledTask.isCancelled() || scheduledTask.getExecutionState() == ScheduledTask.ExecutionState.FINISHED);
                }

                @Override
                public void cancel() {
                    if (!scheduleChunkTask.isCancelled() && scheduleChunkTask.getExecutionState() != ScheduledTask.ExecutionState.FINISHED) {
                        scheduleChunkTask.cancel();
                    }
                    chunkTasks.forEach(scheduledTask -> {
                        if (!scheduledTask.isCancelled() && scheduledTask.getExecutionState() != ScheduledTask.ExecutionState.FINISHED) {
                            scheduledTask.cancel();
                        }
                    });
                }
            };
        }
    }

    @Override
    public BlockProcess setBlock(World world, PositionIterator iterator, Supplier<BlockData> blockDataSupplier, boolean urgent) {
        return setBlock(world, () -> {
            Map<ChunkIndex, Queue<Pair<Position, BlockData>>> chunkMap = new HashMap<>();
            while (iterator.hasNext()) {
                Position position = iterator.next();
                BlockData blockData = blockDataSupplier.get();
                ChunkIndex chunkIndex = new ChunkIndex(position);
                chunkMap.computeIfAbsent(chunkIndex, index -> new ArrayDeque<>()).add(new Pair<>(position, blockData));
            }
            return chunkMap;
        }, urgent);
    }

    @Override
    public BlockProcess setBlock(World world, List<Pair<Position, BlockData>> blocks, boolean urgent) {
        return setBlock(world, () -> {
            Map<ChunkIndex, Queue<Pair<Position, BlockData>>> chunkMap = new HashMap<>();
            for (Pair<Position, BlockData> entry : blocks) {
                Position position = entry.key;
                BlockData blockData = entry.value;
                ChunkIndex chunkIndex = new ChunkIndex(position);
                chunkMap.computeIfAbsent(chunkIndex, index -> new ArrayDeque<>()).add(new Pair<>(position, blockData));
            }
            return chunkMap;
        }, urgent);
    }

    private static class ChunkIndex {
        private final int x;
        private final int z;

        private ChunkIndex(int x, int z) {
            this.x = x;
            this.z = z;
        }

        private ChunkIndex(Position position) {
            this((int) position.x >> 4, (int) position.z >> 4);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkIndex that = (ChunkIndex) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
}
