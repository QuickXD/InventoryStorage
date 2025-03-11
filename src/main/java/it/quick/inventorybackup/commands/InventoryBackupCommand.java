package it.quick.inventorybackup.commands;

import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.database.DatabaseManager;
import it.quick.inventorybackup.database.Backup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class InventoryBackupCommand implements CommandExecutor {

    private final InventoryBackup plugin;
    private final DatabaseManager databaseManager;

    public InventoryBackupCommand(InventoryBackup plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso corretto: /inventorybackupper <loadinv|info> [<player>]");
            return false;
        }

        if (args[0].equalsIgnoreCase("loadinv")) {
            return handleLoadInvCommand(sender, args);
        } else if (args[0].equalsIgnoreCase("info")) {
            return handleInfoCommand(sender);
        } else {
            sender.sendMessage(ChatColor.RED + "Comando sconosciuto. Usa /inventorybackupper <loadinv|info> [<player>]");
            return false;
        }
    }

    private boolean handleLoadInvCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Questo comando può essere eseguito solo da un giocatore!");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("inventorybackupper.use")) {
            player.sendMessage(ChatColor.RED + "Non hai il permesso di usare questo comando.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso corretto: /inventorybackupper loadinv <player>");
            return false;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.RED + "Il giocatore " + playerName + " non è online!");
            return false;
        }

        openMainInventory(player, targetPlayer);
        return true;
    }

    private boolean handleInfoCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Questo comando può essere eseguito solo da un giocatore!");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("inventorybackupper.info")) {
            player.sendMessage(ChatColor.RED + "Non hai il permesso di usare questo comando.");
            return true;
        }

        List<Backup> joinBackups = databaseManager.getBackupsForJoin(player, Integer.MAX_VALUE);
        List<Backup> deathBackups = databaseManager.getBackupsForDeath(player, Integer.MAX_VALUE);
        List<Backup> quitBackups = databaseManager.getBackupsForQuit(player, Integer.MAX_VALUE);

        int maxJoinBackups = plugin.getConfigManager().getMaxBackup("Join");
        int maxDeathBackups = plugin.getConfigManager().getMaxBackup("Death");
        int maxQuitBackups = plugin.getConfigManager().getMaxBackup("Quit");

        int totalBackups = Math.min(joinBackups.size(), maxJoinBackups) +
                Math.min(deathBackups.size(), maxDeathBackups) +
                Math.min(quitBackups.size(), maxQuitBackups);

        player.sendMessage(ChatColor.GOLD + "InventoryStorage -> Hai " + totalBackups + " backup disponibili!");

        return true;
    }

    public void openMainInventory(Player player, Player targetPlayer) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Backup Inventari");

        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Nickname");
            meta.setLore(Collections.singletonList(ChatColor.YELLOW + targetPlayer.getName()));
            feather.setItemMeta(meta);
        }
        gui.setItem(0, feather);

        ItemStack joinButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta joinMeta = joinButton.getItemMeta();
        if (joinMeta != null) {
            joinMeta.setDisplayName(ChatColor.GREEN + "Join");
            joinButton.setItemMeta(joinMeta);
        }
        gui.setItem(3, joinButton);

        ItemStack deathButton = new ItemStack(Material.RED_WOOL);
        ItemMeta deathMeta = deathButton.getItemMeta();
        if (deathMeta != null) {
            deathMeta.setDisplayName(ChatColor.RED + "Deaths");
            deathButton.setItemMeta(deathMeta);
        }
        gui.setItem(5, deathButton);

        ItemStack quitButton = new ItemStack(Material.WHITE_WOOL);
        ItemMeta quitMeta = quitButton.getItemMeta();
        if (quitMeta != null) {
            quitMeta.setDisplayName(ChatColor.WHITE + "Quit");
            quitButton.setItemMeta(quitMeta);
        }
        gui.setItem(4, quitButton);

        player.openInventory(gui);
    }
}