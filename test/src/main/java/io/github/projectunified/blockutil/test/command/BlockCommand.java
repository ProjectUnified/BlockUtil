package io.github.projectunified.blockutil.test.command;

import io.github.projectunified.blockutil.test.BlockUtilTest;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class BlockCommand extends Command {
    protected final BlockUtilTest plugin;

    protected BlockCommand(BlockUtilTest plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    protected BlockCommand(BlockUtilTest plugin, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.plugin = plugin;
    }

    protected abstract boolean execute(Player player, Location pos1, Location pos2, String label, String... args);

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can use this command");
            return true;
        }
        Player player = (Player) commandSender;
        Optional<Location> pos1 = plugin.getPos1(player.getUniqueId());
        Optional<Location> pos2 = plugin.getPos2(player.getUniqueId());
        if (!pos1.isPresent() || !pos2.isPresent()) {
            player.sendMessage("You need to set both positions");
            return true;
        }
        Location location1 = pos1.get();
        Location location2 = pos2.get();
        if (!Objects.equals(location1.getWorld(), location2.getWorld())) {
            player.sendMessage("The positions are not in the same world");
            return true;
        }
        return execute(player, location1, location2, s, strings);
    }
}
