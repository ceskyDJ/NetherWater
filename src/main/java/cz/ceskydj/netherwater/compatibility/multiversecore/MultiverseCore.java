package cz.ceskydj.netherwater.compatibility.multiversecore;

public class MultiverseCore {
    private final PluginVersion version;
    private final com.onarandombox.MultiverseCore.MultiverseCore v4;
    private final org.mvplugins.multiverse.core.MultiverseCoreApi v5;

    private WorldManager worldManager = null;

    public MultiverseCore(com.onarandombox.MultiverseCore.MultiverseCore plugin) {
        v4 = plugin;
        v5 = null;
        version = PluginVersion.V4;
    }

    public MultiverseCore(org.mvplugins.multiverse.core.MultiverseCoreApi plugin) {
        v4 = null;
        v5 = plugin;
        version = PluginVersion.V5;
    }

    public WorldManager getMVWorldManager() {
        if (worldManager != null) {
            return worldManager;
        }

        if (version == PluginVersion.V4) {
            assert v4 != null;
            return worldManager = new WorldManager(v4.getMVWorldManager());
        } else {
            assert v5 != null;
            return worldManager = new WorldManager(v5.getWorldManager());
        }
    }
}
