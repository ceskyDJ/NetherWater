package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class WaterDispenseListener implements Listener {
    private final NetherWater plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final DB db;
    private final PermissionManager permissionManager;

    public WaterDispenseListener(NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
        this.db = plugin.getDatabaseWrapper();
        this.permissionManager = plugin.getPermissionManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        ItemStack item = event.getItem();

        boolean fromDb = false;
        OfflinePlayer player = this.plugin.getClosestPlayer(block.getLocation());
        if (player == null) {
            // Works automatically without player (chunk loaded redstone circuit etc.)
            // In this case, owner of the dispenser will be used
            player = this.db.getDispenserOwner(block);
            fromDb = true;
        }

        this.messageManager.dump("Block dispense event has been handled.");
        this.messageManager.dump("- World: " + world.getName());
        this.messageManager.dump("- Block: " + block.getType().name());
        this.messageManager.dump("- Block's metadata: " + block.getBlockData().getAsString(true));
        this.messageManager.dump("- Item: " + item.getType().name());
        this.messageManager.dump("- Item's metadata: " + item.getItemMeta());
        if (player != null) {
            this.messageManager.dump("- Responsible player: " + player.getName() + (fromDb ? " (from DB)" : ""));
        } else {
            this.messageManager.dump("- Responsible player: unknown (no closed player and dispensed isn't registered)");
        }

        if (block.getType() != Material.DISPENSER) {
            return;
        }

        if (item.getType() != Material.WATER_BUCKET && item.getType() != Material.BUCKET) {
            return;
        }

        if (world.getEnvironment() != org.bukkit.World.Environment.NETHER) {
            return;
        }

        if (player == null) {
            return;
        }

        if (!this.permissionManager.canBeUsedThisPlugin(player, block)) {
            return;
        }

        // event.getVelocity() + counting target block from vector could be used here,
        // but it isn't working properly when there isn't air block in front of dispenser
        Directional directional = (Directional) block.getBlockData();
        Block targetBlock = block.getRelative(directional.getFacing(), 1);
        this.messageManager.dump("- Target block: " + targetBlock);

        Waterlogged targetWaterlogged = null;
        if (targetBlock.getBlockData() instanceof Waterlogged) {
            targetWaterlogged = (Waterlogged) targetBlock.getBlockData();
        }

        if (item.getType() == Material.WATER_BUCKET) {
            // There must be an air in front of dispenser
            Material targetMaterial = targetBlock.getBlockData().getMaterial();
            if (targetMaterial != Material.AIR && targetMaterial != Material.CAVE_AIR && targetWaterlogged == null) {
                return;
            }

            // If water logged block, it must not be inside of water
            if (targetWaterlogged != null && targetWaterlogged.isWaterlogged()) {
                return;
            }

            // Cancel native event actions
            event.setCancelled(true);

            // Add water
            if (targetWaterlogged != null) {
                // Target block supports water logging (could be inside of water)
                // Set selected block as water logged
                targetWaterlogged.setWaterlogged(true);
                targetBlock.setBlockData(targetWaterlogged);
            } else {
                // Target block doesn't support water logging

                // Break not solid block naturally before replacing with water
                if (!targetBlock.getBlockData().getMaterial().isSolid()) {
                    targetBlock.breakNaturally();
                }

                targetBlock.setType(Material.WATER);
            }

            // Replace water bucket with empty one
            Dispenser dispenser = (Dispenser) block.getState();
            Inventory inventory = dispenser.getInventory();

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                int bucketPosition = inventory.first(Material.WATER_BUCKET);
                inventory.setItem(bucketPosition, new ItemStack(Material.BUCKET));
            });

            // If the player has bypass permission, the water block won't disappear
            boolean disappear = !this.permissionManager.hasPermission("disappearing.bypass", player, world);
            this.db.insertWaterBlock(targetBlock, WaterSource.DISPENSER, disappear);
        } else if (item.getType() == Material.BUCKET) {
            this.db.deleteWaterBlock(targetBlock);

            // There must be a water block in front of dispenser
            if (targetBlock.getBlockData().getMaterial() != Material.WATER && targetWaterlogged == null) {
                return;
            }

            // If water logged block, it must be inside of water
            if (targetWaterlogged != null && !targetWaterlogged.isWaterlogged()) {
                return;
            }

            // When scooping is enabled, just let Minecraft does the work (scoop water into bucket)
            if (!this.configManager.isScoopingDisabled() || this.permissionManager.hasPermission("scooping.bypass", player, world)) {
                return;
            }

            // Cancel native event actions
            event.setCancelled(true);

            // Remove water
            if (targetWaterlogged != null) {
                // Water logged block --> set not water logged (remove water around it)
                targetWaterlogged.setWaterlogged(false);

                targetBlock.setBlockData(targetWaterlogged);
            } else {
                // Water block --> set flow water instead of water source block
                Levelled flowingWater = (Levelled) Material.WATER.createBlockData();
                flowingWater.setLevel(1);

                targetBlock.setBlockData(flowingWater);
            }
        }
    }
}
