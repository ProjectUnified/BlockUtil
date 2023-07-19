package me.hsgamer.blockutil.test.command;

import me.hsgamer.blockutil.abstraction.BlockHandlerSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SettingsCommand extends Command {
    public SettingsCommand() {
        super("blocksettings", "Set block settings", "/blocksettings <setting> <value>", Collections.emptyList());
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (strings.length < 2) {
            return false;
        }
        String setting = strings[0];
        String value = strings[1];
        BlockHandlerSettings.set(setting, value);
        commandSender.sendMessage("Set " + setting + " to " + value);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return Arrays.asList("blocks-per-tick", "block-delay");
        }
        return Collections.emptyList();
    }
}
