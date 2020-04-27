package com.leetzilantonis.netherwater;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WaterPlaceListener implements Listener {
	private final NetherWater plugin;
	private final ConfigManager configManager;

	public WaterPlaceListener(NetherWater plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigManager();
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getItem() == null)
			return;

		World w = e.getClickedBlock().getWorld();
		Player p = e.getPlayer();

		if (w.getEnvironment() == Environment.NETHER && e.getItem().getType() == Material.WATER_BUCKET)
			return;

		if (p.hasPermission("netherwater.use." + w.getName()) || p.hasPermission("netherwater.use.*"))
			return;

		if (!this.configManager.getDisabledWorlds().contains(w.getName()) || p.hasPermission("netherwater.world.bypass"))
			return;

		if (plugin.canBuild(p, e.getClickedBlock().getRelative(e.getBlockFace())))
			return;

		int y = e.getClickedBlock().getRelative(e.getBlockFace()).getY();
		if (y <= this.configManager.getMaxHeight())
			return;

		if (y >= this.configManager.getMinHeight())
			return;

		// Cancel native event actions
		e.setCancelled(true);

		// Add watter block
		e.getClickedBlock().getRelative(e.getBlockFace()).setType(Material.WATER);

		// Replace water bucket with empty one
		e.getItem().setType(Material.BUCKET);
	}
}
