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

        this.messageManager.dump(blocks.size() + " will be animated");

        blocks.forEach(block -> {
            // Only generate animation for top-level water blocks with air above them
            Material upperBlockType = block.getRelative(BlockFace.UP).getType();
            if (upperBlockType != Material.AIR && upperBlockType != Material.CAVE_AIR) {
                return;
            }

            block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
        });
    }
}
