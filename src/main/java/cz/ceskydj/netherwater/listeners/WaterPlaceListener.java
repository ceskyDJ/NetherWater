package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class WaterPlaceListener implements Listener {
    private final NetherWater plugin;
    private final MessageManager messageManager;

    public WaterPlaceListener(NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.messageManager.dump("Player interact event has been handled.");
        this.messageManager.dump("- Action: " + event.getAction());
        this.messageManager.dump("- Item: " + (event.getItem() != null ? event.getItem().getType() : "NULL"));
        this.messageManager.dump("- World: " + Objects.requireNonNull(event.getClickedBlock()).getWorld().getName() + " (type: " + event.getClickedBlock().getWorld().getEnvironment() + ")");
        this.messageManager.dump("- Player: " + event.getPlayer());

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null || event.getItem().getType() != Material.WATER_BUCKET) {
            return;
        }

        Player player = event.getPlayer();
        Block selectedBlock = event.getClickedBlock().getRelative(event.getBlockFace());
        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.plugin.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Add water block
        event.getClickedBlock().getRelative(event.getBlockFace()).setType(Material.WATER);

        // Replace water bucket with empty one
        if (player.getGameMode() != GameMode.CREATIVE) {
            event.getItem().setType(Material.BUCKET);
        }
    }
}
