package io.github.projectunified.blockutil.test;

import io.github.projectunified.blockutil.api.BlockHandler;
import io.github.projectunified.blockutil.fawe.FaweBlockHandler;
import io.github.projectunified.blockutil.test.command.Pos1Command;
import io.github.projectunified.blockutil.test.command.Pos2Command;
import io.github.projectunified.blockutil.test.command.SetBlockCommand;
import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BlockUtilTest extends BasePlugin {
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    private final BlockHandler blockHandler = new FaweBlockHandler();

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
    public void enable() {
        getLogger().info("Handler: " + getBlockHandler().getClass());
        registerCommand(new Pos1Command(this));
        registerCommand(new Pos2Command(this));
        registerCommand(new SetBlockCommand(this));
    }
}
