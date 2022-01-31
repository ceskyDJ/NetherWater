package cz.ceskydj.netherwater;

import com.bergerkiller.bukkit.common.internal.CommonPlugin;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import cz.ceskydj.netherwater.commands.BaseCommand;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.EntityStorage;
import cz.ceskydj.netherwater.database.WorldEditChangesStorage;
import cz.ceskydj.netherwater.exceptions.PluginNotFoundException;
import cz.ceskydj.netherwater.listeners.*;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.ConfigManipulator;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import cz.ceskydj.netherwater.tasks.MobDamagingAgent;
import cz.ceskydj.netherwater.tasks.WaterAnimationAgent;
import cz.ceskydj.netherwater.tasks.WaterDisappearingAgent;
import cz.ceskydj.netherwater.tasks.WorldEditChangesAgent;
import cz.ceskydj.netherwater.updater.UpdateChecker;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.Objects;

public class NetherWater extends JavaPlugin {
    @SuppressWarnings("FieldCanBeLocal")
    private final int bStatsPluginId = 8833;

    private WorldGuardPlugin worldGuard = null;
    private WorldEditPlugin worldEdit = null;
    private MultiverseCore multiverseCore = null;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DB db;
    private PermissionManager permissionManager;
    private EntityStorage entityStorage;
    private WorldEditChangesStorage worldEditChangesStorage;

    @Override
    public void onEnable() {
        ConfigManipulator configManipulator = new ConfigManipulator(this);
        this.configManager = new ConfigManager(configManipulator);
        this.messageManager = new MessageManager(this);
        this.db = new DB("data.db", this);
        Permission vaultPermissions = this.setupVaultPermissions();
        this.permissionManager = new PermissionManager(this, vaultPermissions);

        // Temporary storage for entities to be damaged
        this.entityStorage = new EntityStorage();
        // Temporary storage for blocks changed by WorldEdit
        this.worldEditChangesStorage = new WorldEditChangesStorage();

        // Info about Vault permissions
        if (vaultPermissions == null) {
            this.messageManager.consoleMessage("There was a problem with loading Vault. Some functionality won't be available.", ChatColor.RED);
        } else {
            this.messageManager.consoleMessage("Vault has been registered and will be used for some extended functionality.", ChatColor.GREEN);
        }

        // Load WorldGuard
        try {
            this.worldGuard = this.loadWorldGuard();
            this.messageManager.consoleMessage("World Guard has been found and registered.", ChatColor.GREEN);
        } catch (PluginNotFoundException e) {
            this.messageManager.consoleMessage("World Guard hasn't been found. Some functionality won't be activated.");
        }

        // Load WorldEdit
        try {
            this.worldEdit = this.loadWorldEdit();
            this.messageManager.consoleMessage("World Edit has been found and registered.", ChatColor.GREEN);
        } catch (PluginNotFoundException e) {
            this.messageManager.consoleMessage("World Guard hasn't been found. Some functionality won't be activated.");
        }

        // Load Multiverse-Core
        try {
            this.multiverseCore = this.loadMultiverseCore();
            this.messageManager.consoleMessage("Multiverse-Core has been found and registered.", ChatColor.GREEN);
        } catch(PluginNotFoundException e) {
            this.messageManager.consoleMessage("Multiverse-Core hasn't been found. Some functionality won't be activated.");
        }

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new WaterPlaceListener(this), this);
        pluginManager.registerEvents(new IceBreakListener(this), this);
        pluginManager.registerEvents(new WaterFlowListener(this), this);
        pluginManager.registerEvents(new WaterScoopListener(this), this);
        pluginManager.registerEvents(new WaterCreateListener(this), this);
        pluginManager.registerEvents(new WaterDispenseListener(this), this);
        pluginManager.registerEvents(new WaterReplaceListener(this), this);
        pluginManager.registerEvents(new DispenserPlaceListener(this), this);
        pluginManager.registerEvents(new BlockBreakListener(this), this);

