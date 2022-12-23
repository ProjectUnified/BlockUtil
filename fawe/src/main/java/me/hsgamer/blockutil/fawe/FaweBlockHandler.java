package me.hsgamer.blockutil.fawe;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.hsgamer.blockutil.abstraction.BlockHandler;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.hscore.bukkit.block.box.BlockBox;
import me.hsgamer.hscore.bukkit.block.iterator.VectorIterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FaweBlockHandler implements BlockHandler {
    private final Plugin plugin;

    public FaweBlockHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private BlockProcess setBlocks(com.sk89q.worldedit.world.World bukkitWorld, Set<BlockVector3> blockVectors, Pattern pattern) {
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
                session.setBlocks(blockVectors, pattern);
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
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
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
                task.cancel();
            }
        };
    }

    @Override
    public BlockProcess setRandomBlocks(World world, VectorIterator iterator, ProbabilityCollection<XMaterial> probabilityCollection) {
        Set<BlockVector3> blockVectors = new HashSet<>();
        com.sk89q.worldedit.world.World bukkitWorld = null;
        while (iterator.hasNext()) {
            Location location = iterator.nextLocation(world);
            if (bukkitWorld == null) {
                bukkitWorld = BukkitAdapter.adapt(location.getWorld());
            }
            blockVectors.add(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
        }
        if (bukkitWorld == null) {
            return new BlockProcess() {
                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public void cancel() {
                    // EMPTY
                }
            };
        }
        RandomPattern randomPattern = new RandomPattern();
        probabilityCollection.iterator().forEachRemaining(element -> {
            Material material = element.getObject().parseMaterial();
            if (material != null) {
                randomPattern.add(BukkitAdapter.asBlockType(material), element.getProbability());
            }
        });
        return setBlocks(bukkitWorld, blockVectors, randomPattern);
    }

    @Override
    public BlockProcess setRandomBlocks(World world, BlockBox blockBox, ProbabilityCollection<XMaterial> probabilityCollection) {
        RandomPattern randomPattern = new RandomPattern();
        probabilityCollection.iterator().forEachRemaining(element -> {
            Material material = element.getObject().parseMaterial();
            if (material != null) {
                randomPattern.add(BukkitAdapter.asBlockType(material), element.getProbability());
            }
        });
        return setBlocks(world, blockBox, randomPattern);
    }

    @Override
    public BlockProcess clearBlocks(World world, VectorIterator iterator) {
        Set<BlockVector3> blockVectors = new HashSet<>();
        com.sk89q.worldedit.world.World bukkitWorld = null;
        while (iterator.hasNext()) {
            Location location = iterator.nextLocation(world);
            if (bukkitWorld == null) {
                bukkitWorld = BukkitAdapter.adapt(location.getWorld());
            }
            blockVectors.add(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
        }
        if (bukkitWorld == null) {
            return new BlockProcess() {
                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public void cancel() {
                    // EMPTY
                }
            };
        }
        return setBlocks(bukkitWorld, blockVectors, BukkitAdapter.asBlockType(Material.AIR));
    }

    @Override
    public BlockProcess clearBlocks(World world, BlockBox blockBox) {
        return setBlocks(world, blockBox, BukkitAdapter.asBlockType(Material.AIR));
    }

    @Override
    public void clearBlockFast(World world, VectorIterator iterator) {
        Set<BlockVector3> blockVectors = new HashSet<>();
        com.sk89q.worldedit.world.World bukkitWorld = null;
        while (iterator.hasNext()) {
            Location location = iterator.nextLocation(world);
            if (bukkitWorld == null) {
                bukkitWorld = BukkitAdapter.adapt(location.getWorld());
            }
            blockVectors.add(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
        }
        if (bukkitWorld == null) {
            return;
        }
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(bukkitWorld)
                .fastMode(true)
                .forceWNA()
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build()
        ) {
            session.setBlocks(blockVectors, BukkitAdapter.asBlockType(Material.AIR));
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
                .fastMode(true)
                .forceWNA()
                .changeSetNull()
                .limitUnlimited()
                .compile()
                .build()
        ) {
            session.setBlocks((Region) region, BukkitAdapter.asBlockType(Material.AIR));
        }
    }
}
