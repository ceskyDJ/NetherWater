package cz.ceskydj.netherwater.bridges;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.database.WorldEditChangesStorage;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CustomWorldEditExtent extends AbstractDelegateExtent {
    private final MessageManager messageManager;
    private final WorldEditChangesStorage worldEditChangesStorage;

    private final World world;

    public CustomWorldEditExtent(NetherWater plugin, Extent extent, World world) {
        super(extent);

        this.messageManager = plugin.getMessageManager();
        this.worldEditChangesStorage = plugin.getWorldEditChangesStorage();

        this.world = world;
    }

    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        this.handleSetBlock(location, block);

        return super.setBlock(location, block);
    }

    private <T extends BlockStateHolder<T>> void handleSetBlock(BlockVector3 location, T block) {
        this.messageManager.dump("WorldEdit set block event has been handled.");
        this.messageManager.dump("- World: " + this.world.getName());
        this.messageManager.dump("- Location: x = " + location.getX() + ", y = " + location.getY() + ", z = " + location.getZ());
        this.messageManager.dump("- Target block: " + block.getBlockType());

        Block bukkitBlock = world.getBlockAt(location.getX(), location.getY(), location.getZ());

        this.worldEditChangesStorage.addBlockChange(bukkitBlock);
    }
}
