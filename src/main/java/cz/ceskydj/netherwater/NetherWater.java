package cz.ceskydj.netherwater;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import cz.ceskydj.netherwater.commands.BaseCommand;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.exceptions.PluginNotFoundException;
import cz.ceskydj.netherwater.listeners.*;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.ConfigManipulator;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import cz.ceskydj.netherwater.tasks.WaterDisappearingAgent;
import cz.ceskydj.netherwater.updater.UpdateChecker;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class NetherWater extends JavaPlugin {
    private final int bStatsPluginId = 8833;

    private WorldGuardPlugin worldGuard = null;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DB db;
    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        ConfigManipulator configManipulator = new ConfigManipulator(this);
        this.configManager = new ConfigManager(configManipulator);
        this.messageManager = new MessageManager(this);
        this.db = new DB("data.db", this);
        this.permissionManager = new PermissionManager(this);

        try {
            this.worldGuard = this.loadWorldGuard();
            this.messageManager.consoleMessage("World Guard has been found and registered!", ChatColor.GREEN);
        } catch (PluginNotFoundException e) {
            this.worldGuard = null;
            this.messageManager.consoleMessage("World Guard hasn't been found.");
        }

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new WaterPlaceListener(this), this);
        pluginManager.registerEvents(new BlockBreakListener(this), this);
        pluginManager.registerEvents(new WaterFlowListener(this), this);
        pluginManager.registerEvents(new WaterScoopListener(this), this);
        pluginManager.registerEvents(new WaterCreateListener(this), this);
        pluginManager.registerEvents(new WaterDispenseListener(this), this);
        pluginManager.registerEvents(new WaterReplaceListener(this), this);

        this.getCommand("netherwater").setExecutor(new BaseCommand(this));
        this.getCommand("netherwater").setTabCompleter(new BaseCommand(this));

        BukkitScheduler scheduler = this.getServer().getScheduler();
        if (this.configManager.isWaterDisappearingEnabled()) {
            scheduler.scheduleSyncRepeatingTask(this, new WaterDisappearingAgent(this), 0L, 200L);
        }

        MetricsLite metricsLite = new MetricsLite(this, this.bStatsPluginId);
        if (metricsLite.isEnabled()) {
            this.messageManager.consoleMessage("Connected to bStats", ChatColor.GREEN);
        }

        this.messageManager.consoleMessage("Plugin loaded successfully", ChatColor.GREEN);

        this.messageManager.consoleMessage("Checking for updates...");
        this.checkForUpdates();
    }

    @Override
    public void onDisable() {
        this.db.closeConnection();

        this.messageManager.consoleMessage("Plugin has been disabled successfully");
    }

    private WorldGuardPlugin loadWorldGuard() throws PluginNotFoundException {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new PluginNotFoundException("Plugin WorldGuard hasn't been found.");
        }

        return (WorldGuardPlugin) plugin;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public DB getDatabaseWrapper() {
        return this.db;
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
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
                .handleResponse((versionResponse, version) -> {
                    switch (versionResponse) {
                        case FOUND_NEW:
                            this.messageManager.consoleMessage("Updater has found a new version " + version + "!", ChatColor.YELLOW);
                            this.messageManager.consoleMessage("You should update the plugin.", ChatColor.YELLOW);
                            this.messageManager.consoleMessage("See: https://www.spigotmc.org/resources/nether-water-enable-water-in-nether-worlds.79256/", ChatColor.YELLOW);
                            break;
                        case LATEST:
                            this.messageManager.consoleMessage("You have the newest version of the plugin.", ChatColor.GREEN);
                            break;
                        case UNAVAILABLE:
                        default:
                            this.messageManager.consoleMessage("Update check has't been successful.", ChatColor.RED);
                            break;
                    }
                }).check();
    }
}
