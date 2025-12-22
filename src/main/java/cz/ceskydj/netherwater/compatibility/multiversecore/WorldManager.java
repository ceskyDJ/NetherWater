package cz.ceskydj.netherwater.compatibility.multiversecore;

public class WorldManager {
    private final PluginVersion version;
    private final com.onarandombox.MultiverseCore.api.MVWorldManager v4;
    private final org.mvplugins.multiverse.core.world.WorldManager v5;

    public WorldManager(com.onarandombox.MultiverseCore.api.MVWorldManager worldManager) {
        v4 = worldManager;
        v5 = null;
        version = PluginVersion.V4;
    }

    public WorldManager(org.mvplugins.multiverse.core.world.WorldManager worldManager) {
        v4 = null;
        v5 = worldManager;
        version = PluginVersion.V5;
    }

    public boolean isWorld(String world) {
        if (version == PluginVersion.V5) {
            assert v5 != null;
            return v5.isWorld(world);
        }

        assert v4 != null;
        return v4.isMVWorld(world);
    }

    public boolean isUnloadedWorld(String world) {
        if (version == PluginVersion.V5) {
            assert v5 != null;
            return v5.isUnloadedWorld(world);
        }

        assert v4 != null;
        return v4.hasUnloadedWorld(world, true);
    }
}
