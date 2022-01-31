package cz.ceskydj.netherwater.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import cz.ceskydj.netherwater.NetherWater;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionManager {
    private final WorldGuardPlugin worldGuard;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final Permission permission;

    public PermissionManager(NetherWater plugin, Permission vaultPermissions) {
        this.worldGuard = plugin.getWorldGuard();
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
        this.permission = vaultPermissions;
    }

    public boolean canBuild(OfflinePlayer player, Block block) {
        if (this.worldGuard == null) {
            return true;
        }

        RegionQuery regionQuery = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        LocalPlayer wgPlayer = this.worldGuard.wrapOfflinePlayer(player);
        Location weLocation = BukkitAdapter.adapt(block.getLocation());
        World weWorld = BukkitAdapter.adapt(block.getWorld());

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(wgPlayer, weWorld)) {
            return true;
        }

        return regionQuery.testState(weLocation, wgPlayer, Flags.BUILD);
    }

    public boolean canBeUsedThisPlugin(OfflinePlayer player, Block block) {
        org.bukkit.World world = block.getWorld();

        if (world.getEnvironment() != org.bukkit.World.Environment.NETHER) {
            return false;
        }

        if (!(this.hasPermission("use." + world.getName(), player, world) || this.hasPermission("use.*", player, world))) {
            return false;
        }

        if (this.configManager.isDisabledWorld(world) && !this.hasPermission("world.bypass", player, world)) {
            return false;
        }

        if (!this.canBuild(player, block)) {
            if (player instanceof Player) {
                this.messageManager.sendMessage((Player) player, "permissions");
            }

            return false;
        }

        int y = block.getY();
        if (y > this.configManager.getMaxHeight()) {
            return false;
        }

        return y >= this.configManager.getMinHeight();
    }

    public boolean hasPermission(String permission, Player player) {
        permission = "netherwater.".concat(permission);

        // Default method - using Vault
        if (this.permission != null) {
            return this.permission.playerHas(player, permission);
        }

        // Bukkit permissions
        return player.hasPermission(permission);
    }

    public boolean hasPermission(String permission, CommandSender player) {
        permission = "netherwater.".concat(permission);

        // Bukkit permissions
        return player.hasPermission(permission);
    }

    public boolean hasPermission(String permission, OfflinePlayer player, org.bukkit.World world) {
        permission = "netherwater.".concat(permission);

        // Default method - using Vault
        if (this.permission != null) {
            return this.permission.playerHas(world.getName(), player, permission);
        }

        // Bukkit permissions
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            return onlinePlayer.hasPermission(permission);
        }

        // There is no way how to verify permission for that player
        // Better than allow everything is deny everything, so...
        return false;
    }
}
