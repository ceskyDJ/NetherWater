package com.leetzilantonis.netherwater.listeners;

import com.leetzilantonis.netherwater.NetherWater;
import com.leetzilantonis.netherwater.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;

public class WaterFlowListener implements Listener {
    private final NetherWater plugin;
    private final ConfigManager configManager;

    public WaterFlowListener(NetherWater plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
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

        if (this.configManager.isSpreadBypassEnabled()) {
            Player player = this.plugin.getClosestPlayer(event.getToBlock().getLocation());
            if (player.hasPermission("netherwater.spread.bypass")) {
                return;
            }
        }

        if (source.getWorld().getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (source.getType() != Material.WATER || destination.getType() != Material.AIR) {
            return;
        }

        if (!this.configManager.isSpreadEnabled()) {
            event.setCancelled(true);
            return;
        }

        int waterLevel = Integer.parseInt(String.valueOf(source.getBlockData().getAsString().charAt(22)));

        if (waterLevel == 0 && face == BlockFace.DOWN) {
            event.setCancelled(true);
            return;
        }

        if (waterLevel == 8 && face == BlockFace.DOWN) {
            event.setCancelled(true);
            return;
        }

        if (waterLevel != 0 && !isWatterNearby(source)) {
            event.setCancelled(true);
            return;
        }

        if (waterLevel > 3) {
            event.setCancelled(true);
            return;
        }

        if (this.isOnlyAirOverAndUnder(source)) {
            event.setCancelled(true);
        }
    }

    private boolean isWatterNearby(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};

        for (BlockFace face : faces) {
            Block testedBlock = block.getRelative(face, 1);
            if (testedBlock.getType() == Material.WATER) {
                int waterLevel = Integer.parseInt(String.valueOf(testedBlock.getBlockData().getAsString().charAt(22)));

                if (waterLevel != 8 && waterLevel != 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isOnlyAirOverAndUnder(Block block) {
        for (int i = 1; i < 5; i++) {
            Block relativeBlock = block.getRelative(BlockFace.UP, i);
            if (relativeBlock.getType() != Material.AIR && relativeBlock.getType() != Material.WATER) {
                return false;
            }
        }

        for (int i = 1; i < 5; i++) {
            Block relativeBlock = block.getRelative(BlockFace.DOWN, i);
            if (relativeBlock.getType() != Material.AIR && relativeBlock.getType() != Material.WATER) {
                return false;
            }
        }

        return true;
    }
}
