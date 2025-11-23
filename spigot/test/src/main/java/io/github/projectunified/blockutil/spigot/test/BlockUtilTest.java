package io.github.projectunified.blockutil.spigot.test;

import io.github.projectunified.blockutil.spigot.common.BlockHandler;
import io.github.projectunified.blockutil.spigot.fawe.FaweBlockHandler;
import io.github.projectunified.blockutil.spigot.folia.FoliaBlockHandler;
import io.github.projectunified.blockutil.spigot.test.command.Pos1Command;
import io.github.projectunified.blockutil.spigot.test.command.Pos2Command;
import io.github.projectunified.blockutil.spigot.test.command.SetBlockCommand;
import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.command.CommandComponent;
import org.bukkit.Location;

import java.util.*;

public class BlockUtilTest extends BasePlugin {
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    private BlockHandler blockHandler;

    public Optional<Location> getPos1(UUID uuid) {
        return Optional.ofNullable(pos1Map.get(uuid));
    }

    public Optional<Location> getPos2(UUID uuid) {
        return Optional.ofNullable(pos2Map.get(uuid));
    }

    public void setPos1(UUID uuid, Location location) {
        pos1Map.put(uuid, location);
    }

    public void setPos2(UUID uuid, Location location) {
        pos2Map.put(uuid, location);
    }

    public BlockHandler getBlockHandler() {
        return blockHandler;
    }

    @Override
    protected List<Object> getComponents() {
        return Collections.singletonList(new CommandComponent(this,
                new Pos1Command(this),
                new Pos2Command(this),
                new SetBlockCommand(this)
        ));
    }

    @Override
    public void enable() {
        if (FoliaBlockHandler.isAvailable()) {
            blockHandler = new FoliaBlockHandler(this);
        } else if (FaweBlockHandler.isAvailable()) {
            blockHandler = new FaweBlockHandler();
        } else {
            getLogger().warning("No block handler found");
            return;
        }

        getLogger().info("Handler: " + getBlockHandler().getClass());
    }
}
