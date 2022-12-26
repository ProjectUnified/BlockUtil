package me.hsgamer.blockutil.we;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.box.BlockBox;
import me.hsgamer.hscore.bukkit.block.iterator.VectorIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WeBlockHandler implements BlockHandler {
    private static final BlockState AIR = Objects.requireNonNull(BukkitAdapter.asBlockType(Material.AIR)).getDefaultState();
    private final int maxBlocks = BlockHandlerSettings.MAX_BLOCKS.get();
    private final Plugin plugin;

    public WeBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static RandomPattern createRandomPattern(ProbabilityCollection<XMaterial> probabilityCollection) {
        RandomPattern randomPattern = new RandomPattern();
        probabilityCollection.iterator().forEachRemaining(element -> {
            Material material = element.getObject().parseMaterial();
            if (material != null) {
                BlockType blockType = BukkitAdapter.asBlockType(material);
                if (blockType != null) {
                    randomPattern.add(blockType.getDefaultState(), element.getProbability());
                }
            }
        });
        return randomPattern;
    }

    private static Set<BlockVector3> createBlockVectors(VectorIterator iterator) {
        Set<BlockVector3> blockVectors = new HashSet<>();
        iterator.forEachRemaining(vector -> blockVectors.add(BlockVector3.at(vector.getX(), vector.getY(), vector.getZ())));
        return blockVectors;
    }

    private BlockProcess setBlocks(com.sk89q.worldedit.world.World bukkitWorld, Set<BlockVector3> blockVectors, Pattern pattern) {
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        BukkitTask task = Bukkit.getScheduler().runTask(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .maxBlocks(maxBlocks)
                    .build()
            ) {
                for (BlockVector3 blockVector : blockVectors) {
                    session.setBlock(blockVector, pattern);
                }
            } catch (MaxChangedBlocksException e) {
                plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
            } finally {
                blockFuture.complete(null);
            }
        });
        return new BlockProcess() {
            @Override
            public boolean isDone() {
                return blockFuture.isDone();
            }

            @Override
            public void cancel() {
                task.cancel();
            }
        };
    }

    private BlockProcess setBlocks(World world, BlockBox blockBox, Pattern pattern) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        CuboidRegion region = new CuboidRegion(
                bukkitWorld,
                BlockVector3.at(blockBox.minX, blockBox.minY, blockBox.minZ),
                BlockVector3.at(blockBox.maxX, blockBox.maxY, blockBox.maxZ)
        );
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        BukkitTask task = Bukkit.getScheduler().runTask(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .maxBlocks(maxBlocks)
                    .build()
            ) {
                session.setBlocks(region, pattern);
            } catch (MaxChangedBlocksException e) {
                plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
            } finally {
                blockFuture.complete(null);
            }
        });
        return new BlockProcess() {
            @Override
            public boolean isDone() {
                return blockFuture.isDone();
            }

            @Override
            public void cancel() {
                task.cancel();
            }
        };
    }

    @Override
    public BlockProcess setRandomBlocks(World world, VectorIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        if (world == null) return BlockProcess.COMPLETED;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        RandomPattern randomPattern = createRandomPattern(probabilityCollection);
        return setBlocks(bukkitWorld, blockVectors, randomPattern);
    }

    @Override
    public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        if (world == null) return BlockProcess.COMPLETED;
        return setBlocks(world, blockBox, createRandomPattern(probabilityCollection));
    }

    @Override
    public BlockProcess clearBlocks(World world, VectorIterator iterator) {
        if (world == null) return BlockProcess.COMPLETED;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        return setBlocks(bukkitWorld, blockVectors, AIR);
    }

    @Override
    public BlockProcess clearBlocks(World world, BlockBox blockBox) {
        return setBlocks(world, blockBox, AIR);
    }

    @Override
    public void clearBlockFast(World world, VectorIterator iterator) {
        if (world == null) return;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(maxBlocks)
                .build()
        ) {
            for (BlockVector3 blockVector : blockVectors) {
                session.setBlock(blockVector, AIR);
            }
        } catch (MaxChangedBlocksException e) {
            plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
        }
    }

    @Override
    public void clearBlocksFast(World world, BlockBox blockBox) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        CuboidRegion region = new CuboidRegion(
                bukkitWorld,
                BlockVector3.at(blockBox.minX, blockBox.minY, blockBox.minZ),
                BlockVector3.at(blockBox.maxX, blockBox.maxY, blockBox.maxZ)
        );
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(maxBlocks)
                .build()
        ) {
            session.setBlocks(region, AIR);
        } catch (MaxChangedBlocksException e) {
            plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
        }
    }
}
