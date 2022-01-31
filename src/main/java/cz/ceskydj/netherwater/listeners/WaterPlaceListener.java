package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Objects;

public class WaterPlaceListener implements Listener {
    private final MessageManager messageManager;
    private final DB db;
    private final PermissionManager permissionManager;

    public WaterPlaceListener(NetherWater plugin) {
        this.messageManager = plugin.getMessageManager();
        this.db = plugin.getDatabaseWrapper();
        this.permissionManager = plugin.getPermissionManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack usedItem = event.getItem();
        Block eventBlock = event.getClickedBlock();
        Block selectedBlock;
        if (eventBlock != null) {
            selectedBlock = eventBlock.getRelative(event.getBlockFace());
        } else {
            return;
        }

        Levelled level = null;
        if (eventBlock.getBlockData() instanceof Levelled) {
            level = (Levelled) eventBlock.getBlockData();
        }

        this.messageManager.dump("Player interact event has been handled.");
        this.messageManager.dump("- Action: " + event.getAction());
        this.messageManager.dump("- Item: " + (usedItem != null ? usedItem.getType() : "NULL"));
        this.messageManager.dump("- Block: " + eventBlock.getBlockData().getAsString());
        this.messageManager.dump("- Level: " + (level != null ? level.getLevel() : "NULL"));
        this.messageManager.dump("- World: " + Objects.requireNonNull(selectedBlock).getWorld().getName() + " (type: " + selectedBlock.getWorld().getEnvironment() + ")");
        this.messageManager.dump("- Player: " + player);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (usedItem == null) {
            return;
        }

        if (usedItem.getType() != Material.WATER_BUCKET) {
            // Cauldrons could be fill with glass bottles
            if (!eventBlock.getType().toString().contains("CAULDRON")) {
                return;
            }

            if (usedItem.getType() != Material.POTION) {
                // Filling empty glass bottle could completely empty cauldron
                if (usedItem.getType() == Material.GLASS_BOTTLE && eventBlock.getBlockData() instanceof Levelled) {
                    Levelled water_level = (Levelled) eventBlock.getBlockData();
                    if (water_level.getLevel() == 1) {
                        this.db.deleteWaterBlock(eventBlock);
                    }
                }

                return;
            }

            ItemMeta metadata = usedItem.getItemMeta();
            if (!(metadata instanceof PotionMeta)) {
                return;
            }

            PotionMeta potionMetadata = (PotionMeta) metadata;
            if (potionMetadata.getBasePotionData().getType() != PotionType.WATER) {
                return;
            }
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.permissionManager.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        // FIX: cauldrons could be normally used in nether and this event is for placing water,
        // not for inserting, so it needs an exception
        if (eventBlock.getType() == Material.CAULDRON) {
            // If the player has bypass permission, the cauldron won't dry
            boolean dry = !this.permissionManager.hasPermission("disappearing.bypass", player)
                    && !this.permissionManager.hasPermission("drying.bypass", player);
            this.db.insertWaterBlock(eventBlock, WaterSource.BUCKET, dry);

            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Add water block
        selectedBlock.setType(Material.WATER);

        // Replace water bucket with empty one
        if (player.getGameMode() != GameMode.CREATIVE) {
            Objects.requireNonNull(event.getItem()).setType(Material.BUCKET);
        }

        // If the player has bypass permission, the water block won't disappear
        boolean disappear = !this.permissionManager.hasPermission("disappearing.bypass", player);
        this.db.insertWaterBlock(selectedBlock, WaterSource.BUCKET, disappear);
    }
}
