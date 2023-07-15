package me.hsgamer.blockutil.fawe;

import com.cryptomorin.xseries.XMaterial;
import com.fastasyncworldedit.core.util.TaskManager;
import com.lewdev.probabilitylib.ProbabilityCollection;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FaweBlockHandler implements BlockHandler {
    public static boolean isAvailable() {
        return XMaterial.supports(16) && Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null && BlockHandlerSettings.getBoolean("use-fawe", true);
    }

    private RandomPattern createRandomPattern(ProbabilityCollection<XMaterial> probabilityCollection) {
        RandomPattern randomPattern = new RandomPattern();
        probabilityCollection.iterator().forEachRemaining(element -> {
            Material material = element.getObject().parseMaterial();
            if (material != null) {
                randomPattern.add(BukkitAdapter.asBlockType(material), element.getProbability());
            }
        });
        return randomPattern;
    }

    private Set<BlockVector3> createBlockVectors(Iterator<Position> iterator) {
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
        EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .fastMode(true)
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build();
        TaskManager.taskManager().async(() -> {
            try (session) {
                for (Pair<Set<BlockVector3>, Pattern> pair : patternList) {
                    session.setBlocks(pair.getKey(), pair.getValue());
                }
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
                session.cancel();
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
        EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .fastMode(true)
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build();
        TaskManager.taskManager().async(() -> {
            try (session) {
                session.setBlocks((Region) region, pattern);
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
                session.cancel();
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
        return setBlocks(bukkitWorld, Collections.singletonList(Pair.of(blockVectors, BukkitAdapter.asBlockType(material.parseMaterial()))));
    }

    @Override
    public BlockProcess setBlocks(World world, BlockBox blockBox, XMaterial material) {
        return setBlocks(world, blockBox, BukkitAdapter.asBlockType(material.parseMaterial()));
    }

    @Override
    public void setBlocksFast(World world, PositionIterator iterator, XMaterial material) {
        if (world == null) return;
        BlockType blockType = BukkitAdapter.asBlockType(material.parseMaterial());
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        Set<BlockVector3> blockVectors = createBlockVectors(iterator);
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .fastMode(true)
                .forceWNA()
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build()
        ) {
            session.setBlocks(blockVectors, blockType);
        }
    }

    @Override
    public void setBlocksFast(World world, BlockBox blockBox, XMaterial material) {
        if (world == null) return;
        BlockType blockType = BukkitAdapter.asBlockType(material.parseMaterial());
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        CuboidRegion region = new CuboidRegion(
                bukkitWorld,
                BlockVector3.at(blockBox.minX, blockBox.minY, blockBox.minZ),
                BlockVector3.at(blockBox.maxX, blockBox.maxY, blockBox.maxZ)
        );
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .fastMode(true)
                .forceWNA()
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build()
        ) {
            session.setBlocks((Region) region, blockType);
        }
    }

    @Override
    public BlockProcess setBlocks(World world, Map<XMaterial, Collection<Position>> blockMap) {
        if (world == null || blockMap.isEmpty()) return BlockProcess.COMPLETED;
        List<Pair<Set<BlockVector3>, Pattern>> patternList = new ArrayList<>();
        for (Map.Entry<XMaterial, Collection<Position>> entry : blockMap.entrySet()) {
            Pattern pattern = BukkitAdapter.asBlockType(entry.getKey().parseMaterial());
            Set<BlockVector3> blockVectors = createBlockVectors(entry.getValue().iterator());
            patternList.add(Pair.of(blockVectors, pattern));
        }
        return setBlocks(BukkitAdapter.adapt(world), patternList);
    }

    @Override
    public void setBlocksFast(World world, Map<XMaterial, Collection<Position>> blockMap) {
        if (world == null || blockMap.isEmpty()) return;
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .maxBlocks(getMaxBlocks())
                .fastMode(true)
                .forceWNA()
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build()
        ) {
            for (Map.Entry<XMaterial, Collection<Position>> entry : blockMap.entrySet()) {
                Pattern pattern = BukkitAdapter.asBlockType(entry.getKey().parseMaterial());
                Set<BlockVector3> blockVectors = createBlockVectors(entry.getValue().iterator());
                session.setBlocks(blockVectors, pattern);
            }
        }
    }
}
