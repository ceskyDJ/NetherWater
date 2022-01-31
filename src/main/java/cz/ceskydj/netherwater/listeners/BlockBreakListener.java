package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

// Block break listener for deleting watched blocks from DB
public class BlockBreakListener implements Listener {
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final PermissionManager permissionManager;
    private final DB db;

    public BlockBreakListener(NetherWater plugin) {
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
        this.permissionManager = plugin.getPermissionManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        World world = block.getWorld();

        this.messageManager.dump("Block break event has been handled.");
        this.messageManager.dump("- World: " + world.getName() + " (type: " + world.getEnvironment() + ")");
        this.messageManager.dump("- Player: " + player);

        if (world.getEnvironment() != org.bukkit.World.Environment.NETHER) {
            return;
        }

        if (this.configManager.isDisabledWorld(world) && !this.permissionManager.hasPermission("world.bypass", player)) {
            return;
        }

        if (block.getType() == Material.CAULDRON) {
            this.db.deleteWaterBlock(block);
        } else if ( block.getType() == Material.DISPENSER) {
            this.db.deleteDispenser(block);
        }
    }
}
