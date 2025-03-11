package it.quick.inventorybackup;

import it.quick.inventorybackup.commands.InventoryBackupCommand;
import it.quick.inventorybackup.database.DatabaseManager;
import it.quick.inventorybackup.config.ConfigManager;
import it.quick.inventorybackup.listeners.GUIListener;
import it.quick.inventorybackup.listeners.JoinListener;
import it.quick.inventorybackup.listeners.DeathListener;
import it.quick.inventorybackup.listeners.QuitListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class InventoryBackup extends JavaPlugin {

    private DatabaseManager databaseManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this, configManager);

        this.getCommand("inventorybackupper").setExecutor(new InventoryBackupCommand(this, databaseManager));

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GUIListener(this, new InventoryBackupCommand(this, databaseManager)), this);
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new DeathListener(this), this);
        pluginManager.registerEvents(new QuitListener(this), this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}