        // Some listeners requires BKCommonLib
        if (this.isBKCommonLibInstalled()) {
            this.messageManager.consoleMessage("BKCommonLib has been found.", ChatColor.GREEN);

            pluginManager.registerEvents(new MobMoveListener(this), this);
        } else {
            this.messageManager.consoleMessage("BKCommonLib hasn't been found. Some functionality won't be activated.");
        }

        // Some listeners requires WorldEdit
        if (this.worldEdit != null) {
            this.worldEdit.getWorldEdit().getEventBus().register(new WorldEditActionListener(this));
        }

        Objects.requireNonNull(this.getCommand("netherwater")).setExecutor(new BaseCommand(this));
        Objects.requireNonNull(this.getCommand("netherwater")).setTabCompleter(new BaseCommand(this));

        BukkitScheduler scheduler = this.getServer().getScheduler();
        // Water disappearing
        if (this.configManager.isWaterDisappearingEnabled()) {
            scheduler.scheduleSyncRepeatingTask(this, new WaterDisappearingAgent(this), 0L, 200L);
            this.messageManager.dump("Water disappearing agent has been activated");
        }
        // Water animation
        if (this.configManager.isWaterAnimationEnabled()) {
            scheduler.scheduleSyncRepeatingTask(this, new WaterAnimationAgent(this), 0L, 60L);
            this.messageManager.dump("Water animating agent has been activated");
        }
        // Mob damaging
        if (this.configManager.isMobDamagingEnabled() || this.configManager.isPlayerDamagingEnabled()) {
            scheduler.scheduleSyncRepeatingTask(this, new MobDamagingAgent(this), 0L, 20L);
            this.messageManager.dump("Mob damaging agent has been activated");
        }
        // WorldEdit changes batch parser
        if (this.worldEdit != null) {
            scheduler.scheduleSyncRepeatingTask(this, new WorldEditChangesAgent(this), 0L, 20L);
            this.messageManager.dump("WorldEdit changes agent has been activated");
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
        this.entityStorage.clearEntities();
        this.worldEditChangesStorage.clearBlockChanges();

        this.messageManager.consoleMessage("Plugin has been disabled successfully");
    }

    private WorldGuardPlugin loadWorldGuard() throws PluginNotFoundException {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(plugin instanceof WorldGuardPlugin)) {
            throw new PluginNotFoundException("Plugin WorldGuard hasn't been found.");
        }

        return (WorldGuardPlugin) plugin;
    }

    private boolean isBKCommonLibInstalled() {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("BKCommonLib");

        return (plugin instanceof CommonPlugin);
    }

    private WorldEditPlugin loadWorldEdit() throws PluginNotFoundException {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldEdit");

        if (!(plugin instanceof WorldEditPlugin)) {
            throw new PluginNotFoundException("Plugin WorldEdit hasn't been found.");
        }

        return (WorldEditPlugin) plugin;
    }

    private MultiverseCore loadMultiverseCore() throws PluginNotFoundException {
        Plugin plugin = this.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (!(plugin instanceof MultiverseCore)) {
            throw new PluginNotFoundException("Plugin Multiverse-Core hasn't been found.");
        }

        return (MultiverseCore) plugin;
    }

    private Permission setupVaultPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);

        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
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
        return this.worldGuard;
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public EntityStorage getEntityStorage() {
        return this.entityStorage;
    }

    public WorldEditChangesStorage getWorldEditChangesStorage() {
        return this.worldEditChangesStorage;
    }

    public MultiverseCore getMultiverseCore() {
        return this.multiverseCore;
    }

    public Player getClosestPlayer(org.bukkit.Location baseLocation) {
        World world = baseLocation.getWorld();
        List<Player> playersInSameWorld = Objects.requireNonNull(world).getPlayers();

        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player currentPlayer: playersInSameWorld) {
            double currentDistance = currentPlayer.getLocation().distance(baseLocation);
            if (nearestPlayer == null || currentDistance < nearestDistance) {
                nearestPlayer = currentPlayer;
                nearestDistance = currentDistance;
            }
        }

        // Player has to be very close to the place where the event occurred
        if (nearestDistance > 10.0) {
            return null;
        }

        return nearestPlayer;
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
