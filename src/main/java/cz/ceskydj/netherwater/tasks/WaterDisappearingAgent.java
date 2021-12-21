package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.util.List;

public class WaterDisappearingAgent implements Runnable {
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final DB db;

    public WaterDisappearingAgent(NetherWater plugin) {
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @Override
    public void run() {
        this.messageManager.dump("Checking for water blocks for disappearing...");

        // Water blocks
        int disappearingTime = this.configManager.getWaterDisappearingTime();
        List<Block> blocksForDisappearing = this.db.getWaterBlocksForDisappearing(disappearingTime);

        this.messageManager.dump(blocksForDisappearing.size() + " blocks for disappearing have been found");

        blocksForDisappearing.forEach(block -> {
            String blockType = block.getType().toString();

            // Skip cauldrons, they will be processed later
            if (blockType.contains("CAULDRON")) {
                return;
            }

            this.db.deleteWaterBlock(block);

            if (block.getType() == Material.WATER) {
                // Remove water (set flow water instead of water source block)
                Levelled flowingWater = (Levelled) Material.WATER.createBlockData();
                flowingWater.setLevel(1);

                block.setBlockData(flowingWater);
            }
        });

        // Cauldrons
        if (!this.configManager.isCauldronDryingEnabled()) {
            return;
        }

        int dryingTime = this.configManager.getCauldronDryingTime();
        List<Block> cauldronsForDrying = this.db.getWaterBlocksForDisappearing(dryingTime);

        this.messageManager.dump(cauldronsForDrying.size() + " cauldrons for drying have been found");

        cauldronsForDrying.forEach(block -> {
            String blockType = block.getType().toString();

            // Water block should be already done it, so just to be sure...
            if (block.getType() == Material.WATER) {
                return;
            }

            // Every time the block is deleted but when the cauldron is not empty, it's inserted back
            this.db.deleteWaterBlock(block);

            if (blockType.contains("CAULDRON") && block.getBlockData() instanceof Levelled) {
                Levelled water_level = (Levelled) block.getBlockData();
                if (water_level.getLevel() == 1) {
                    // There is the last bit of water --> empty cauldron completely
                    block.setType(Material.CAULDRON);

                    return;
                } else {
                    // Cauldron isn't empty --> insert the block back to DB
                    this.db.insertWaterBlock(block, WaterSource.BUCKET, true);
                }

                // Remove one level of water from cauldron
                water_level.setLevel(water_level.getLevel() - 1);
                block.setBlockData(water_level);
            }
        });
    }
}