package cz.ceskydj.netherwater.tasks;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.EntityStorage;
import cz.ceskydj.netherwater.managers.ConfigManager;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public class MobDamagingAgent implements Runnable {
    EntityStorage entityStorage;
    MessageManager messageManager;
    ConfigManager configManager;

    public MobDamagingAgent(NetherWater plugin) {
        this.entityStorage = plugin.getEntityStorage();
        this.messageManager = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
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
            String blockType = block.getType().toString();

            Waterlogged waterlogged = null;
            if (block.getBlockData() instanceof Waterlogged) {
                waterlogged = (Waterlogged) block.getBlockData();
            }

            // The mob must be in the water
            // Mobs outside the water are at the save place, so there is no reason to damage them
            if (block.getType() != Material.WATER && !blockType.contains("CAULDRON") && waterlogged == null) {
                this.entityStorage.markEntityForDeletion(entity);

                return;
            }

            // Cauldron must contain some water
            if (blockType.contains("CAULDRON") && !(block.getBlockData() instanceof Levelled)) {
                this.entityStorage.markEntityForDeletion(entity);

                return;
            }

            // Water logged block must be surrounded by water
            if (waterlogged != null && !waterlogged.isWaterlogged()) {
                this.entityStorage.markEntityForDeletion(entity);

                return;
            }

            // Ignore entities with fire resistance potion effect
            if (entity.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                return;
            }

            World world = entity.getLocation().getWorld();
            int difficulty;
            if (world != null) {
                difficulty = world.getDifficulty().ordinal();
            } else {
                difficulty = 1;
            }

            double damageValue = entity instanceof Player
                    ? this.configManager.getPlayerDamageValue() * difficulty
                    : this.configManager.getMobDamageValue();

            EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.LAVA;
            EntityDamageByBlockEvent damageEvent = new EntityDamageByBlockEvent(block, entity, cause, damageValue);

            entity.damage(damageValue);
            entity.setLastDamageCause(damageEvent);
        });

        this.messageManager.dump("Damaged " + entityStorage.getEntities().size() + " entities.");

        // Confirm entity deletion
        entityStorage.deleteEntities();
    }
}
