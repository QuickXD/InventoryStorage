package it.quick.inventorybackup.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.database.DatabaseManager;

public class DeathListener implements Listener {

    private final InventoryBackup plugin;
    private final DatabaseManager databaseManager;

    public DeathListener(InventoryBackup plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player instanceof Player) {
            String cause = event.getDeathMessage() != null ? event.getDeathMessage() : "Unknown";
            long deathTime = System.currentTimeMillis();

            databaseManager.logPlayerDeath(player, cause, deathTime);

            ItemStack[] inventory = player.getInventory().getContents();
            ItemStack[] armor = player.getInventory().getArmorContents();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                databaseManager.saveInventory(player, inventory, armor, deathTime);
            });
        }
    }
}