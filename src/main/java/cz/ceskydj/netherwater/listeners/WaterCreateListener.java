package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;

public class WaterCreateListener implements Listener {
    private final NetherWater plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public WaterCreateListener(NetherWater plugin) {
        this.plugin = plugin;

        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFromTo(BlockFromToEvent event) {
        Block sourceBlock = event.getBlock();
        Block newBlock = event.getToBlock();

        World world = newBlock.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (this.configManager.getDisabledWorlds().contains(world.getName())) {
            return;
        }

        if (sourceBlock.getType() != Material.WATER || newBlock.getType() != Material.AIR) {
            return;
        }

        if (!this.configManager.areInfiniteSourcesDisabled()) {
            return;
        }

        if (!this.isSurroundedByWater(newBlock)) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Remove water (set flow water instead of water source block)
        Levelled flowingWater = (Levelled) Material.WATER.createBlockData();
        flowingWater.setLevel(1);

        newBlock.setBlockData(flowingWater);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFluidLevelChange(FluidLevelChangeEvent event) {
        Block block = event.getBlock();

        this.messageManager.dump("Fluid level change event has been handled.");
        this.messageManager.dump("- World: " + block.getWorld().getName());
        this.messageManager.dump("- Block: " + block.getType().name());
        this.messageManager.dump("- Metadata: " + block.getBlockData().getAsString(true));

        World world = block.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (this.configManager.getDisabledWorlds().contains(world.getName())) {
            return;
        }

        if (!this.configManager.areInfiniteSourcesDisabled()) {
            return;
        }

        if (!this.isSurroundedByWater(block)) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);
    }

    private boolean isSurroundedByWater(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

        int numberOfWaterBlocks = 0;
        for (BlockFace face : faces) {
            Block testedBlock = block.getRelative(face, 1);
            if (testedBlock.getType() == Material.WATER) {
                // Count only still water blocks (with level 0)
                Levelled water = (Levelled) testedBlock.getBlockData();
                if (water.getLevel() == 0) {
                    numberOfWaterBlocks++;
                }
            }
        }

        return numberOfWaterBlocks >= 2;
    }
}
