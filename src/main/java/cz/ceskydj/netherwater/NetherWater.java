package cz.ceskydj.netherwater;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import cz.ceskydj.netherwater.commands.NWReloadCommand;
import cz.ceskydj.netherwater.config.ConfigManager;
import cz.ceskydj.netherwater.exceptions.PluginNotFoundException;
import cz.ceskydj.netherwater.listeners.*;
import cz.ceskydj.netherwater.updater.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherWater extends JavaPlugin {
    private WorldGuardPlugin worldGuard = null;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);

        try {
            this.worldGuard = this.getWorldGuard();
            this.getLogger().info("World Guard has been found and registered!");
        } catch (PluginNotFoundException e) {
            this.worldGuard = null;
            this.colorMessage("World Guard hasn't been found.", ChatColor.YELLOW);
        }

        this.getServer().getPluginManager().registerEvents(new WaterPlaceListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        this.getServer().getPluginManager().registerEvents(new WaterFlowListener(this), this);
        this.getServer().getPluginManager().registerEvents(new WaterScoopListener(this), this);
        this.getServer().getPluginManager().registerEvents(new WaterCreateListener(this), this);
        this.getCommand("nwreload").setExecutor(new NWReloadCommand(this));

        this.colorMessage("Plugin loaded successfully", ChatColor.GREEN);

        this.getLogger().info("Checking for updates...");
        this.checkForUpdates();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Plugin has been disabled successfully");
    }

    private WorldGuardPlugin getWorldGuard() throws PluginNotFoundException {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new PluginNotFoundException("Plugin WorldGuard hasn't been found.");
        }

        return (WorldGuardPlugin) plugin;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public void colorMessage(String message, ChatColor color) {
        this.getServer().getConsoleSender().sendMessage("[NetherWater] " + color + message);
    }

    public void dump(String message) {
        if (this.configManager.isDebugOn()) {
            this.colorMessage(message, ChatColor.YELLOW);
        }
    }

    public boolean canBuild(Player player, Block block) {
        if (this.worldGuard == null) {
            return true;
        }

        RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        LocalPlayer wgPlayer = this.worldGuard.wrapPlayer(player);
        Location weLocation = BukkitAdapter.adapt(block.getLocation());
        World weWorld = BukkitAdapter.adapt(block.getWorld());

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(wgPlayer, weWorld)) {
            return true;
        }

        return regionQuery.testState(weLocation, wgPlayer, Flags.BUILD);
    }

    public boolean canBeUsedThisPlugin(Player player, Block block) {
        org.bukkit.World world = block.getWorld();

        if (world.getEnvironment() != org.bukkit.World.Environment.NETHER) {
            return false;
        }

        if (!(player.hasPermission("netherwater.use." + world.getName()) || player.hasPermission("netherwater.use.*"))) {
            return false;
        }

        if (this.configManager.getDisabledWorlds().contains(world.getName()) && !player.hasPermission("netherwater.world.bypass")) {
            return false;
        }

        if (!this.canBuild(player, block)) {
            return false;
        }

        int y = block.getY();
        if (y > this.configManager.getMaxHeight()) {
            return false;
        }

        return y >= this.configManager.getMinHeight();
    }

    public Player getClosestPlayer(org.bukkit.Location location) {
        // From https://gist.github.com/fourohfour/8243657
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            double distance = player.getLocation().distance(location);
            if (closestDistance == Double.MAX_VALUE || distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    private void checkForUpdates() {
        UpdateChecker
                .of(this)
                .resourceId(79256)
                .handleResponse((versionResponse, version) -> {
                    switch (versionResponse) {
                        case FOUND_NEW:
                            this.colorMessage("Updater has found a new version " + version + "!", ChatColor.YELLOW);
                            this.colorMessage("You should update the plugin.", ChatColor.YELLOW);
                            this.colorMessage("See: https://www.spigotmc.org/resources/nether-water-enable-water-in-nether-worlds.79256/", ChatColor.YELLOW);
                            break;
                        case LATEST:
                            this.colorMessage("You have the newest version of the plugin.", ChatColor.GREEN);
                            break;
                        case UNAVAILABLE:
                        default:
                            this.colorMessage("Update check has't been successful.", ChatColor.RED);
                            break;
                    }
                }).check();
    }
}
