package it.quick.inventorybackup;

import it.quick.inventorybackup.commands.LoadInvCommand;
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

        databaseManager = new DatabaseManager(configManager);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GUIListener(this), this);
        pluginManager.registerEvents(new JoinListener(this), this);
        pluginManager.registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);

        this.getCommand("inventorybackupper").setExecutor(new LoadInvCommand(this));
    }

    @Override
    public void onDisable() {

        databaseManager.close();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}