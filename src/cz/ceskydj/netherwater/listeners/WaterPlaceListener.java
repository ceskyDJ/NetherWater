package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
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

	public WaterPlaceListener(NetherWater plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		this.plugin.dump("Player interact event has been handled.");
		this.plugin.dump("- Action: " + event.getAction());
		this.plugin.dump("- Item: " + (event.getItem() != null ? event.getItem().getType() : "NULL"));
		this.plugin.dump("- World: " + Objects.requireNonNull(event.getClickedBlock()).getWorld().getName() + " (type: " + event.getClickedBlock().getWorld().getEnvironment() + ")");
		this.plugin.dump("- Player: " + event.getPlayer());

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (event.getItem() == null) {
			return;
		}

		Player player = event.getPlayer();
		Block selectedBlock = event.getClickedBlock().getRelative(event.getBlockFace());

		if (event.getItem() == null || event.getItem().getType() != Material.WATER_BUCKET) {
			return;
		}

		// Check general conditions for using this plugin (world type, player permissions, world height etc.)
		if (!this.plugin.canBeUsedThisPlugin(player, selectedBlock)) {
			return;
		}

		// Cancel native event actions
		event.setCancelled(true);

		// Add watter block
		event.getClickedBlock().getRelative(event.getBlockFace()).setType(Material.WATER);

		// Replace water bucket with empty one
		if (player.getGameMode() != GameMode.CREATIVE) {
			event.getItem().setType(Material.BUCKET);
		}
	}
}
