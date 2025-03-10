package it.quick.inventorybackup.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void saveDefaultConfig() {

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    public boolean isTriggerEnabled(String trigger) {
        return this.config.getBoolean("triggers." + trigger + ".enabled", true);
    }

    public int getMaxBackup(String trigger) {
        return this.config.getInt("triggers." + trigger + ".max_backup", 10);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }
}