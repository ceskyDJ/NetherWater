package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WaterReplaceListener implements Listener {
    private final NetherWater plugin;
    private final DB db;

    public WaterReplaceListener(NetherWater plugin) {
        this.plugin = plugin;

        this.db = plugin.getDatabaseWrapper();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block selectedBlock = event.getClickedBlock().getRelative(event.getBlockFace());

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (selectedBlock.getType() != Material.WATER) {
            return;
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.plugin.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        this.db.deleteWaterBlock(selectedBlock);
    }
}
