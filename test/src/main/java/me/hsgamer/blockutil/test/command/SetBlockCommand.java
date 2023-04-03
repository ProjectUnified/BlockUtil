package me.hsgamer.blockutil.test.command;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.blockutil.test.BlockUtilTest;
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
import java.util.Optional;
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

        ProbabilityCollection<XMaterial> materialCollection = new ProbabilityCollection<>();
        for (String material : args) {
            Optional<XMaterial> optionalXMaterial = XMaterial.matchXMaterial(material);
            if (!optionalXMaterial.isPresent()) {
                player.sendMessage("Invalid material: " + material);
                return true;
            }
            XMaterial xMaterial = optionalXMaterial.get();
            Material bukkitMaterial = xMaterial.parseMaterial();
            if (bukkitMaterial == null || !bukkitMaterial.isBlock()) {
                player.sendMessage("Invalid material: " + material);
                return true;
            }
            materialCollection.add(xMaterial, 1);
        }

        World world = pos1.getWorld();
        BlockBox blockBox = new BlockBox(BukkitBlockAdapter.adapt(pos1), BukkitBlockAdapter.adapt(pos2), false);
        BlockProcess blockProcess = plugin.getBlockHandler().setRandomBlocks(world, blockBox, materialCollection);
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
            return Arrays.stream(XMaterial.VALUES)
                    .map(XMaterial::name)
                    .filter(name -> name.toLowerCase().startsWith(arg.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
