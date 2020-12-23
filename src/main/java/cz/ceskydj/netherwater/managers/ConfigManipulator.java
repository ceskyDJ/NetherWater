package cz.ceskydj.netherwater.managers;

import cz.ceskydj.netherwater.NetherWater;
import de.leonhard.storage.Config;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConfigManipulator {
    private final NetherWater plugin;
    private final MessageManager messageManager;

    private YamlConfiguration defaultConfig;
    private Config configData;

    public ConfigManipulator(NetherWater plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();

        this.loadConfig();
    }

    private void loadConfig() {
        if (!this.plugin.getDataFolder().exists()) {
            if(!this.plugin.getDataFolder().mkdir()) {
                this.messageManager.consoleMessage("Error occurred while creating plugin's data folder (plugins/NetherWater).", ChatColor.RED);
            }
        }

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.plugin.saveResource("config.yml", false);
        } else {
            this.configData = new Config(configFile);
        }

        // Loading default config for cases that local config in plugins directory doesn't contains some values
        try {
            Reader defaultStream = new InputStreamReader(this.plugin.getResource("config.yml"), StandardCharsets.UTF_8);
            this.defaultConfig = YamlConfiguration.loadConfiguration(defaultStream);
        } catch (Exception ignored) {
        }
    }

    public <T> T getObject(String key, Class<T> clazz) {
        T defaultValue = this.defaultConfig.getObject(key, clazz);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        boolean defaultValue = this.defaultConfig.getBoolean(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Boolean> getBooleanList(String key) {
        List<Boolean> defaultValue = this.defaultConfig.getBooleanList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public int getInt(String key) {
        int defaultValue = this.defaultConfig.getInt(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Integer> getIntegerList(String key) {
        List<Integer> defaultValue = this.defaultConfig.getIntegerList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public long getLong(String key) {
        long defaultValue = this.defaultConfig.getLong(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Long> getLongList(String key) {
        List<Long> defaultValue = this.defaultConfig.getLongList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public float getFloat(String key) {
        float defaultValue = (float) this.defaultConfig.getDouble(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Float> getFloatList(String key) {
        List<Float> defaultValue = this.defaultConfig.getFloatList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public double getDouble(String key) {
        double defaultValue = this.defaultConfig.getDouble(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Double> getDoubleList(String key) {
        List<Double> defaultValue = this.defaultConfig.getDoubleList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public char getChar(String key) {
        char defaultValue = this.defaultConfig.getString(key).charAt(0);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Character> getCharacterList(String key) {
        List<Character> defaultValue = this.defaultConfig.getCharacterList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public String getString(String key) {
        String defaultValue = this.defaultConfig.getString(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<String> getStringList(String key) {
        List<String> defaultValue = this.defaultConfig.getStringList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public Color getColor(String key) {
        Color defaultValue = this.defaultConfig.getColor(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public ItemStack getItemStack(String key) {
        ItemStack defaultValue = this.defaultConfig.getItemStack(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public Location getLocation(String key) {
        Location defaultValue = this.defaultConfig.getLocation(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public OfflinePlayer getOfflinePlayer(String key) {
        OfflinePlayer defaultValue = this.defaultConfig.getOfflinePlayer(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public Vector getVector(String key) {
        Vector defaultValue = this.defaultConfig.getVector(key);

        return this.configData.getOrDefault(key, defaultValue);
    }

    public List<Map<?, ?>> getMapList(String key) {
        List<Map<?, ?>> defaultValue = this.defaultConfig.getMapList(key);

        return this.configData.getOrDefault(key, defaultValue);
    }
}
