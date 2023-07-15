package me.hsgamer.blockutil.folia;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Objects;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.blockutil.abstraction.SimpleBlockHandler;
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

    public FoliaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isAvailable() {
        return XMaterial.supports(16) && Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null && BlockHandlerSettings.getBoolean("use-fawe", true);
    }

    private int getBlocksPerTick() {
        String value = BlockHandlerSettings.get("blocks-per-tick", "1");
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private long getBlockDelay() {
        String value = BlockHandlerSettings.get("block-delay", "0");
        try {
            return Math.max(0, Long.parseLong(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BlockProcess setBlocks(World world, Supplier<Map<ChunkIndex, Queue<MaterialPositionPair>>> chunkIndexSupplier) {
        int blocksPerTick = getBlocksPerTick();
        long blockDelay = getBlockDelay();

        Set<ScheduledTask> chunkTasks = ConcurrentHashMap.newKeySet();

        ScheduledTask scheduleChunkTask = Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            for (Map.Entry<ChunkIndex, Queue<MaterialPositionPair>> entry : chunkIndexSupplier.get().entrySet()) {
                ChunkIndex chunkIndex = entry.getKey();
                Queue<MaterialPositionPair> queue = entry.getValue();
                ScheduledTask task = Bukkit.getRegionScheduler().runAtFixedRate(plugin, world, chunkIndex.x, chunkIndex.z, s -> {
                    for (int i = 0; i < blocksPerTick; i++) {
                        MaterialPositionPair materialLocationPair = queue.poll();
                        if (materialLocationPair == null) {
                            s.cancel();
                            break;
                        }
                        Block block = BukkitBlockAdapter.adapt(world, materialLocationPair.position).getBlock();
                        XBlock.setType(block, materialLocationPair.material, false);
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

    @Override
    public BlockProcess setBlocks(World world, PositionIterator iterator, Supplier<XMaterial> materialSupplier) {
        return setBlocks(world, () -> {
            Map<ChunkIndex, Queue<MaterialPositionPair>> chunkMap = new HashMap<>();
            while (iterator.hasNext()) {
                Position position = iterator.next();
                XMaterial material = materialSupplier.get();
                ChunkIndex chunkIndex = new ChunkIndex(position);
                chunkMap.computeIfAbsent(chunkIndex, index -> new ArrayDeque<>()).add(new MaterialPositionPair(material, position));
            }
            return chunkMap;
        });
    }

    @Override
    public BlockProcess setBlocks(World world, Map<XMaterial, Collection<Position>> blockMap) {
        return setBlocks(world, () -> {
            Map<ChunkIndex, Queue<MaterialPositionPair>> chunkMap = new HashMap<>();
            for (Map.Entry<XMaterial, Collection<Position>> entry : blockMap.entrySet()) {
                XMaterial material = entry.getKey();
                for (Position position : entry.getValue()) {
                    ChunkIndex chunkIndex = new ChunkIndex(position);
                    chunkMap.computeIfAbsent(chunkIndex, index -> new ArrayDeque<>()).add(new MaterialPositionPair(material, position));
                }
            }
            return chunkMap;
        });
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
            return Objects.hashCode(x, z);
        }
    }

    private static class MaterialPositionPair {
        private final XMaterial material;
        private final Position position;

        private MaterialPositionPair(XMaterial material, Position position) {
            this.material = material;
            this.position = position;
        }
    }
}
