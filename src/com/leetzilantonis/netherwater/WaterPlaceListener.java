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
	Main plugin;

	public WaterPlaceListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getItem() == null)
			return;

		World w = e.getClickedBlock().getWorld();
		Player p = e.getPlayer();

		if (w.getEnvironment() == Environment.NETHER && e.getItem().getType() == Material.WATER_BUCKET) {
			if (p.hasPermission("netherwater.use." + w.getName()) || p.hasPermission("netherwater.use.*")) {
				if (!plugin.getWorlds().contains(w.getName()) || p.hasPermission("netherwater.world.bypass")) {
					if (plugin.canBuild(p, e.getClickedBlock().getRelative(e.getBlockFace()))) {
						int y = e.getClickedBlock().getRelative(e.getBlockFace()).getY();
						if (y <= plugin.getConfig().getInt("maxHeight")) {
							if (y >= plugin.getConfig().getInt("minHeight")) {
								e.setCancelled(true);
								e.getClickedBlock().getRelative(e.getBlockFace()).setType(Material.WATER);
							}
						}
					}
				}
			}
		}
	}
}
