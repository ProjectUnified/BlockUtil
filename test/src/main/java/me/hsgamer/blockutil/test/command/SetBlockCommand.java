package me.hsgamer.blockutil.test.command;

import com.cryptomorin.xseries.XMaterial;
import me.hsgamer.blockutil.abstraction.BlockProcess;
import me.hsgamer.blockutil.test.BlockUtilTest;
import me.hsgamer.hscore.bukkit.block.BukkitBlockAdapter;
import me.hsgamer.hscore.minecraft.block.box.BlockBox;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SetBlockCommand extends BlockCommand {
    public SetBlockCommand(BlockUtilTest plugin) {
        super(plugin, "setblocks", "setblocks <material>", "Set the blocks", Collections.emptyList());
    }

    @Override
    protected boolean execute(Player player, Location pos1, Location pos2, String label, String... args) {
        if (args.length < 1) {
            return false;
        }
        String material = args[0];
        Optional<XMaterial> optionalXMaterial = XMaterial.matchXMaterial(material);
        if (!optionalXMaterial.isPresent()) {
            player.sendMessage("Invalid material");
            return true;
        }
        XMaterial xMaterial = optionalXMaterial.get();

        World world = pos1.getWorld();
        BlockBox blockBox = new BlockBox(BukkitBlockAdapter.adapt(pos1), BukkitBlockAdapter.adapt(pos2), false);
        BlockProcess blockProcess = plugin.getBlockHandler().setBlocks(world, blockBox, xMaterial);
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
}
