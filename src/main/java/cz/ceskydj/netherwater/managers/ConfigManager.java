package cz.ceskydj.netherwater.managers;

import java.util.List;

public class ConfigManager {
    private final ConfigManipulator configManipulator;

    public ConfigManager(ConfigManipulator configManipulator) {
        this.configManipulator = configManipulator;
    }

    private String replaceColorChars(String text) {
        return text.replace("&", "§");
    }

    public boolean isDebugOn() {
        return this.configManipulator.getBoolean("debug");
    }

    public List<String> getDisabledWorlds() {
        return this.configManipulator.getStringList("disabled-worlds");
    }

    public String getMessage(String name) {
        return this.replaceColorChars(this.configManipulator.getString("messages." + name));
    }

    public String getMessagePrefix() {
        return this.replaceColorChars(this.configManipulator.getString("messages.prefix"));
    }

    public int getMinHeight() {
        return this.configManipulator.getInt("min-height");
    }

    public int getMaxHeight() {
        return this.configManipulator.getInt("max-height");
    }

    public boolean isSpreadBypassEnabled() {
        return this.configManipulator.getBoolean("spread-bypass");
    }

    public boolean isSpreadEnabled() {
        return this.configManipulator.getBoolean("spread-enabled");
    }

    public boolean isScoopingDisabled() {
        return !this.configManipulator.getBoolean("scooping-into-buckets");
    }

    public boolean areInfiniteSourcesDisabled() {
        return !this.configManipulator.getBoolean("infinite-sources");
    }

    public int getWaterDisappearingTime() {
        return this.configManipulator.getInt("water-disappearing");
    }

    public int getCauldronDryingTime() {
        return this.configManipulator.getInt("cauldron-drying");
    }

    public boolean isWaterDisappearingEnabled() {
        return this.getWaterDisappearingTime() != 0;
    }

    public boolean isCauldronDryingEnabled() {
        return isWaterDisappearingEnabled() && this.getCauldronDryingTime() != 0;
    }

    public boolean isMobDamagingEnabled() {
        return this.configManipulator.getBoolean("mob-damaging");
    }

    public boolean isPlayerDamagingEnabled() {
        return this.configManipulator.getBoolean("player-damaging");
    }

    public double getMobDamageValue() {
        return this.configManipulator.getDouble("damage-value.mob");
    }

    public double getPlayerDamageValue() {
        return this.configManipulator.getDouble("damage-value.player");
    }

    public boolean isWaterAnimationEnabled() {
        return this.configManipulator.getBoolean("hot-water-animation");
    }
}
