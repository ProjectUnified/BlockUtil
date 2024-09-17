package io.github.projectunified.blockutil.fawe;

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
import com.sk89q.worldedit.world.block.BlockState;
import io.github.projectunified.blockutil.api.*;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import me.hsgamer.hscore.minecraft.block.box.Position;
import me.hsgamer.hscore.minecraft.block.iterator.PositionIterator;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FaweBlockHandler implements BlockHandler {
    private int maxBlocks = -1;

    public static boolean isAvailable() {
        return Version.isAtLeast(16) && Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }

    private BlockState toBlockState(BlockData blockData) {
        if (blockData.state == null) {
            return Objects.requireNonNull(BukkitAdapter.asBlockType(blockData.material)).getDefaultState();
        } else {
            org.bukkit.block.data.BlockData bukkitBlockData = Bukkit.createBlockData(blockData.material, blockData.state);
            return BukkitAdapter.adapt(bukkitBlockData);
        }
    }

    private RandomPattern createRandomPattern(ProbabilityCollection<BlockData> probabilityCollection) {
        RandomPattern randomPattern = new RandomPattern();
        probabilityCollection.iterator().forEachRemaining(element -> randomPattern.add(toBlockState(element.getObject()), element.getProbability()));
        return randomPattern;
    }

    private Set<BlockVector3> createBlockVectors(Iterator<Position> iterator) {
        Set<BlockVector3> blockVectors = new HashSet<>();
        iterator.forEachRemaining(position -> blockVectors.add(BlockVector3.at(position.x, position.y, position.z)));
        return blockVectors;
    }

    private BlockProcess runSession(com.sk89q.worldedit.world.World world, Consumer<EditSession> editSessionConsumer, boolean urgent) {
        AtomicReference<EditSession> sessionRef = new AtomicReference<>();
        Runnable runnable = () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(world)
                    .maxBlocks(maxBlocks)
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()) {
                sessionRef.set(session);
                editSessionConsumer.accept(session);
            }
        };
        if (urgent) {
            runnable.run();
            return BlockProcess.COMPLETED;
        } else {
            CompletableFuture<Void> blockFuture = new CompletableFuture<>();
            TaskManager.taskManager().async(() -> {
                try {
                    runnable.run();
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
                    blockFuture.cancel(true);
                    EditSession session = sessionRef.get();
                    if (session != null) {
                        session.cancel();
                    }
                }
            };
        }
    }

    private BlockProcess setBlocks(World world, List<Pair<Set<BlockVector3>, Pattern>> patternList, boolean urgent) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        return runSession(bukkitWorld, session -> {
            for (Pair<Set<BlockVector3>, Pattern> pair : patternList) {
                session.setBlocks(pair.key, pair.value);
            }
        }, urgent);
    }

    private BlockProcess setBlocks(World world, BlockBox blockBox, Pattern pattern, boolean urgent) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        CuboidRegion region = new CuboidRegion(
                bukkitWorld,
                BlockVector3.at(blockBox.minX, blockBox.minY, blockBox.minZ),
                BlockVector3.at(blockBox.maxX, blockBox.maxY, blockBox.maxZ)
        );
        return runSession(BukkitAdapter.adapt(world), session -> session.setBlocks((Region) region, pattern), urgent);
    }

    public FaweBlockHandler setMaxBlocks(int maxBlocks) {
        this.maxBlocks = maxBlocks;
        return this;
    }

    @Override
    public BlockProcess setBlock(World world, PositionIterator iterator, BlockData blockData, boolean urgent) {
        return setBlocks(world, Collections.singletonList(new Pair<>(createBlockVectors(iterator), toBlockState(blockData))), urgent);
    }

    @Override
    public BlockProcess setBlock(World world, BlockBox blockBox, BlockData blockData, boolean urgent) {
        return setBlocks(world, blockBox, toBlockState(blockData), urgent);
    }

    @Override
    public BlockProcess setBlock(World world, PositionIterator iterator, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent) {
        return setBlocks(world, Collections.singletonList(new Pair<>(createBlockVectors(iterator), createRandomPattern(probabilityCollection))), urgent);
    }

    @Override
    public BlockProcess setBlock(World world, BlockBox blockBox, ProbabilityCollection<BlockData> probabilityCollection, boolean urgent) {
        return setBlocks(world, blockBox, createRandomPattern(probabilityCollection), urgent);
    }

    @Override
    public BlockProcess setBlock(World world, PositionIterator iterator, Supplier<BlockData> blockDataSupplier, boolean urgent) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        return runSession(bukkitWorld, session -> {
            while (iterator.hasNext()) {
                Position position = iterator.next();
                int x = (int) Math.floor(position.x);
                int y = (int) Math.floor(position.y);
                int z = (int) Math.floor(position.z);
                BlockData blockData = blockDataSupplier.get();
                BlockState blockState = toBlockState(blockData);
                session.setBlock(x, y, z, blockState);
            }
        }, urgent);
    }

    @Override
    public BlockProcess setBlock(World world, BlockBox blockBox, Supplier<BlockData> blockDataSupplier, boolean urgent) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        return runSession(bukkitWorld, session -> {
            for (int x = blockBox.minX; x <= blockBox.maxX; x++) {
                for (int y = blockBox.minY; y <= blockBox.maxY; y++) {
                    for (int z = blockBox.minZ; z <= blockBox.maxZ; z++) {
                        BlockData blockData = blockDataSupplier.get();
                        BlockState blockState = toBlockState(blockData);
                        session.setBlock(x, y, z, blockState);
                    }
                }
            }
        }, urgent);
    }

    @Override
    public BlockProcess setBlock(World world, List<Pair<Position, BlockData>> blocks, boolean urgent) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        return runSession(bukkitWorld, session -> {
            Map<BlockData, BlockState> blockStateMap = new HashMap<>();
            Function<BlockData, BlockState> blockStateFunction = blockData -> blockStateMap.computeIfAbsent(blockData, this::toBlockState);
            for (Pair<Position, BlockData> pair : blocks) {
                Position position = pair.key;
                int x = (int) Math.floor(position.x);
                int y = (int) Math.floor(position.y);
                int z = (int) Math.floor(position.z);
                BlockState blockState = blockStateFunction.apply(pair.value);
                session.setBlock(x, y, z, blockState);
            }
        }, urgent);
    }
}
