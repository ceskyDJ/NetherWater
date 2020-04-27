package com.leetzilantonis.netherwater;

import java.io.File;
import java.util.List;

public class ConfigManager {
    private final NetherWater plugin;

    public ConfigManager(NetherWater plugin) {
        this.plugin = plugin;

        this.loadConfig();
    }

    private void loadConfig() {
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }

        if (!new File(this.plugin.getDataFolder(), "config.yml").exists()) {
            this.plugin.saveDefaultConfig();
        } else {
            this.reloadConfig();
        }
    }

    public void reloadConfig() {
        this.plugin.reloadConfig();
    }

    public List<String> getDisabledWorlds() {
        return this.plugin.getConfig().getStringList("disabledWorlds");
    }

    public String getMessage(String name) {
        return this.plugin.getConfig().getString("messages." + name);
    }

    public int getMinHeight() {
        return plugin.getConfig().getInt("minHeight");
    }

    public int getMaxHeight() {
        return plugin.getConfig().getInt("maxHeight");
    }
}
