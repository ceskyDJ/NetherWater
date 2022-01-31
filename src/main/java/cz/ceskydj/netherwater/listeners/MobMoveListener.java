package cz.ceskydj.netherwater.listeners;

import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.EntityStorage;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MobMoveListener implements Listener {
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final EntityStorage entityStorage;

    public MobMoveListener(NetherWater plugin) {
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
        this.entityStorage = plugin.getEntityStorage();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityMove(EntityMoveEvent event) {
        if (!this.configManager.isMobDamagingEnabled()) {
            return;
        }

        this.processEntityMove(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!this.configManager.isPlayerDamagingEnabled()) {
            return;
        }

        this.processEntityMove(event.getPlayer());
    }

    private void processEntityMove(Entity entity) {
        Block block = entity.getLocation().getBlock();
        World world = block.getWorld();

        if (world.getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (this.configManager.isDisabledWorld(world)) {
            return;
        }

        String blockType = block.getType().toString();
        if (block.getType() != Material.WATER && !blockType.contains("CAULDRON")) {
            return;
        }

        if (blockType.contains("CAULDRON") && !(block.getBlockData() instanceof Levelled)) {
            return;
        }

        // It must be living entity (mob)
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        if (this.isExceptEntity(entity.getType())) {
            return;
        }

        // Entity can be dead in this time, so it can't be converted to living entity
        if (entity.isDead()) {
            return;
        }

        // If the mob has already been in the list, don't do anything
        if (this.entityStorage.isSaved((LivingEntity) entity)) {
            return;
        }

        // Save mob to temporary list
        // These mobs are automatically damaged if they are still in the water
        this.entityStorage.addEntity((LivingEntity) entity);

        this.messageManager.dump("Entity move event event has been handled.");
        this.messageManager.dump("- World: " + world.getName());
        this.messageManager.dump("- Block: " + block.getType().name());
        this.messageManager.dump("- Metadata: " + block.getBlockData().getAsString(true));
        this.messageManager.dump("- Entity: " + entity.getName());
        this.messageManager.dump("- Entity type: " + entity.getType().name());
    }

    private boolean isExceptEntity(EntityType type) {
        // Entities not to damage
        // EntityType enum isn't used due to compatibility issues
        String[] exceptEntities = {
                "DOLPHIN", "DROWNED", "ELDER_GUARDIAN", "TROPICAL_FISH", "GUARDIAN", "PUFFERFISH", "TURTLE",
                "SQUID", "AXOLOTL", "TROPICAL_FISH", "GLOW_SQUID", "COD", "SALMON"
        };

        for (String modifiedEntity : exceptEntities) {
            if (modifiedEntity.equals(type.toString())) {
                return true;
            }
        }

        return false;
    }
}
