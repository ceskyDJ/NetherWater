package cz.ceskydj.netherwater.database;

import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityStorage {
    List<LivingEntity> savedEntities;
    List<LivingEntity> markForDeletion;

    public EntityStorage() {
        this.savedEntities = new ArrayList<>();
        this.markForDeletion = new ArrayList<>();
    }

    public List<LivingEntity> getEntities() {
        return this.savedEntities;
    }

    public boolean isSaved(LivingEntity entity) {
        return savedEntities.contains(entity);
    }

    public void addEntity(LivingEntity entity) {
        this.savedEntities.add(entity);
    }

    public void markEntityForDeletion(LivingEntity entity) {
        this.markForDeletion.add(entity);
    }

    public void deleteEntities() {
        this.savedEntities.removeAll(this.markForDeletion);
        this.markForDeletion.clear();
    }

    public void clearEntities() {
        this.savedEntities.clear();
        this.markForDeletion.clear();
    }
}
