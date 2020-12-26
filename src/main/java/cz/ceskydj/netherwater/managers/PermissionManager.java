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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PermissionManager {
    private final WorldGuardPlugin worldGuard;
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public PermissionManager(NetherWater plugin) {
        this.worldGuard = plugin.getWorldGuard();
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
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
            this.messageManager.sendMessage(player, "permissions");

            return false;
        }

        int y = block.getY();
        if (y > this.configManager.getMaxHeight()) {
            return false;
        }

        return y >= this.configManager.getMinHeight();
    }
}
