package com.leetzilantonis.netherwater;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class WaterFlowListener implements Listener {
    private final NetherWater plugin;

    public WaterFlowListener(NetherWater plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFromTo(BlockFromToEvent event) {
        Block source = event.getBlock();
        Block destination = event.getToBlock();
        BlockFace face = event.getFace();

        this.plugin.dump("Block from to event has been handled.");
        this.plugin.dump("- World: " + source.getWorld().getName());
        this.plugin.dump("- Source block: " + source.getType().name());
        this.plugin.dump("- New block: " + destination.getType().name());
        this.plugin.dump("- Source metadata: " + source.getBlockData().getAsString(true));
        this.plugin.dump("- New block metadata: " + destination.getBlockData().getAsString(true));
        this.plugin.dump("- Face: " + face.name());

        if (source.getWorld().getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (source.getType() != Material.WATER || destination.getType() != Material.AIR) {
            return;
        }

        int waterLevel = Integer.parseInt(String.valueOf(source.getBlockData().getAsString().charAt(22)));

        if (waterLevel == 0 && face == BlockFace.DOWN) {
            event.setCancelled(true);
            return;
        }

        if (waterLevel == 8 && face == BlockFace.DOWN) {
            event.setCancelled(true);
        }
    }
}
