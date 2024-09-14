package io.github.projectunified.blockutil.test.command;

import io.github.projectunified.blockutil.test.BlockUtilTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Pos1Command extends Command {
    private final BlockUtilTest plugin;

    public Pos1Command(BlockUtilTest plugin) {
        super("pos1", "Set the first position", "/pos1", Collections.emptyList());
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            plugin.setPos1(player.getUniqueId(), player.getLocation());
            player.sendMessage("Set the first position");
        } else {
            commandSender.sendMessage("You must be a player");
        }
        return true;
    }
}
