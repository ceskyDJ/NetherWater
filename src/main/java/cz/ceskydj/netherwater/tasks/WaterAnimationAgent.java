package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

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
            if (block.getType() == Material.WATER) {
                block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
            }
        });
    }
}
