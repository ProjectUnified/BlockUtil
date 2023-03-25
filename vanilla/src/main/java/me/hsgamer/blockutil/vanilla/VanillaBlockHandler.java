package me.hsgamer.blockutil.vanilla;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.BasePositionIterator;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class VanillaBlockHandler implements BlockHandler {
    private final int blocksPerTick = Math.max(1, BlockHandlerSettings.BLOCKS_PER_TICK.get());
    private final long blockDelay = Math.max(0, BlockHandlerSettings.BLOCK_DELAY.get());
    private final Plugin plugin;

    public VanillaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static PositionIterator toIterator(BlockBox blockBox) {
        return new BasePositionIterator(blockBox) {
            @Override
            public Position initial() {
                return new Position(blockBox.minX, blockBox.minY, blockBox.minZ);
            }

            @Override
            public Position getContinue(Position current) throws NoSuchElementException {
                if (current.x < blockBox.maxX) {
                    return new Position(current.x + 1, current.y, current.z);
                } else if (current.y < blockBox.maxY) {
                    return new Position(blockBox.minX, current.y + 1, current.z);
                } else if (current.z < blockBox.maxZ) {
                    return new Position(blockBox.minX, blockBox.minY, current.z + 1);
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public boolean hasContinue(Position current) {
                return current.x < blockBox.maxX || current.y < blockBox.maxY || current.z < blockBox.maxZ;
            }
        };
    }

    @Override
    public BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (iterator.hasNext()) {
                        Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
                        XBlock.setType(block, probabilityCollection.get(), false);
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
    public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        return setRandomBlocks(world, toIterator(blockBox), probabilityCollection);
    }

    @Override
    public BlockProcess clearBlocks(World world, PositionIterator iterator) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (iterator.hasNext()) {
                        Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR, false);
                        }
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
    public BlockProcess clearBlocks(World world, BlockBox blockBox) {
        return clearBlocks(world, toIterator(blockBox));
    }

    @Override
    public void clearBlockFast(World world, PositionIterator iterator) {
        while (iterator.hasNext()) {
            Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR, false);
            }
        }
    }

    @Override
    public void clearBlocksFast(World world, BlockBox blockBox) {
        clearBlockFast(world, toIterator(blockBox));
    }
}
