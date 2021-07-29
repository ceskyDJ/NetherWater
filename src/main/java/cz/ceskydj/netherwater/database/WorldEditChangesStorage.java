package cz.ceskydj.netherwater.database;

import org.bukkit.block.Block;

import java.util.*;

public class WorldEditChangesStorage {
    private final Set<Block> addedWaterBlocks;
    private final Set<Block> removedWaterBlocks;

    public WorldEditChangesStorage() {
        this.addedWaterBlocks = new LinkedHashSet<>();
        this.removedWaterBlocks = new LinkedHashSet<>();
    }

    public void addWaterBlock(Block block) {
        this.addedWaterBlocks.add(block);
    }

    public void removeWaterBlock(Block block) {
        this.removedWaterBlocks.add(block);
    }

    public void clearBlockChanges() {
        this.addedWaterBlocks.clear();
        this.removedWaterBlocks.clear();
    }

    public Set<Block> getAddedWaterBlocks() {
        return this.addedWaterBlocks;
    }

    public Set<Block> getRemovedWaterBlocks() {
        return this.removedWaterBlocks;
    }
}
