package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class WaterPlaceListener implements Listener {
    private final NetherWater plugin;
    private final MessageManager messageManager;
    private final DB db;

    public WaterPlaceListener(NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack usedItem = event.getItem();
        Block selectedBlock = event.getClickedBlock().getRelative(event.getBlockFace());

        this.messageManager.dump("Player interact event has been handled.");
        this.messageManager.dump("- Action: " + event.getAction());
        this.messageManager.dump("- Item: " + (usedItem != null ? usedItem.getType() : "NULL"));
        this.messageManager.dump("- World: " + Objects.requireNonNull(selectedBlock).getWorld().getName() + " (type: " + selectedBlock.getWorld().getEnvironment() + ")");
        this.messageManager.dump("- Player: " + player);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (usedItem == null || usedItem.getType() != Material.WATER_BUCKET) {
            return;
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.plugin.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Add water block
        selectedBlock.setType(Material.WATER);

        // Replace water bucket with empty one
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.getItem().setType(Material.BUCKET);
        }

        if (!player.hasPermission("netherwater.disappearing.bypass")) {
            this.db.insertWaterBlock(selectedBlock, WaterSource.BUCKET);
        }
    }
}
