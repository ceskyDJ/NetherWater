package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.database.WorldEditChangesStorage;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.block.Block;

import java.util.*;

public class WorldEditChangesAgent implements Runnable {
    private final DB db;
    private final WorldEditChangesStorage worldEditChangesStorage;
    private final MessageManager messageManager;

    public WorldEditChangesAgent(NetherWater plugin) {
        this.db = plugin.getDatabaseWrapper();
        this.worldEditChangesStorage = plugin.getWorldEditChangesStorage();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public void run() {
        this.messageManager.dump("Checking for WorldEdit changes...");

        Set<Block> addedBlocks = this.worldEditChangesStorage.getAddedWaterBlocks();
        Set<Block> removedBlocks = this.worldEditChangesStorage.getRemovedWaterBlocks();

        // There is no changes to process
        if (addedBlocks.isEmpty() && removedBlocks.isEmpty()) {
            return;
        }

        this.messageManager.dump("Found " + (addedBlocks.size() + removedBlocks.size()) + " WorldEdit changes.");

        this.db.deleteMultipleWaterBlocks(removedBlocks);
        this.db.insertMultipleWaterBlocks(addedBlocks, WaterSource.WORLD_EDIT, false);

        this.worldEditChangesStorage.clearBlockChanges();
    }
}
