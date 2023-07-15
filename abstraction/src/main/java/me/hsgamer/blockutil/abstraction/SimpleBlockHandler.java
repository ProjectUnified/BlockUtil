package me.hsgamer.blockutil.abstraction;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface SimpleBlockHandler extends BlockHandler {
    static SimpleBlockHandler getDefault(Plugin plugin) {
        return new SimpleBlockHandler() {
            @Override
            public BlockProcess setBlocks(World world, PositionIterator iterator, Supplier<XMaterial> materialSupplier) {
                int blocksPerTick = Math.max(1, BlockHandlerSettings.BLOCKS_PER_TICK.get());
                long blockDelay = Math.max(0, BlockHandlerSettings.BLOCK_DELAY.get());

                CompletableFuture<Void> future = new CompletableFuture<>();
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < blocksPerTick; i++) {
                            if (iterator.hasNext()) {
                                Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
                                XBlock.setType(block, materialSupplier.get(), false);
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
                        task.cancel();
                    }
                };
            }

            @Override
            public BlockProcess setBlocks(World world, Map<XMaterial, Collection<Position>> blockMap) {
                int blocksPerTick = Math.max(1, BlockHandlerSettings.BLOCKS_PER_TICK.get());
                long blockDelay = Math.max(0, BlockHandlerSettings.BLOCK_DELAY.get());

                Queue<Pair<XMaterial, Position>> queue = new ArrayDeque<>();
                blockMap.forEach((material, positions) -> positions.forEach(position -> queue.add(Pair.of(material, position))));

                CompletableFuture<Void> future = new CompletableFuture<>();
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < blocksPerTick; i++) {
                            Pair<XMaterial, Position> pair = queue.poll();
                            if (pair != null) {
                                Block block = BukkitBlockAdapter.adapt(world, pair.getValue()).getBlock();
                                XBlock.setType(block, pair.getKey(), false);
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
                        task.cancel();
                    }
                };
            }
        };
    }

    BlockProcess setBlocks(World world, PositionIterator iterator, Supplier<XMaterial> materialSupplier);

    @Override
    default BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setBlocks(world, iterator, probabilityCollection::get);
    }

    @Override
    default BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setRandomBlocks(world, BlockHandler.iterator(blockBox), probabilityCollection);
    }

    @Override
    default BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
        return setBlocks(world, iterator, () -> material);
    }

    @Override
    default BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
        return setBlocks(world, BlockHandler.iterator(blockBox), material);
    }

    @Override
    default void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
        while (iterator.hasNext()) {
            Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
            XBlock.setType(block, material, false);
        }
    }

    @Override
    default void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
        setBlocksFast(world, BlockHandler.iterator(blockBox), material);
    }

    @Override
    default void setBlocksFast(World world, Map<XMaterial, Collection<Position>> blockMap) {
        blockMap.forEach((material, positions) -> positions.forEach(position -> {
            Block block = BukkitBlockAdapter.adapt(world, position).getBlock();
            XBlock.setType(block, material, false);
        }));
    }
}
