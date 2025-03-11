package it.quick.inventorybackup.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.database.DatabaseManager;

public class JoinListener implements Listener {

    private final InventoryBackup plugin;
    private final DatabaseManager databaseManager;

    public JoinListener(InventoryBackup plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long joinTime = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            databaseManager.logPlayerJoin(player, joinTime);

            ItemStack[] inventory = databaseManager.getInventory(player, joinTime);
            ItemStack[] armor = databaseManager.getArmor(player, joinTime);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (inventory.length > 0 || armor.length > 0) {
                    player.getInventory().setContents(inventory);
                    player.getInventory().setArmorContents(armor);
                }
            });
        });
    }
}