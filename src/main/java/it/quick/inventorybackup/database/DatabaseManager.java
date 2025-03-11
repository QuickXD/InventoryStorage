package it.quick.inventorybackup.database;

import it.quick.inventorybackup.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private Connection connection;
    private final ConfigManager configManager;
    private final JavaPlugin plugin;

    public DatabaseManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;

        try {
            File dataFolder = new File("plugins/InventoryBackup");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String DATABASE_URL = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/inventory.db";
            connection = DriverManager.getConnection(DATABASE_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        String createInventoryTableSQL = "CREATE TABLE IF NOT EXISTS inventories ("
                + "uuid TEXT,"
                + "inventory BLOB,"
                + "armor BLOB,"
                + "timestamp INTEGER,"
                + "PRIMARY KEY (uuid, timestamp)"
                + ");";

        String createQuitLogTableSQL = "CREATE TABLE IF NOT EXISTS quit_logs ("
                + "uuid TEXT,"
                + "username TEXT,"
                + "quit_time INTEGER,"
                + "PRIMARY KEY (uuid, quit_time)"
                + ");";

        String createJoinLogTableSQL = "CREATE TABLE IF NOT EXISTS join_logs ("
                + "uuid TEXT,"
                + "username TEXT,"
                + "join_time INTEGER,"
                + "PRIMARY KEY (uuid, join_time)"
                + ");";

        String createDeathLogTableSQL = "CREATE TABLE IF NOT EXISTS death_logs ("
                + "uuid TEXT,"
                + "username TEXT,"
                + "death_time INTEGER,"
                + "cause TEXT,"
                + "PRIMARY KEY (uuid, death_time)"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            System.out.println("Creazione tabelle...");
            stmt.executeUpdate(createInventoryTableSQL);
            stmt.executeUpdate(createQuitLogTableSQL);
            stmt.executeUpdate(createJoinLogTableSQL);
            stmt.executeUpdate(createDeathLogTableSQL);
            System.out.println("Tabelle create con successo.");
        } catch (SQLException e) {
            System.out.println("Errore nella creazione delle tabelle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveInventory(Player player, ItemStack[] inventory, ItemStack[] armor, long timestamp) {
        try {
            byte[] inventoryBytes = serializeItems(inventory);
            byte[] armorBytes = serializeItems(armor);

            String query = "INSERT OR REPLACE INTO inventories (uuid, inventory, armor, timestamp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setBytes(2, inventoryBytes);
                stmt.setBytes(3, armorBytes);
                stmt.setLong(4, timestamp);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] serializeItems(ItemStack[] items) {
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null) {
                serializedItems.add(item.serialize());
            }
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(serializedItems);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private void enforceBackupLimit(Player player, String table, int maxBackup) {
        String countQuery = "SELECT COUNT(*) FROM " + table + " WHERE uuid = ?";
        String deleteQuery = "DELETE FROM " + table + " WHERE uuid = ? AND " +
                "quit_time = (SELECT MIN(quit_time) FROM " + table + " WHERE uuid = ?)";

        try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
            countStmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = countStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > maxBackup) {
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                    deleteStmt.setString(1, player.getUniqueId().toString());
                    deleteStmt.setString(2, player.getUniqueId().toString());
                    deleteStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logPlayerQuit(Player player, long quitTime) {
        if (!configManager.isTriggerEnabled("Quit")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveInventory(player, player.getInventory().getContents(), player.getInventory().getArmorContents(), quitTime);

            String query = "INSERT INTO quit_logs (uuid, username, quit_time) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setLong(3, quitTime);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            enforceBackupLimit(player, "quit_logs", configManager.getMaxBackup("Quit"));
        });
    }

    public void logPlayerJoin(Player player, long joinTime) {
        saveInventory(player, player.getInventory().getContents(), player.getInventory().getArmorContents(), joinTime);

        String query = "INSERT INTO join_logs (uuid, username, join_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, joinTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        enforceBackupLimit(player, "join_logs", configManager.getMaxBackup("Join"));
    }

    public void logPlayerDeath(Player player, String cause, long deathTime) {
        saveInventory(player, player.getInventory().getContents(), player.getInventory().getArmorContents(), deathTime);

        String query = "INSERT INTO death_logs (uuid, username, death_time, cause) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, deathTime);
            stmt.setString(4, cause);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        enforceBackupLimit(player, "death_logs", configManager.getMaxBackup("Death"));
    }

    public List<Backup> getBackupsForJoin(Player player, int maxBackup) {
        List<Backup> backups = new ArrayList<>();
        String query = "SELECT * FROM join_logs WHERE uuid = ? ORDER BY join_time DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, maxBackup);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long joinTime = rs.getLong("join_time");
                String username = rs.getString("username");
                backups.add(new Backup(player.getUniqueId(), username, joinTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return backups;
    }

    public List<Backup> getBackupsForDeath(Player player, int maxBackup) {
        List<Backup> backups = new ArrayList<>();
        String query = "SELECT * FROM death_logs WHERE uuid = ? ORDER BY death_time DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, maxBackup);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long deathTime = rs.getLong("death_time");
                String username = rs.getString("username");
                backups.add(new Backup(player.getUniqueId(), username, deathTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return backups;
    }

    public List<Backup> getBackupsForQuit(Player player, int maxBackup) {
        List<Backup> backups = new ArrayList<>();
        String query = "SELECT * FROM quit_logs WHERE uuid = ? ORDER BY quit_time DESC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, maxBackup);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long quitTime = rs.getLong("quit_time");
                String username = rs.getString("username");
                backups.add(new Backup(player.getUniqueId(), username, quitTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return backups;
    }

    public ItemStack[] getInventory(Player player, long timestamp) {
        String query = "SELECT inventory FROM inventories WHERE uuid = ? AND timestamp = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setLong(2, timestamp);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] inventoryBytes = rs.getBytes("inventory");
                if (inventoryBytes != null && inventoryBytes.length > 0) {
                    return deserializeInventory(inventoryBytes);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];
    }

    public ItemStack[] getArmor(Player player, long timestamp) {
        String query = "SELECT armor FROM inventories WHERE uuid = ? AND timestamp = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setLong(2, timestamp);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] armorBytes = rs.getBytes("armor");
                if (armorBytes != null && armorBytes.length > 0) {
                    return deserializeInventory(armorBytes);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];
    }

    private ItemStack[] deserializeInventory(byte[] data) {
        if (data == null || data.length == 0) {
            return new ItemStack[0];
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            List<Map<String, Object>> serializedItems = (List<Map<String, Object>>) in.readObject();
            ItemStack[] items = new ItemStack[serializedItems.size()];
            for (int i = 0; i < serializedItems.size(); i++) {
                items[i] = ItemStack.deserialize(serializedItems.get(i));
            }
            return items;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}