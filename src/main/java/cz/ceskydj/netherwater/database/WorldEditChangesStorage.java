package cz.ceskydj.netherwater.database;

import org.bukkit.block.Block;

import java.util.*;

public class WorldEditChangesStorage {
    private final List<Block> savedBlockChanges;

    public WorldEditChangesStorage() {
        this.savedBlockChanges = new ArrayList<>();
    }

    public List<Block> getBlockChanges() {
        return this.savedBlockChanges;
    }

    public void addBlockChange(Block block) {
        this.savedBlockChanges.add(block);
    }

    public void clearBlockChanges() {
        this.savedBlockChanges.clear();
    }
}
