package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.EntityStorage;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class MobDamagingAgent implements Runnable {
    EntityStorage entityStorage;
    MessageManager messageManager;

    public MobDamagingAgent(NetherWater plugin) {
        this.entityStorage = plugin.getEntityStorage();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public void run() {
        this.messageManager.dump("Damaging mobs in the water in nether worlds...");

        // There are no entities for damaging
        if (entityStorage.getEntities().isEmpty()) {
            return;
        }

        entityStorage.getEntities().forEach(entity -> {
            // Entity can be already dead
            if (entity.isDead()) {
                this.entityStorage.markEntityForDeletion(entity);

                return;
            }

            // Block the mob is standing on
            Block block = entity.getLocation().getBlock();

            // The mob must be in the water
            // Mobs outside of the water are at the save place, so there is no reason to damage them
            if (block.getType() == Material.WATER) {
                EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.LAVA;
                EntityDamageByBlockEvent damageEvent = new EntityDamageByBlockEvent(block, entity, cause, 1.0D);

                entity.damage(1.0D);
                entity.setLastDamageCause(damageEvent);
            } else {
                this.entityStorage.markEntityForDeletion(entity);
            }
        });

        this.messageManager.dump("Damaged " + entityStorage.getEntities().size() + " entities.");

        // Confirm entity deletion
        entityStorage.deleteEntities();
    }
}
