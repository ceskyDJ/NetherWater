package com.leetzilantonis.netherwater.config;

import com.leetzilantonis.netherwater.NetherWater;

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

    public boolean isDebugOn() {
        return this.plugin.getConfig().getBoolean("debug");
    }

    public List<String> getDisabledWorlds() {
        return this.plugin.getConfig().getStringList("disabled-worlds");
    }

    public String getMessage(String name) {
        return this.plugin.getConfig().getString("messages." + name);
    }

    public int getMinHeight() {
        return this.plugin.getConfig().getInt("min-height");
    }

    public int getMaxHeight() {
        return this.plugin.getConfig().getInt("max-height");
    }

    public boolean isSpreadBypassEnabled() {
        return this.plugin.getConfig().getBoolean("spread-bypass");
    }
}
