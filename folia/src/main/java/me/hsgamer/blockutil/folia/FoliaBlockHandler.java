package me.hsgamer.blockutil.folia;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Objects;
import com.lewdev.probabilitylib.ProbabilityCollection;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FoliaBlockHandler implements BlockHandler {
    private final int blocksPerTick = Math.max(1, BlockHandlerSettings.BLOCKS_PER_TICK.get());
    private final long blockDelay = Math.max(0, BlockHandlerSettings.BLOCK_DELAY.get());
    private final Plugin plugin;

    public FoliaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static ChunkIndex getChunkIndex(Position position) {
        int blockX = (int) position.x;
        int blockZ = (int) position.z;
        return new ChunkIndex(blockX >> 4, blockZ >> 4);
    }

    private static MaterialPositionPair wrapMaterialPositionPair(XMaterial material, Position position) {
        return new MaterialPositionPair(material, position);
    }

    private BlockProcess setBlocks(World world, PositionIterator iterator, Supplier<XMaterial> materialSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Set<ScheduledTask> chunkTasks = ConcurrentHashMap.newKeySet();
        Map<ChunkIndex, Queue<MaterialPositionPair>> chunkMap = new ConcurrentHashMap<>();

        Function<ChunkIndex, Queue<MaterialPositionPair>> chunkQueueFunction = index -> chunkMap.computeIfAbsent(index, chunkIndex -> {
            Queue<MaterialPositionPair> queue = new ConcurrentLinkedQueue<>();
            ScheduledTask task = Bukkit.getRegionScheduler().run(plugin, world, chunkIndex.x, chunkIndex.z, s -> {
                while (!future.isDone()) {
                    MaterialPositionPair materialLocationPair = queue.poll();
                    if (materialLocationPair == null) continue;
                    Block block = BukkitBlockAdapter.adapt(world, materialLocationPair.position).getBlock();
                    XBlock.setType(block, materialLocationPair.material, false);
                }
            });
            chunkTasks.add(task);
            return queue;
        });

        Consumer<ScheduledTask> runnable = scheduledTask -> {
            for (int i = 0; i < blocksPerTick; i++) {
                if (iterator.hasNext()) {
                    Position position = iterator.next();
                    XMaterial material = materialSupplier.get();
                    chunkQueueFunction.apply(getChunkIndex(position)).add(wrapMaterialPositionPair(material, position));
                } else {
                    scheduledTask.cancel();
                    future.complete(null);
                    break;
                }
            }
        };

        long blockDelayMillis = Math.max(1, blockDelay * 50L);
        ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, runnable, blockDelayMillis, blockDelayMillis, TimeUnit.MILLISECONDS);

        return new BlockProcess() {
            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public void cancel() {
                task.cancel();
                if (!future.isDone()) {
                    future.completeExceptionally(new RuntimeException("Cancelled"));
                }
                chunkTasks.forEach(ScheduledTask::cancel);
            }
        };
    }

    @Override
    public BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setBlocks(world, iterator, probabilityCollection::get);
    }

    @Override
    public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setRandomBlocks(world, BlockHandler.iterator(blockBox), probabilityCollection);
    }

    @Override
    public BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
        return setBlocks(world, iterator, () -> material);
    }

    @Override
    public BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
        return setBlocks(world, BlockHandler.iterator(blockBox), material);
    }

    @Override
    public void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
        while (iterator.hasNext()) {
            Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
            XBlock.setType(block, material, false);
        }
    }

    @Override
    public void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
        setBlocksFast(world, BlockHandler.iterator(blockBox), material);
    }

    private static class ChunkIndex {
        private final int x;
        private final int z;

        private ChunkIndex(int x, int z) {
            this.x = x;
            this.z = z;
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
