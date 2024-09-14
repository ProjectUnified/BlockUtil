package io.github.projectunified.blockutil.test.command;

import com.lewdev.probabilitylib.ProbabilityCollection;
import io.github.projectunified.blockutil.api.BlockData;
import io.github.projectunified.blockutil.api.BlockProcess;
import io.github.projectunified.blockutil.test.BlockUtilTest;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SetBlockCommand extends BlockCommand {
    public SetBlockCommand(BlockUtilTest plugin) {
        super(plugin, "setblocks", "setblocks <material>", "Set the blocks", Collections.emptyList());
    }

    @Override
    protected boolean execute(Player player, Location pos1, Location pos2, String label, String... args) {
        if (args.length < 1) {
            return false;
        }

        ProbabilityCollection<BlockData> materialCollection = new ProbabilityCollection<>();
        for (String material : args) {
            Material bukkitMaterial = Material.matchMaterial(material);
            if (bukkitMaterial == null || !bukkitMaterial.isBlock()) {
                player.sendMessage("Invalid material: " + material);
                return true;
            }
            materialCollection.add(new BlockData(bukkitMaterial), 1);
        }

        World world = pos1.getWorld();
        BlockBox blockBox = new BlockBox(BukkitBlockAdapter.adapt(pos1), BukkitBlockAdapter.adapt(pos2));
        BlockProcess blockProcess = plugin.getBlockHandler().setBlock(world, blockBox, materialCollection, false);
        CompletableFuture.runAsync(() -> {
            while (!blockProcess.isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
            player.sendMessage("Done");
        });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length > 0) {
            String arg = args[args.length - 1];
            return Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(name -> name.toLowerCase().startsWith(arg.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
