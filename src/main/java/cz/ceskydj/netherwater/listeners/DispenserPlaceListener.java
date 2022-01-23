package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class DispenserPlaceListener implements Listener {
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final DB db;

    public DispenserPlaceListener(NetherWater plugin) {
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();

        this.messageManager.dump("Block place event has been handled.");
        this.messageManager.dump("- Item: " + item.getType());
        this.messageManager.dump("- Block: " + block.getBlockData().getAsString());
        this.messageManager.dump("- World: " + block.getWorld().getName() + " (type: " + block.getWorld().getEnvironment() + ")");
        this.messageManager.dump("- Player: " + player);

        if (item.getType() != Material.DISPENSER) {
            return;
        }

        World world = block.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (this.configManager.getDisabledWorlds().contains(world.getName()) && !player.hasPermission("netherwater.world.bypass")) {
            return;
        }

        this.db.insertDispenser(block, player);
    }
}
