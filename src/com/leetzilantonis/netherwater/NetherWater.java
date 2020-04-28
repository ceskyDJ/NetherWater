package com.leetzilantonis.netherwater;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class NetherWater extends JavaPlugin {
	private WorldGuardPlugin worldGuard;

	private ConfigManager configManager;

	@Override
	public void onEnable() {
		this.configManager = new ConfigManager(this);

		try {
			this.worldGuard = this.getWorldGuard();
			this.getLogger().info("World Guard has been found and registered!");
		} catch (PluginNotFoundException e) {
			this.worldGuard = null;
			this.getLogger().warning("World Guard cannot be found.");
		}

		this.getServer().getPluginManager().registerEvents(new WaterPlaceListener(this), this);
		this.getCommand("nwreload").setExecutor(new NWReloadCommand(this));

		this.getLogger().info("Plugin loaded successfully");
	}

	@Override
	public void onDisable() {
		this.saveConfig();
	}

	private WorldGuardPlugin getWorldGuard() throws PluginNotFoundException {
		Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

		if (!(plugin instanceof WorldGuardPlugin)) {
			throw new PluginNotFoundException("Plugin WorldGuard hasn't been found.");
		}

		return (WorldGuardPlugin) plugin;
	}

	public boolean canBuild(Player p, Block b) {
		if (this.worldGuard == null) {
			return true;
		}

		LocalPlayer wgPlayer = this.worldGuard.wrapPlayer(p);
		World weWorld = wgPlayer.getWorld();
		Location location = new Location(weWorld, b.getX(), b.getY(), b.getZ());
		RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery regionQuery = regionContainer.createQuery();

		boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(wgPlayer, weWorld);

		return canBypass || regionQuery.testState(location, wgPlayer, Flags.BUILD);
	}

	public ConfigManager getConfigManager() {
		return this.configManager;
	}

	public void dump(String message) {
		if (this.configManager.isDebugOn()) {
			this.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[NetherWater] " + message);
		}
	}
}
