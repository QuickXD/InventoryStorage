package it.quick.inventorybackup.commands;

import it.quick.inventorybackup.InventoryBackup;
import it.quick.inventorybackup.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class LoadInvCommand implements CommandExecutor {

    private final InventoryBackup plugin;
    private final DatabaseManager databaseManager;

    public LoadInvCommand(InventoryBackup plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            sender.sendMessage(ChatColor.RED + "Uso corretto: /inventorybackupper loadinv <player>");
            return false;
        }

        if (!args[0].equalsIgnoreCase("loadinv")) {
            sender.sendMessage(ChatColor.RED + "Comando sconosciuto. Usa /inventorybackupper loadinv <player>");
            return false;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(playerName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Il giocatore " + playerName + " non è online!");
            return false;
        }

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

        return true;
    }

    public void openMainInventory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Backup Inventari");

        ItemStack joinButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta joinMeta = joinButton.getItemMeta();
        joinMeta.setDisplayName(ChatColor.GREEN + "Join");
        joinButton.setItemMeta(joinMeta);
        gui.setItem(3, joinButton);

        ItemStack deathButton = new ItemStack(Material.RED_WOOL);
        ItemMeta deathMeta = deathButton.getItemMeta();
        deathMeta.setDisplayName(ChatColor.RED + "Deaths");
        deathButton.setItemMeta(deathMeta);
        gui.setItem(5, deathButton);

        ItemStack quitButton = new ItemStack(Material.WHITE_WOOL);
        ItemMeta quitMeta = quitButton.getItemMeta();
        quitMeta.setDisplayName(ChatColor.WHITE + "Quit");
        quitButton.setItemMeta(quitMeta);
        gui.setItem(4, quitButton);

        player.openInventory(gui);
    }
}