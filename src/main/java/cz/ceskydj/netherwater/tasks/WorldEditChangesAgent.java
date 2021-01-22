package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.database.WorldEditChangesStorage;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

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

        List<Block> cachedBlocks = worldEditChangesStorage.getBlockChanges();

        Comparator<Block> comparator = Comparator.comparing(
                Block::getLocation, (loc1, loc2) -> loc1.toVector().equals(loc2.toVector()) ? 0 : 1
        );
        cachedBlocks = cachedBlocks.stream().collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparator)),
                        ArrayList::new));

        // There is no changes to process
        if (cachedBlocks.isEmpty()) {
            return;
        }

        this.messageManager.dump("Found " + cachedBlocks.size() + " WorldEdit changes.");

        List<Block> forDeletions = new ArrayList<>();
        cachedBlocks.forEach(block -> {
            if (block.getBlockData().getMaterial() != Material.WATER) {
                forDeletions.add(block);
            }
        });
        cachedBlocks.removeAll(forDeletions);

        this.db.deleteMultipleWaterBlocks(forDeletions);
        this.db.insertMultipleWaterBlocks(cachedBlocks, WaterSource.WORLD_EDIT, false);

        this.worldEditChangesStorage.clearBlockChanges();
    }
}
