package me.hsgamer.blockutil.test.command;

import me.hsgamer.blockutil.test.BlockUtilTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Pos2Command extends Command {
    private final BlockUtilTest plugin;

    public Pos2Command(BlockUtilTest plugin) {
        super("pos2", "Set the second position", "/pos2", Collections.emptyList());
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            plugin.setPos2(player.getUniqueId(), player.getLocation());
            player.sendMessage("Set the second position");
        } else {
            commandSender.sendMessage("You must be a player");
        }
        return true;
    }
}
