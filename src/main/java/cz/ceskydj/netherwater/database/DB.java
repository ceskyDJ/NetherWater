package cz.ceskydj.netherwater.database;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class DB {
    private final NetherWater plugin;
    private final MessageManager messageManager;
    private final MultiverseCore multiverseCore;

    private Connection connection = null;

    private final String WATER_BLOCKS_TABLE = "water_blocks";
    private final String DISPENSERS_TABLE = "dispensers";

    public DB(String fileName, NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
        this.multiverseCore = plugin.getMultiverseCore();

        this.connect(fileName);
        this.generateStructure();
        this.upgradeStructure();
    }

    private void connect(String fileName) {
        String url = "jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + "/" + fileName;

        try {
            this.connection = DriverManager.getConnection(url);

            if (connection == null) {
                throw new SQLException("System cannot connect to database");
            }

            DatabaseMetaData metaData = connection.getMetaData();
            this.messageManager.dump("SQL");
            this.messageManager.dump("- Driver: " + metaData.getDriverName());
            this.messageManager.dump("- DB has been created");
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    private void generateStructure() {
        // This is the first time database is being accessed, it needs to generate its structure
        this.messageManager.dump("Generating database structure...");

        List<String> structuresGenerationSQLs = new ArrayList<>();

        // Water blocks table
        structuresGenerationSQLs.add("CREATE TABLE IF NOT EXISTS `" + this.WATER_BLOCKS_TABLE + "` (\n"
                + "     `x` INTEGER NOT NULL,\n"
                + "     `y` INTEGER NOT NULL,\n"
                + "     `z` INTEGER NOT NULL,\n"
                + "     `placed` TEXT NOT NULL,\n"
                + "     `world` TEXT NOT NULL,\n"
                + "     `from` TEXT NOT NULL,\n"
                + "     `disappear` INTEGER NOT NULL CHECK(`disappear` IN (0, 1)),"
                + "     PRIMARY KEY (`x`, `y`, `z`, `world`)\n"
                + ");");

        // Dispensers table
        structuresGenerationSQLs.add("CREATE TABLE IF NOT EXISTS `" + this.DISPENSERS_TABLE + "` (\n"
                + "     `x` INTEGER NOT NULL,\n"
                + "     `y` INTEGER NOT NULL,\n"
                + "     `z` INTEGER NOT NULL,\n"
                + "     `placed` TEXT NOT NULL,\n"
                + "     `world` TEXT NOT NULL,\n"
                + "     `player` TEXT NOT NULL,\n"
                + "     PRIMARY KEY (`x`, `y`, `z`, `world`)\n"
                + ");");

        try {
            for (String sql : structuresGenerationSQLs) {
                Statement query = this.connection.createStatement();

                query.execute(sql);
            }
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        this.messageManager.dump("Database structure generated");
    }

    private void upgradeStructure() {
        try {
            String sql = "SELECT `disappear` FROM `" + this.WATER_BLOCKS_TABLE + "` LIMIT 1;";
            Statement query = this.connection.createStatement();

            query.execute(sql);

            // It's OK, there is nothing to change
            return;
        } catch (SQLException e) {
            this.messageManager.dump("Column `disappear` is missing in DB");
        }

        // Disappear column is missing
        this.messageManager.dump("Upgrading database structure...");

        // Rename old table (backup it respectively)
        try {
            String sql = "ALTER TABLE `" + this.WATER_BLOCKS_TABLE + "` RENAME TO `water_blocks_backup`";
            Statement query = this.connection.createStatement();

            query.execute(sql);
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        // Generate the right structure
        this.generateStructure();

        // Revert data from the backup
        try {
            String sql = "INSERT INTO `" + this.WATER_BLOCKS_TABLE + "`(`x`, `y`, `z`, `placed`, `world`, `from`, `disappear`)\n" +
                    "       SELECT `x`, `y`, `z`, `placed`, `world`, `from`, 1 FROM `water_blocks_backup`";
            Statement query = this.connection.createStatement();

            query.execute(sql);
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        // Delete the old table
        try {
            String sql = "DROP TABLE `water_blocks_backup`";
            Statement query = this.connection.createStatement();

            query.execute(sql);
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public void insertWaterBlock(Block block, WaterSource from, boolean disappear) {
        String sql = "INSERT INTO `" + this.WATER_BLOCKS_TABLE + "` (`x`, `y`, `z`, `placed`, `world`, `from`, `disappear`) "
                + "     VALUES (?, ?, ?, DATETIME('now'), ?, ?, ?)";

        try {
            PreparedStatement query = this.connection.prepareStatement(sql);

            query.setInt(1, block.getX());
            query.setInt(2, block.getY());
            query.setInt(3, block.getZ());
            query.setString(4, block.getWorld().getName());
            query.setString(5, from.toString());
            query.setInt(6, disappear ? 1 : 0);

            query.executeUpdate();
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public void insertMultipleWaterBlocks(Collection<Block> blocks, WaterSource from, boolean disappear) {
        try {
            this.connection.setAutoCommit(false);

            for (Block block : blocks) {
                this.insertWaterBlock(block, from, disappear);
            }

            this.connection.commit();
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException e2) {
                this.messageManager.dump("DB error: " + e2.getMessage());
            }

            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public void insertDispenser(Block block, Player player) {
        String sql = "INSERT INTO `" + this.DISPENSERS_TABLE + "` (`x`, `y`, `z`, `placed`, `world`, `player`) "
                + "     VALUES (?, ?, ?, DATETIME('now'), ?, ?)";

        try {
            PreparedStatement query = this.connection.prepareStatement(sql);

            query.setInt(1, block.getX());
            query.setInt(2, block.getY());
            query.setInt(3, block.getZ());
            query.setString(4, block.getWorld().getName());
            query.setString(5, player.getUniqueId().toString());

            query.executeUpdate();
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public void deleteWaterBlock(Block block) {
        this.deleteBlock(block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), this.WATER_BLOCKS_TABLE);
    }

    public void deleteMultipleWaterBlocks(Collection<Block> blocks) {
        try {
            this.connection.setAutoCommit(false);

            for (Block block : blocks) {
                this.deleteWaterBlock(block);
            }

            this.connection.commit();
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException e2) {
                this.messageManager.dump("DB error: " + e2.getMessage());
            }

            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public void deleteDispenser(Block block) {
        this.deleteBlock(block.getX(), block.getY(), block.getZ(), block.getWorld().getName(), this.DISPENSERS_TABLE);
    }

    public List<Block> getAllWaterBlocks() {
        List<Block> blocks = new ArrayList<>();
        try {
            String sql = "SELECT `world`, `x`, `y`, `z` FROM `" + this.WATER_BLOCKS_TABLE + "`";
            Statement query = this.connection.createStatement();

            ResultSet resultSet = query.executeQuery(sql);

            while (resultSet.next()) {
                Block block;
                if ((block = this.createBlockFromResultSet(resultSet)) == null) {
                    // Block cannot be loaded by Bukkit wrapper (invalid world, coords etc.)
                    continue;
                }

                blocks.add(block);
            }
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        return blocks;
    }

    public List<Block> getWaterBlocksForDisappearing(int disappearAfter) {
        List<Block> blocks = new ArrayList<>();
        try {
            String sql = "SELECT `world`, `x`, `y`, `z` FROM `" + this.WATER_BLOCKS_TABLE + "` WHERE `placed` < DATETIME(?, 'unixepoch') AND `disappear` = 1";
            PreparedStatement query = this.connection.prepareStatement(sql);

            Date date = new Date();
            query.setLong(1, (date.getTime() / 1000) - (disappearAfter * 60L));

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                Block block;
                if ((block = this.createBlockFromResultSet(resultSet)) == null) {
                    // Block cannot be loaded by Bukkit wrapper (invalid world, coords etc.)
                    continue;
                }

                blocks.add(block);
            }
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        return blocks;
    }

    public OfflinePlayer getDispenserOwner(Block dispenser) {
        try {
            String sql = "SELECT `player` FROM `" + this.DISPENSERS_TABLE + "` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ?";
            PreparedStatement query = this.connection.prepareStatement(sql);

            query.setInt(1, dispenser.getX());
            query.setInt(2, dispenser.getY());
            query.setInt(3, dispenser.getZ());
            query.setString(4, dispenser.getWorld().getName());

            ResultSet resultSet = query.executeQuery();
            // Get first result item
            resultSet.next();

            String playerUUID = resultSet.getString("player");

            return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());

            return null;
        }
    }

    private Block createBlockFromResultSet(ResultSet resultSet) {
        try {
            String world = resultSet.getString("world");
            int x = resultSet.getInt("x");
            int y = resultSet.getInt("y");
            int z = resultSet.getInt("z");
            String table = resultSet.getMetaData().getTableName(1);

            World bukkitWorld;
            if ((bukkitWorld = this.plugin.getServer().getWorld(world)) == null) {
                if (this.inspectInvalidWorld(world)) {
                    this.messageManager.dump("Block from non-existing world found in DB, removing...");

                    this.deleteBlock(x, y, z, world, table);
                } else {
                    this.messageManager.dump("Unloaded world by Multiverse-Core '" + world + "' detected. Ignoring...");
                }

                return null;
            }

            return bukkitWorld.getBlockAt(x, y, z);
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());

            return null;
        }
    }

    private boolean inspectInvalidWorld(String world) {
        // There is no chance to detect if the world is just unloaded or removed completely,
        // so its records will be removed for safety reasons (problems with this kind of worlds)
        if (this.multiverseCore == null) {
            return true;
        }

        MVWorldManager worldManager = this.multiverseCore.getMVWorldManager();

        // World isn't a valid one or has already been removed
        if (worldManager.isMVWorld(world)) {
            return true;
        }

        // If the world isn't exists, its records will be removed (true returned)
        // In other cases, the world is unloaded, so no actions are required to do
        return !worldManager.hasUnloadedWorld(world, true);
    }

    private void deleteBlock(int x, int y, int z, String world, String table) {
        String sql = "DELETE FROM `" + table + "` WHERE `x` = ? AND `y` = ? AND `z` = ? AND `world` = ?";

        try {
            PreparedStatement query = this.connection.prepareStatement(sql);

            query.setInt(1, x);
            query.setInt(2, y);
            query.setInt(3, z);
            query.setString(4, world);

            query.executeUpdate();
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }
}
