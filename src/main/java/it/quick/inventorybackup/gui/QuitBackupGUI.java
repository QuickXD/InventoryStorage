package it.quick.inventorybackup.gui;

import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.database.Backup;
import it.quick.inventorybackup.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.util.List;

public class QuitBackupGUI {

    private final InventoryBackup plugin;
    private final DatabaseManager databaseManager;

    public QuitBackupGUI(InventoryBackup plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    public void open(Player player) {
        int maxBackup = plugin.getConfigManager().getMaxBackup("Quit");
        Inventory inventory = plugin.getServer().createInventory(null, 27, ChatColor.GOLD + "Backup Quit");

        List<Backup> backups = databaseManager.getBackupsForQuit(player, maxBackup);

        int slot = 0;
        for (Backup backup : backups) {
            ItemStack backupItem = new ItemStack(Material.BONE);
            ItemMeta meta = backupItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.WHITE + "Backup " + backup.getDateFormatted());
                meta.setLore(List.of(ChatColor.GRAY + "Timestamp: " + backup.getTimestamp()));
                backupItem.setItemMeta(meta);
            }

            inventory.setItem(slot++, backupItem);
        }

        player.openInventory(inventory);
    }

    public void openFullBackup(Player player, Backup backup) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Backup Quit - " + backup.getDateFormatted());

        ItemStack[] backupInventory = plugin.getDatabaseManager().getInventory(player, backup.getTimestamp());
        ItemStack[] backupArmor = plugin.getDatabaseManager().getArmor(player, backup.getTimestamp());

        for (int i = 0; i < backupInventory.length; i++) {
            if (backupInventory[i] != null) {
                inventory.setItem(i, backupInventory[i]);
            }
        }

        for (int i = 0; i < backupArmor.length; i++) {
            if (backupArmor[i] != null) {
                inventory.setItem(i + 36, backupArmor[i]);
            }
        }

        player.openInventory(inventory);
    }
}