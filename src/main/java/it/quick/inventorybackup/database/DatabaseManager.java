package it.quick.inventorybackup;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.sql.*;
import java.util.Arrays;

public class DatabaseManager {

    private final String DATABASE_URL = "jdbc:sqlite:plugins/InventoryBackup/InventoryBackup/inventory.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            // Crea la connessione al database
            connection = DriverManager.getConnection(DATABASE_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // Crea la tabella per gli inventari
        String createInventoryTableSQL = "CREATE TABLE IF NOT EXISTS inventories ("
                + "uuid TEXT PRIMARY KEY,"
                + "inventory BLOB,"
                + "armor BLOB,"
                + "timestamp INTEGER"
                + ");";

        // Crea la tabella per i log di join
        String createJoinLogTableSQL = "CREATE TABLE IF NOT EXISTS join_logs ("
                + "uuid TEXT,"
                + "username TEXT,"
                + "join_time INTEGER,"
                + "PRIMARY KEY (uuid, join_time)"
                + ");";

        // Crea la tabella per i log di morte
        String createDeathLogTableSQL = "CREATE TABLE IF NOT EXISTS death_logs ("
                + "uuid TEXT,"
                + "username TEXT,"
                + "death_time INTEGER,"
                + "cause TEXT,"
                + "PRIMARY KEY (uuid, death_time)"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createInventoryTableSQL);
            stmt.executeUpdate(createJoinLogTableSQL);
            stmt.executeUpdate(createDeathLogTableSQL);
        }
    }

    // Funzione per salvare l'inventario
    public void saveInventory(Player player, ItemStack[] inventory, ItemStack[] armor) {
        String query = "INSERT OR REPLACE INTO inventories (uuid, inventory, armor, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setBytes(2, serializeInventory(inventory));
            stmt.setBytes(3, serializeInventory(armor));
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funzione per loggare l'evento di join
    public void logPlayerJoin(Player player) {
        String query = "INSERT INTO join_logs (uuid, username, join_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funzione per loggare l'evento di morte
    public void logPlayerDeath(Player player, String cause) {
        String query = "INSERT INTO death_logs (uuid, username, death_time, cause) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setString(4, cause);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funzione per recuperare l'inventario di un giocatore
    public ItemStack[] getInventory(Player player) {
        String query = "SELECT inventory FROM inventories WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] inventoryBytes = rs.getBytes("inventory");
                return deserializeInventory(inventoryBytes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];  // Restituisce un inventario vuoto se non trovato
    }

    // Funzione per recuperare l'armatura di un giocatore
    public ItemStack[] getArmor(Player player) {
        String query = "SELECT armor FROM inventories WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] armorBytes = rs.getBytes("armor");
                return deserializeInventory(armorBytes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];  // Restituisce un'armatura vuota se non trovata
    }

    // Serializzazione e deserializzazione dell'inventario
    private byte[] serializeInventory(ItemStack[] items) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(items);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private ItemStack[] deserializeInventory(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (ItemStack[]) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
