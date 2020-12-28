package cz.ceskydj.netherwater.database;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DB {
    private final NetherWater plugin;
    private final MessageManager messageManager;

    private Connection connection = null;

    public DB(String fileName, NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();

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

        String sql = "CREATE TABLE IF NOT EXISTS `water_blocks` (\n"
                + "     `x` INTEGER NOT NULL,\n"
                + "     `y` INTEGER NOT NULL,\n"
                + "     `z` INTEGER NOT NULL,\n"
                + "     `placed` TEXT NOT NULL,\n"
                + "     `world` TEXT NOT NULL,\n"
                + "     `from` TEXT NOT NULL,\n"
                + "     `disappear` INTEGER NOT NULL CHECK(`disappear` IN (0, 1)),"
                + "     PRIMARY KEY (`x`, `y`, `z`)\n"
                + ");";

        try {
            Statement query = this.connection.createStatement();

            query.execute(sql);

            this.messageManager.dump("Database structure generated");
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    private void upgradeStructure() {
        try {
            String sql = "SELECT `disappear` FROM `water_blocks` LIMIT 1;";
            Statement query = this.connection.createStatement();

            query.execute(sql);

            // It's OK, there is nothing to change
            return;
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        // Disappear column is missing
        this.messageManager.dump("Upgrading database structure...");

        // Add disappear column to the table
        try {
            String sql = "ALTER TABLE `water_blocks` ADD `disappear` INTEGER NOT NULL CHECK(`disappear` IN (0, 1))";
            Statement query = this.connection.createStatement();

            query.execute(sql);
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        // Fill disappear column with data (it's true for every item - items with opposite value didn't use to add)
        try {
            // Fill all rows
            String sql = "UPDATE `water_blocks` SET `disappear` = 1";
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
        String sql = "INSERT INTO `water_blocks` (`x`, `y`, `z`, `placed`, `world`, `from`, `disappear`) "
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

    public void deleteWaterBlock(Block block) {
        String sql = "DELETE FROM `water_blocks` WHERE `x` = ? AND `y` = ? AND `z` = ?";

        try {
            PreparedStatement query = this.connection.prepareStatement(sql);

            query.setInt(1, block.getX());
            query.setInt(2, block.getY());
            query.setInt(3, block.getZ());

            query.executeUpdate();
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }
    }

    public List<Block> getAllWaterBlocks() {
        List<Block> blocks = new ArrayList<>();
        try {
            String sql = "SELECT `world`, `x`, `y`, `z` FROM `water_blocks`";
            Statement query = this.connection.createStatement();

            ResultSet resultSet = query.executeQuery(sql);

            while (resultSet.next()) {
                blocks.add(this.createBlockFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        return blocks;
    }

    public List<Block> getWaterBlocksForDisappearing(int disappearAfter) {
        List<Block> blocks = new ArrayList<>();
        try {
            String sql = "SELECT `world`, `x`, `y`, `z` FROM `water_blocks` WHERE `placed` < DATETIME(?, 'unixepoch') AND `disappear` = 1";
            PreparedStatement query = this.connection.prepareStatement(sql);

            Date date = new Date();
            query.setLong(1, (date.getTime() / 1000) - (disappearAfter * 60L));

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                blocks.add(this.createBlockFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());
        }

        return blocks;
    }

    private Block createBlockFromResultSet(ResultSet resultSet) {
        try {
            String world = resultSet.getString("world");
            int x = resultSet.getInt("x");
            int y = resultSet.getInt("y");
            int z = resultSet.getInt("z");

            return Objects.requireNonNull(this.plugin.getServer().getWorld(world)).getBlockAt(x, y, z);
        } catch (SQLException e) {
            this.messageManager.dump("DB error: " + e.getMessage());

            return null;
        }
    }
}
