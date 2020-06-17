package cz.ceskydj.netherwater.config;

import cz.ceskydj.netherwater.NetherWater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigManager {
    private final NetherWater plugin;

    private FileConfiguration configData;

    public ConfigManager(NetherWater plugin) {
        this.plugin = plugin;

        this.loadConfig();
    }

    public void loadConfig() {
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            this.plugin.saveResource("config.yml", false);
        } else {
            this.configData = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public boolean isDebugOn() {
        return this.configData.getBoolean("debug");
    }

    public List<String> getDisabledWorlds() {
        return this.configData.getStringList("disabled-worlds");
    }

    public String getMessage(String name) {
        return this.configData.getString("messages." + name);
    }

    public int getMinHeight() {
        return this.configData.getInt("min-height");
    }

    public int getMaxHeight() {
        return this.configData.getInt("max-height");
    }

    public boolean isSpreadBypassEnabled() {
        return this.configData.getBoolean("spread-bypass");
    }

    public boolean isSpreadEnabled() {
        return this.configData.getBoolean("spread-enabled");
    }
}
