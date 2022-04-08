package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WaterReplaceListener implements Listener {
    private final DB db;
    private final PermissionManager permissionManager;
    private final MessageManager messageManager;

    public WaterReplaceListener(NetherWater plugin) {
        this.db = plugin.getDatabaseWrapper();
        this.permissionManager = plugin.getPermissionManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block eventBlock = event.getClickedBlock();
        Block selectedBlock;
        if (eventBlock != null) {
            selectedBlock = eventBlock.getRelative(event.getBlockFace());
        } else {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (selectedBlock.getType() != Material.WATER && eventBlock.getType() != Material.CAULDRON) {
            return;
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.permissionManager.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        this.messageManager.dump("Deleted water block with replacing event");
        this.db.deleteWaterBlock(selectedBlock);
    }
}
