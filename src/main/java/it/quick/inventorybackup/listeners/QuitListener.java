package it.quick.inventorybackup.listeners;

import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.database.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;

public class QuitListener implements Listener {

    private final InventoryBackup plugin;
    private final DatabaseManager databaseManager;

    public QuitListener(InventoryBackup plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        long quitTime = System.currentTimeMillis();

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                databaseManager.logPlayerQuit(player, quitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
