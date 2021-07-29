package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class WaterAnimationAgent implements Runnable {
    private final MessageManager messageManager;
    private final DB db;

    public WaterAnimationAgent(NetherWater plugin) {
        this.messageManager = plugin.getMessageManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @Override
    public void run() {
        this.messageManager.dump("Generating water animation effects...");

        List<Block> blocks = this.db.getAllWaterBlocks();

        Set<Block> nonWaterBlocks = new LinkedHashSet<>();
        AtomicInteger animated = new AtomicInteger();
        blocks.forEach(block -> {
            if (block.getType() != Material.WATER) {
                nonWaterBlocks.add(block);
                return;
            }

            // Only generate animation for top-level water blocks with air above them
            Material upperBlockType = block.getRelative(BlockFace.UP).getType();
            if (upperBlockType != Material.AIR && upperBlockType != Material.CAVE_AIR) {
                return;
            }

            block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
            animated.getAndIncrement();
        });

        this.messageManager.dump(animated + " water blocks has been animated");

        this.db.deleteMultipleWaterBlocks(nonWaterBlocks);
    }
}
