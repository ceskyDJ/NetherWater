package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Objects;

public class BlockBreakListener implements Listener {
    private final NetherWater plugin;

    public BlockBreakListener(NetherWater plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        this.plugin.dump("Block break event has been handled.");
        this.plugin.dump("- World: " + Objects.requireNonNull(event.getBlock()).getWorld().getName() + " (type: " + event.getBlock().getWorld().getEnvironment() + ")");
        this.plugin.dump("- Player: " + event.getPlayer());

        Player player = event.getPlayer();

        if (event.getBlock().getType() != Material.ICE) {
            return;
        }

        // Silk touch has different behaviour
        if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }

        // Ice to water change doesn't apply to creative game players
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!plugin.canBeUsedThisPlugin(player, event.getBlock())) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Replace ice for watter block
        event.getBlock().setType(Material.WATER);
    }
}
