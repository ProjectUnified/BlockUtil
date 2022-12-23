package me.hsgamer.blockutil.vanilla;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.iterator.VectorIterator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;

public class VanillaBlockHandler implements BlockHandler {
    private static final int BLOCKS_PER_TICK = Math.max(1, Integer.parseInt(System.getProperty("blockutil.blocksPerTick", "50")));
    private static final long BLOCK_DELAY = Math.max(0, Long.parseLong(System.getProperty("blockutil.blockDelay", "0")));
    private final Plugin plugin;

    public VanillaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BlockProcess setRandomBlocks(World world, VectorIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < BLOCKS_PER_TICK; i++) {
                    if (iterator.hasNext()) {
                        Block block = iterator.nextLocation(world).getBlock();
                        XBlock.setType(block, probabilityCollection.get(), false);
                    } else {
                        cancel();
                        future.complete(null);
                        break;
                    }
                }
            }
        }.runTaskTimer(plugin, BLOCK_DELAY, BLOCK_DELAY);
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
    public BlockProcess clearBlocks(World world, VectorIterator iterator) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < BLOCKS_PER_TICK; i++) {
                    if (iterator.hasNext()) {
                        Block block = iterator.nextLocation(world).getBlock();
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
        }.runTaskTimer(plugin, BLOCK_DELAY, BLOCK_DELAY);
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
    public void clearBlockFast(World world, VectorIterator iterator) {
        while (iterator.hasNext()) {
            Block block = iterator.nextLocation(world).getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR, false);
            }
        }
    }
}
