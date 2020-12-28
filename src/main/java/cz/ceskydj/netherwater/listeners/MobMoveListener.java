package cz.ceskydj.netherwater.listeners;

import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bouncycastle.util.Arrays;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;

import java.util.Objects;

public class MobMoveListener implements Listener {
    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public MobMoveListener(NetherWater plugin) {
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityMove(EntityMoveEvent event) {
        Entity entity = event.getEntity();
        Block block = entity.getLocation().getBlock();
        World world = event.getWorld();

        if (world.getEnvironment() != World.Environment.NETHER) {
            return;
        }

        if (this.configManager.getDisabledWorlds().contains(world.getName())) {
            return;
        }

        if (block.getType() != Material.WATER) {
            return;
        }

        if (this.isExceptEntity(entity.getType())) {
            return;
        }

        // Entity can be dead in this time, so it can't be converted to living entity
        if (entity.isDead()) {
            return;
        }

        EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.LAVA;
        EntityDamageByBlockEvent damageEvent = new EntityDamageByBlockEvent(block, entity, cause, 1.0D);

        LivingEntity livingEntity = (LivingEntity) entity;
        livingEntity.damage(1.0);
        livingEntity.setLastDamageCause(damageEvent);

        this.messageManager.dump("Entity move event event has been handled.");
        this.messageManager.dump("- World: " + world.getName());
        this.messageManager.dump("- Block: " + block.getType().name());
        this.messageManager.dump("- Metadata: " + block.getBlockData().getAsString(true));
        this.messageManager.dump("- Entity: " + entity.getName());
        this.messageManager.dump("- Entity type: " + entity.getType().name());
        this.messageManager.dump("- Last cause: " + Objects.requireNonNull(entity.getLastDamageCause()).getCause().name());
        this.messageManager.dump("- Health: " + ((LivingEntity) entity).getHealth());
    }

    private boolean isExceptEntity(EntityType type) {
        EntityType[] exceptEntities = {
                EntityType.GUARDIAN
        };

        for (EntityType modifiedEntity : exceptEntities) {
            if (modifiedEntity == type) {
                return true;
            }
        }

        return false;
    }
}
