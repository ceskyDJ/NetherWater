package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Objects;

public class BlockBreakListener implements Listener {
    private final NetherWater plugin;
    private final MessageManager messageManager;
    private final DB db;

    public BlockBreakListener(NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
        this.db = plugin.getDatabaseWrapper();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        this.messageManager.dump("Block break event has been handled.");
        this.messageManager.dump("- World: " + Objects.requireNonNull(block).getWorld().getName() + " (type: " + event.getBlock().getWorld().getEnvironment() + ")");
        this.messageManager.dump("- Player: " + player);

        if (block.getType() != Material.ICE) {
            return;
        }

        // Silk touch has different behaviour
        if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        // Ice to water change doesn't apply to creative game players
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!plugin.canBeUsedThisPlugin(player, block)) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Replace ice for watter block
        event.getBlock().setType(Material.WATER);

        if (!player.hasPermission("netherwater.disappearing.bypass")) {
            this.db.insertWaterBlock(block, WaterSource.ICE);
        }
    }
}
