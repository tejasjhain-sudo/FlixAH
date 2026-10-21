package gg.flixstudios.auction.command;

import gg.flixstudios.auction.FlixAH;
import gg.flixstudios.auction.gui.AuctionGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Handles /ah, /ah sell <price>, /ah mail, /ah reload.
 */
public class AHCommand implements CommandExecutor {
    private final FlixAH plugin;

    public AHCommand(FlixAH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("messages.must-be-player"));
            return true;
        }

        // Default: Open Auction House GUI
        if (args.length == 0) {
            if (!player.hasPermission("flixah.use")) {
                player.sendMessage(plugin.getMessage("messages.no-permission"));
                return true;
            }
            AuctionGUI.open(player, plugin, 1);
            return true;
        }

        String sub = args[0].toLowerCase();

        // /ah sell <price>
        if (sub.equals("sell")) {
            if (!player.hasPermission("flixah.sell")) {
                player.sendMessage(plugin.getMessage("messages.no-permission"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /ah sell <price>");
                return true;
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) {
                player.sendMessage(plugin.getMessage("messages.hold-item"));
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessage("messages.invalid-price"));
                return true;
            }

            double min = plugin.getConfig().getDouble("settings.min-price", 10.0);
            double max = plugin.getConfig().getDouble("settings.max-price", 1000000000.0);

            if (price < min) {
                player.sendMessage(plugin.getMessage("messages.price-too-low", "{min}", String.valueOf(min)));
                return true;
            }

            if (price > max) {
                player.sendMessage(plugin.getMessage("messages.price-too-high", "{max}", String.valueOf(max)));
                return true;
            }

            // Check listing fee / tax
            double taxPercent = plugin.getConfig().getDouble("settings.listing-tax-percent", 2.5);
            double taxAmount = (price * taxPercent) / 100.0;
            if (taxAmount > 0) {
                if (!plugin.getEconomyManager().has(player, taxAmount)) {
                    player.sendMessage(plugin.getMessage("messages.tax-cannot-afford", "{tax}", plugin.getEconomyManager().format(taxAmount)));
                    return true;
                }
                plugin.getEconomyManager().withdraw(player, taxAmount);
            }

            String itemName = hand.hasItemMeta() && hand.getItemMeta().hasDisplayName()
                    ? hand.getItemMeta().getDisplayName()
                    : hand.getType().name().replace("_", " ").toLowerCase();

            if (plugin.getAuctionManager().createListing(player, hand, price)) {
                player.sendMessage(plugin.getMessage("messages.item-listed",
                        "{item}", itemName,
                        "{price}", plugin.getEconomyManager().format(price)));
            }
            return true;
        }

        // /ah mail
        if (sub.equals("mail")) {
            if (plugin.getMailManager().claimAll(player)) {
                player.sendMessage(ChatColor.GREEN + "Claimed all items from your mail!");
            } else {
                player.sendMessage(ChatColor.YELLOW + "You have no items waiting in your mail.");
            }
            return true;
        }

        // /ah reload
        if (sub.equals("reload")) {
            if (!player.hasPermission("flixah.admin")) {
                player.sendMessage(plugin.getMessage("messages.no-permission"));
                return true;
            }
            plugin.reloadPluginConfig();
            player.sendMessage(plugin.getMessage("messages.reloaded"));
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Usage: /ah [sell <price>|mail|reload]");
        return true;
    }
}
