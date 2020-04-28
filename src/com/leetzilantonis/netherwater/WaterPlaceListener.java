package com.leetzilantonis.netherwater;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class WaterPlaceListener implements Listener {
	private final NetherWater plugin;
	private final ConfigManager configManager;

	public WaterPlaceListener(NetherWater plugin) {
		this.plugin = plugin;

		this.configManager = this.plugin.getConfigManager();
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

		World world = event.getClickedBlock().getWorld();
		Player player = event.getPlayer();

		if (world.getEnvironment() != Environment.NETHER) {
			return;
		}

		if (event.getItem() == null || event.getItem().getType() != Material.WATER_BUCKET) {
			return;
		}

		if (!(player.hasPermission("netherwater.use." + world.getName()) || player.hasPermission("netherwater.use.*"))) {
			return;
		}

		if (this.configManager.getDisabledWorlds().contains(world.getName()) && !player.hasPermission("netherwater.world.bypass")) {
			return;
		}

		if (!plugin.canBuild(player, event.getClickedBlock().getRelative(event.getBlockFace())))
			return;

		int y = event.getClickedBlock().getRelative(event.getBlockFace()).getY();
		if (y > this.configManager.getMaxHeight()) {
			return;
		}

		if (y < this.configManager.getMinHeight()) {
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
