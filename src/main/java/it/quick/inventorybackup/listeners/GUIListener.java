package it.quick.inventorybackup.listeners;

import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.commands.InventoryBackupCommand;
import it.quick.inventorybackup.database.Backup;
import it.quick.inventorybackup.gui.DeathBackupGUI;
import it.quick.inventorybackup.gui.JoinBackupGUI;
import it.quick.inventorybackup.gui.QuitBackupGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIListener implements Listener {

    private final InventoryBackup plugin;
    private final InventoryBackupCommand inventoryBackupCommand;

    public GUIListener(InventoryBackup plugin, InventoryBackupCommand inventoryBackupCommand) {
        this.plugin = plugin;
        this.inventoryBackupCommand = inventoryBackupCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.GOLD + "Backup Inventari") && !player.hasPermission("inventorybackupper.use")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Non hai il permesso di aprire questa GUI.");
            return;
        }

        if (title.equals(ChatColor.GOLD + "Backup Inventari")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                if (meta != null) {
                    if (event.getCurrentItem().getType() == Material.GREEN_WOOL && meta.getDisplayName().equals(ChatColor.GREEN + "Join")) {
                        new JoinBackupGUI(plugin).open(player);
                    } else if (event.getCurrentItem().getType() == Material.RED_WOOL && meta.getDisplayName().equals(ChatColor.RED + "Deaths")) {
                        new DeathBackupGUI(plugin).open(player);
                    } else if (event.getCurrentItem().getType() == Material.WHITE_WOOL && meta.getDisplayName().equals(ChatColor.WHITE + "Quit")) {
                        new QuitBackupGUI(plugin).open(player);
                    }
                }
            }
        }

        if (title.contains("Backup Quit") || title.contains("Backup Join") || title.contains("Backup Morti")) {
            if (event.getCurrentItem() != null) {
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                if (meta != null && meta.getLore() != null && !meta.getLore().isEmpty()) {
                    String loreLine = meta.getLore().get(0);
                    String[] parts = loreLine.split(": ");
                    if (parts.length > 1) {
                        long timestamp = Long.parseLong(parts[1]);
                        Backup backup = new Backup(player.getUniqueId(), player.getName(), timestamp);

                        if (title.contains("Quit")) {
                            new QuitBackupGUI(plugin).openFullBackup(player, backup);
                        } else if (title.contains("Join")) {
                            new JoinBackupGUI(plugin).openFullBackup(player, backup);
                        } else if (title.contains("Morti")) {
                            new DeathBackupGUI(plugin).openFullBackup(player, backup);
                        }
                    }
                }
            }
            if (!player.hasPermission("inventorybackupper.interact")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("Backup Inventari") || title.contains("Backup Quit")
                || title.contains("Backup Join") || title.contains("Backup Morti")
                || title.contains("Backup Completo") || title.contains("Inventario Backup")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        event.setCancelled(true);
    }
}