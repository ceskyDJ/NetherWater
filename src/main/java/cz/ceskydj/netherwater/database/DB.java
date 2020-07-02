package cz.ceskydj.netherwater.database;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.managers.MessageManager;
import org.bukkit.block.Block;

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
            this.messageManager.dump(e.getMessage());
        }
    }

    private void generateStructure() {
        String sql = "CREATE TABLE IF NOT EXISTS `water_blocks` (\n"
                + "     `x` INTEGER NOT NULL ,\n"
                + "     `y` INTEGER NOT NULL,\n"
                + "     `z` INTEGER NOT NULL,\n"
                + "     `placed` TEXT NOT NULL,\n"
                + "     `world` TEXT NOT NULL,\n"
                + "     `from` TEXT NOT NULL,\n"
                + "     PRIMARY KEY (`x`, `y`, `z`)\n"
                + ");";

        try {
            Statement query = this.connection.createStatement();

            query.execute(sql);

            this.messageManager.dump("Database structure generated");
        } catch (SQLException e) {
            this.messageManager.dump(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            this.messageManager.dump(e.getMessage());
        }
    }

    public void insertWaterBlock(Block block, WaterSource from) {
        String sql = "INSERT INTO `water_blocks` (`x`, `y`, `z`, `placed`, `world`, `from`) "
                + "     VALUES (?, ?, ?, DATETIME('now'), ?, ?)";

        try {
            PreparedStatement query = this.connection.prepareStatement(sql);

            query.setInt(1, block.getX());
            query.setInt(2, block.getY());
            query.setInt(3, block.getZ());
            query.setString(4, block.getWorld().getName());
            query.setString(5, from.toString());

            query.executeUpdate();
        } catch (SQLException e) {
            this.messageManager.dump(e.getMessage());
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
            this.messageManager.dump(e.getMessage());
        }
    }

    public List<Block> getWaterBlocksForDisappearing(int disappearAfter) {
        String sql = "SELECT `world`, `x`, `y`, `z` FROM `water_blocks` WHERE `placed` < DATETIME(?, 'unixepoch')";

        List<Block> blocks = new ArrayList<>();
        try {
            PreparedStatement query = this.connection.prepareStatement(sql);

            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime() - (disappearAfter * 60));
            query.setLong(1, (date.getTime() / 1000) - (disappearAfter * 60));

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                String world = resultSet.getString("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");

                blocks.add(Objects.requireNonNull(this.plugin.getServer().getWorld(world)).getBlockAt(x, y, z));
            }
        } catch (SQLException e) {
            this.messageManager.dump(e.getMessage());
        }

        return blocks;
    }
}
