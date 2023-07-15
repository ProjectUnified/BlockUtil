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
import me.hsgamer.hscore.common.Pair;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WeBlockHandler implements BlockHandler {
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

    private static Set<BlockVector3> createBlockVectors(Iterator<Position> iterator) {
        Set<BlockVector3> blockVectors = new HashSet<>();
        iterator.forEachRemaining(position -> blockVectors.add(BlockVector3.at(position.x, position.y, position.z)));
        return blockVectors;
    }

    private int getMaxBlocks() {
        String value = BlockHandlerSettings.get("max-blocks", "-1");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private BlockProcess setBlocks(com.sk89q.worldedit.world.World bukkitWorld, List<Pair<Set<BlockVector3>, Pattern>> patternList) {
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        BukkitTask task = Bukkit.getScheduler().runTask(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .maxBlocks(getMaxBlocks())
                    .build()
            ) {
                for (Pair<Set<BlockVector3>, Pattern> pair : patternList) {
                    for (BlockVector3 blockVector : pair.getKey()) {
                        session.setBlock(blockVector, pair.getValue());
                    }
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
                    .maxBlocks(getMaxBlocks())
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
    public BlockProcess setRandomBlocks(World world, PositionIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        if (world == null) return BlockProcess.COMPLETED;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        RandomPattern randomPattern = createRandomPattern(probabilityCollection);
        return setBlocks(bukkitWorld, Collections.singletonList(Pair.of(blockVectors, randomPattern)));
    }

    @Override
    public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        if (world == null) return BlockProcess.COMPLETED;
        return setBlocks(world, blockBox, createRandomPattern(probabilityCollection));
    }

    @Override
    public BlockProcess setBlocks(World world, PositionIterator iterator, XMaterial material) {
        if (world == null) return BlockProcess.COMPLETED;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        BlockState blockState = Objects.requireNonNull(BukkitAdapter.asBlockType(material.parseMaterial())).getDefaultState();
        return setBlocks(bukkitWorld, Collections.singletonList(Pair.of(blockVectors, blockState)));
    }

    @Override
    public BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
        return setBlocks(world, blockBox, Objects.requireNonNull(BukkitAdapter.asBlockType(material.parseMaterial())).getDefaultState());
    }

    @Override
    public void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
        if (world == null) return;
        BlockState blockState = Objects.requireNonNull(BukkitAdapter.asBlockType(material.parseMaterial())).getDefaultState();
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .build()
        ) {
            for (BlockVector3 blockVector : blockVectors) {
                session.setBlock(blockVector, blockState);
            }
        } catch (MaxChangedBlocksException e) {
            plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
        }
    }

    @Override
    public void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
        if (world == null) return;
        BlockState blockState = Objects.requireNonNull(BukkitAdapter.asBlockType(material.parseMaterial())).getDefaultState();
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        CuboidRegion region = new CuboidRegion(
                bukkitWorld,
                BlockVector3.at(blockBox.minX, blockBox.minY, blockBox.minZ),
                BlockVector3.at(blockBox.maxX, blockBox.maxY, blockBox.maxZ)
        );
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .build()
        ) {
            session.setBlocks(region, blockState);
        } catch (MaxChangedBlocksException e) {
            plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
        }
    }

    @Override
    public BlockProcess setBlocks(World world, Map<XMaterial, Collection<Position>> blockMap) {
        if (world == null || blockMap.isEmpty()) return BlockProcess.COMPLETED;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        List<Pair<Set<BlockVector3>, Pattern>> patternList = new ArrayList<>();
        for (Map.Entry<XMaterial, Collection<Position>> entry : blockMap.entrySet()) {
            Set<BlockVector3> blockVectors = createBlockVectors(entry.getValue().iterator());
            Pattern pattern = Objects.requireNonNull(BukkitAdapter.asBlockType(entry.getKey().parseMaterial())).getDefaultState();
            patternList.add(Pair.of(blockVectors, pattern));
        }
        return setBlocks(bukkitWorld, patternList);
    }

    @Override
    public void setBlocksFast(World world, Map<XMaterial, Collection<Position>> blockMap) {
        if (world == null || blockMap.isEmpty()) return;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .build()
        ) {
            for (Map.Entry<XMaterial, Collection<Position>> entry : blockMap.entrySet()) {
                BlockState blockState = Objects.requireNonNull(BukkitAdapter.asBlockType(entry.getKey().parseMaterial())).getDefaultState();
                for (Position position : entry.getValue()) {
                    session.setBlock(BlockVector3.at(position.x, position.y, position.z), blockState);
                }
            }
        } catch (MaxChangedBlocksException e) {
            plugin.getLogger().warning("Max blocks exceeded. The process will be stopped");
        }
    }
}
