package it.quick.inventorybackup.listeners;

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

        databaseManager.logPlayerJoin(player, joinTime);

        plugin.getServer().getScheduler().runTask(plugin, () -> {

            ItemStack[] inventory = databaseManager.getInventory(player, joinTime);
            ItemStack[] armor = databaseManager.getArmor(player, joinTime);

            if (inventory.length > 0 || armor.length > 0) {
                player.getInventory().setContents(inventory);
                player.getInventory().setArmorContents(armor);
            }
        });
    }
}