package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
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

    public WaterDispenseListener(NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItem();

        this.messageManager.dump("Block dispense event has been handled.");
        this.messageManager.dump("- World: " + block.getWorld().getName());
        this.messageManager.dump("- Block: " + block.getType().name());
        this.messageManager.dump("- Block's metadata: " + block.getBlockData().getAsString(true));
        this.messageManager.dump("- Item: " + item.getType().name());
        this.messageManager.dump("- Item's metadata: " + item.getItemMeta().toString());

        if (block.getType() != Material.DISPENSER) {
            return;
        }

        if (item.getType() != Material.WATER_BUCKET && item.getType() != Material.BUCKET) {
            return;
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        Player player = this.plugin.getClosestPlayer(block.getLocation());
        if (!this.plugin.canBeUsedThisPlugin(player, block)) {
            return;
        }

        if (item.getType() == Material.WATER_BUCKET) {
            // Cancel native event actions
            event.setCancelled(true);

            // Add water block
            Vector vector = event.getVelocity();
            Block targetBlock = block.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            targetBlock.setType(Material.WATER);

            // Replace water bucket with empty one
            Dispenser dispenser = (Dispenser) block.getState();
            Inventory inventory = dispenser.getInventory();

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                @Override
                public void run() {
                    int bucketPosition = inventory.first(Material.WATER_BUCKET);
                    inventory.setItem(bucketPosition, new ItemStack(Material.BUCKET));
                }
            });
        } else if (item.getType() == Material.BUCKET) {
            if (!this.configManager.isScoopingDisabled() || player.hasPermission("netherwater.scooping.bypass")) {
                return;
            }

            // Cancel native event actions
            event.setCancelled(true);

            // Remove water block
            Vector vector = event.getVelocity();
            Block targetBlock = block.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

            Levelled flowingWater = (Levelled) Material.WATER.createBlockData();
            flowingWater.setLevel(1);

            targetBlock.setBlockData(flowingWater);
        }
    }
}
