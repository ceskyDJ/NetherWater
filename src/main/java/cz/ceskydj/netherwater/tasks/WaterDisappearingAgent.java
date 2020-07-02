package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.util.List;

public class WaterDisappearingAgent implements Runnable {
    private final NetherWater plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final DB db;

    public WaterDisappearingAgent(NetherWater plugin) {
        this.plugin = plugin;

        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @Override
    public void run() {
        this.messageManager.dump("Checking for water blocks for disappearing...");

        int disappearingTime = this.configManager.getWaterDisappearingTime();
        List<Block> blocksForDisappearing = this.db.getWaterBlocksForDisappearing(disappearingTime);

        this.messageManager.dump(blocksForDisappearing.size() + " blocks for disappearing have been found");

        blocksForDisappearing.forEach(block -> {
            this.db.deleteWaterBlock(block);

            if (block.getType() == Material.WATER) {
                // Remove water (set flow water instead of water source block)
                Levelled flowingWater = (Levelled) Material.WATER.createBlockData();
                flowingWater.setLevel(1);

                block.setBlockData(flowingWater);
            }
        });
    }
}