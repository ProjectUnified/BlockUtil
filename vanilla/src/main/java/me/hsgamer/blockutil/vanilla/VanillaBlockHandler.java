package me.hsgamer.blockutil.vanilla;

import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;

public class VanillaBlockHandler implements BlockHandler {
    private final int blocksPerTick = Math.max(1, BlockHandlerSettings.BLOCKS_PER_TICK.get());
    private final long blockDelay = Math.max(0, BlockHandlerSettings.BLOCK_DELAY.get());
    private final Plugin plugin;

    public VanillaBlockHandler(Plugin plugin) {
        this.plugin = plugin;
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
        return setRandomBlocks(world, BlockHandler.iterator(blockBox), probabilityCollection);
    }

    @Override
    public BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick; i++) {
                    if (iterator.hasNext()) {
                        Block block = BukkitBlockAdapter.adapt(world, iterator.next()).getBlock();
                        XBlock.setType(block, material, false);
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
}
