package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;

public class WaterCreateListener implements Listener {
    private final NetherWater plugin;
    private final ConfigManager configManager;

    public WaterCreateListener(NetherWater plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
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

        this.plugin.dump("Fluid level change event has been handled.");
        this.plugin.dump("- World: " + block.getWorld().getName());
        this.plugin.dump("- Block: " + block.getType().name());
        this.plugin.dump("- Metadata: " + block.getBlockData().getAsString(true));

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
                numberOfWaterBlocks++;
            }
        }

        return numberOfWaterBlocks >= 2;
    }
}
