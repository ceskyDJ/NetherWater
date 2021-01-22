package cz.ceskydj.netherwater.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.bridges.CustomWorldEditExtent;
import org.bukkit.World;

import java.util.Objects;

public class WorldEditActionListener {
    NetherWater plugin;
    private final ConfigManager configManager;

    public WorldEditActionListener(NetherWater plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        World world = BukkitAdapter.adapt(Objects.requireNonNull(event.getWorld()));

        if (event.getStage() != EditSession.Stage.BEFORE_HISTORY) {
            return;
        }

        if (world.getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (this.configManager.getDisabledWorlds().contains(world.getName())) {
            return;
        }

        // Delegate extent to the custom one
        event.setExtent(new CustomWorldEditExtent(this.plugin, event.getExtent(), world));
    }
}
