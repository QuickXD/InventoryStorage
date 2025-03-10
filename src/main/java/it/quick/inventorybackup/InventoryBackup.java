package it.quick.inventorybackup;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryGUI implements Listener {

    private final DatabaseManager databaseManager;

    public InventoryGUI(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    // Metodo per creare la GUI principale da 9 slot
    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Inventory Backupper");

        // Item per "JOIN" nello slot 3
        ItemStack joinItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta joinMeta = joinItem.getItemMeta();
        joinMeta.setDisplayName("JOIN");
        joinItem.setItemMeta(joinMeta);

        // Item per "DEATHS" nello slot 5
        ItemStack deathItem = new ItemStack(Material.RED_WOOL);
        ItemMeta deathMeta = deathItem.getItemMeta();
        deathMeta.setDisplayName("DEATHS");
        deathItem.setItemMeta(deathMeta);

        // Aggiungi gli item alla GUI
        inventory.setItem(3, joinItem);
        inventory.setItem(5, deathItem);

        // Apri la GUI per il giocatore
        player.openInventory(inventory);
    }

    // Metodo per creare la GUI da 27 slot per visualizzare gli inventari di un altro giocatore
    public void openPlayerInventoryGUI(Player player, String targetPlayerUUID) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Player Inventory: " + targetPlayerUUID);

        // Crea l'item per "JOIN" (Diamond Sword) nello slot 13
        ItemStack joinItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta joinMeta = joinItem.getItemMeta();
        joinMeta.setDisplayName("JOIN");
        joinItem.setItemMeta(joinMeta);

        inventory.setItem(13, joinItem);

        // Carica l'inventario del giocatore target (se esiste)
        databaseManager.loadInventoryFromDatabase(player, targetPlayerUUID);

        // Apri la GUI per il giocatore
        player.openInventory(inventory);
    }

    // Metodo per aprire la GUI da 54 slot con l'inventario completo
    public void openFullInventoryGUI(Player player, String targetPlayerUUID) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Full Inventory: " + targetPlayerUUID);

        // Carica l'inventario completo
        databaseManager.loadFullInventoryForPlayer(player, targetPlayerUUID);

        // Apri la GUI per il giocatore
        player.openInventory(inventory);
    }

    // Gestisci l'interazione con la GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Inventory Backupper")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GREEN_WOOL) {
                Player player = (Player) event.getWhoClicked();
                openPlayerInventoryGUI(player, player.getUniqueId().toString()); // Apri la GUI per il proprio inventario
            }
        }

        if (event.getView().getTitle().startsWith("Player Inventory:")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.DIAMOND_SWORD) {
                Player player = (Player) event.getWhoClicked();
                String targetPlayerUUID = event.getView().getTitle().replace("Player Inventory: ", "");

                openFullInventoryGUI(player, targetPlayerUUID);
            }
        }
    }

    // Gestisci la chiusura della GUI
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Inventory Backupper")) {

        }
    }
}
