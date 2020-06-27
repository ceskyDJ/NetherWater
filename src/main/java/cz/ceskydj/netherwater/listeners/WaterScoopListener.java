package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WaterScoopListener implements Listener {
    private final NetherWater plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public WaterScoopListener(NetherWater plugin) {
        this.plugin = plugin;

        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null || event.getItem().getType() != Material.BUCKET) {
            return;
        }

        Player player = event.getPlayer();
        Block selectedBlock = event.getClickedBlock().getRelative(event.getBlockFace());
        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.plugin.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        if (player.hasPermission("netherwater.scooping.bypass")) {
            this.messageManager.dump("Stopped by permissions");
        }

        if (!this.configManager.isScoopingDisabled() || player.hasPermission("netherwater.scooping.bypass")) {
            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Remove water (set flow water instead of water source block)
        Levelled flowingWater = (Levelled) Material.WATER.createBlockData();
        flowingWater.setLevel(1);

        selectedBlock.setBlockData(flowingWater);
    }
}
