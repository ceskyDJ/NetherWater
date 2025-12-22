package cz.ceskydj.netherwater.listeners;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.DB;
import cz.ceskydj.netherwater.database.WaterSource;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.managers.PermissionManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public void onPlayerInteract(PlayerInteractEvent event) throws InvocationTargetException, IllegalAccessException {
        Player player = event.getPlayer();
        ItemStack usedItem = event.getItem();
        Block clickedBlock = event.getClickedBlock();
        Block selectedBlock;
        if (clickedBlock != null) {
            selectedBlock = clickedBlock.getRelative(event.getBlockFace());
        } else {
            return;
        }
        World world = clickedBlock.getWorld();

        Levelled level = null;
        if (clickedBlock.getBlockData() instanceof Levelled) {
            level = (Levelled) clickedBlock.getBlockData();
        }

        Waterlogged clickedWaterlogged = null;
        if (clickedBlock.getBlockData() instanceof Waterlogged) {
            clickedWaterlogged = (Waterlogged) clickedBlock.getBlockData();
        }

        Waterlogged selectedWaterlogged = null;
        if (selectedBlock.getBlockData() instanceof Waterlogged) {
            selectedWaterlogged = (Waterlogged) selectedBlock.getBlockData();
        }

        this.messageManager.dump("Player interact event has been handled.");
        this.messageManager.dump("- Action: " + event.getAction());
        this.messageManager.dump("- Item: " + (usedItem != null ? usedItem.getType() : "NULL"));
        this.messageManager.dump("- Block: " + clickedBlock.getBlockData().getAsString() + (!clickedBlock.getBlockData().getMaterial().isSolid() ? " (not solid)" : ""));
        this.messageManager.dump("- Clicked block level: " + (level != null ? level.getLevel() : "NULL"));
        this.messageManager.dump("- Clicked block watter logged: " + (clickedWaterlogged != null ? (clickedWaterlogged.isWaterlogged() ? "True" : "False") : "NULL"));
        this.messageManager.dump("- Selected block watter logged: " + (selectedWaterlogged != null ? (selectedWaterlogged.isWaterlogged() ? "True" : "False") : "NULL"));
        this.messageManager.dump("- World: " + world.getName() + " (type: " + world.getEnvironment() + ")");
        this.messageManager.dump("- Player: " + player);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (usedItem == null) {
            return;
        }

        if (usedItem.getType() != Material.WATER_BUCKET) {
            // Cauldrons could be fill with glass bottles
            if (!clickedBlock.getType().toString().contains("CAULDRON")) {
                return;
            }

            if (usedItem.getType() != Material.POTION) {
                // Filling empty glass bottle could completely empty cauldron
                if (usedItem.getType() == Material.GLASS_BOTTLE && clickedBlock.getBlockData() instanceof Levelled) {
                    Levelled water_level = (Levelled) clickedBlock.getBlockData();
                    if (water_level.getLevel() == 1) {
                        this.db.deleteWaterBlock(clickedBlock);
                    }
                }

                return;
            }

            ItemMeta metadata = usedItem.getItemMeta();
            if (!(metadata instanceof PotionMeta)) {
                return;
            }

            PotionType potionType = getPotionType(metadata);
            if (potionType != PotionType.WATER) {
                return;
            }
        }

        // Check general conditions for using this plugin (world type, player permissions, world height etc.)
        if (!this.permissionManager.canBeUsedThisPlugin(player, selectedBlock)) {
            return;
        }

        // Container opening has higher priority
        if (clickedBlock.getState() instanceof Container && !player.isSneaking()) {
            return;
        }

        // FIX: cauldrons could be normally used in nether and this event is for placing water,
        // not for inserting, so it needs an exception
        if (clickedBlock.getType() == Material.CAULDRON) {
            // If the player has bypass permission, the cauldron won't dry
            boolean dry = !this.permissionManager.hasPermission("disappearing.bypass", player)
                    && !this.permissionManager.hasPermission("drying.bypass", player);
            this.db.insertWaterBlock(clickedBlock, WaterSource.BUCKET, dry);

            return;
        }

        // Cancel native event actions
        event.setCancelled(true);

        // Clicked block supports water logging (could be inside of water)
        // Regularly the selected block (in front of the clicked one) is processed,
        // so we need to replace the selected block with the clicked one to select
        // the right block
        if (clickedWaterlogged != null) {
            selectedBlock = clickedBlock;
            selectedWaterlogged = clickedWaterlogged;
        }

        // Add water
        if (selectedWaterlogged != null) {
            // Selected block supports water logging (could be inside of water)
            // Set selected block as water logged
            selectedWaterlogged.setWaterlogged(true);
            selectedBlock.setBlockData(selectedWaterlogged);
        } else {
            // Selected block doesn't support water logging
            Material selectedMaterial = selectedBlock.getBlockData().getMaterial();
            if (selectedMaterial != Material.AIR && selectedMaterial != Material.CAVE_AIR && selectedMaterial.isSolid()) {
                // Only air block or not solid block can be replaced with water
                return;
            }

            // Break not solid block naturally before replacing with water
            if (!selectedMaterial.isSolid()) {
                selectedBlock.breakNaturally();
            }

            selectedBlock.setType(Material.WATER);
        }

        // Replace water bucket with empty one
        if (player.getGameMode() != GameMode.CREATIVE) {
            Objects.requireNonNull(event.getItem()).setType(Material.BUCKET);
        }

        // If the player has bypass permission, the water block won't disappear
        boolean disappear = !this.permissionManager.hasPermission("disappearing.bypass", player);
        this.db.insertWaterBlock(selectedBlock, WaterSource.BUCKET, disappear);
    }

    private PotionType getPotionType(ItemMeta metadata) throws IllegalAccessException, InvocationTargetException {
        PotionMeta potionMetadata = (PotionMeta) metadata;
        PotionType potionType;
        try {
            // 1.20.6+
            Method getBasePotionType = metadata.getClass().getMethod("getBasePotionType");

            getBasePotionType.setAccessible(true);
            potionType = (PotionType) getBasePotionType.invoke(potionMetadata);
        } catch (NoSuchMethodException|IllegalArgumentException e) {
            // Legacy mode (< 1.20.6)
            potionType = potionMetadata.getBasePotionData().getType();
        }
        return potionType;
    }
}
