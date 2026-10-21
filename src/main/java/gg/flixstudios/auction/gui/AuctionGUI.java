package gg.flixstudios.auction.gui;

import gg.flixstudios.auction.FlixAH;
import gg.flixstudios.auction.model.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and renders the Auction House pagination chest GUI.
 */
public class AuctionGUI {
    private static final int ITEMS_PER_PAGE = 45;

    public static void open(Player player, FlixAH plugin, int page) {
        List<AuctionItem> all = plugin.getAuctionManager().getActiveListings();
        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / ITEMS_PER_PAGE));
        int currentPage = Math.max(1, Math.min(page, totalPages));

        String title = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8» &0Auction House &8(Page {page})")
                        .replace("{page}", String.valueOf(currentPage)));

        Inventory inv = Bukkit.createInventory(player, 54, title);

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, all.size());

        for (int i = startIndex; i < endIndex; i++) {
            AuctionItem auction = all.get(i);
            ItemStack display = auction.getItem();
            ItemMeta meta = display.getItemMeta();

            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(" ");
                lore.add(ChatColor.DARK_GRAY + "-------------------------");
                lore.add(ChatColor.GRAY + "Seller: " + ChatColor.YELLOW + auction.getSellerName());
                lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + plugin.getEconomyManager().format(auction.getPrice()));
                lore.add(ChatColor.GRAY + "Expires in: " + ChatColor.AQUA + formatTime(auction.getRemainingSeconds()));
                lore.add(" ");
                if (auction.getSellerUuid().equals(player.getUniqueId())) {
                    lore.add(ChatColor.RED + "▶ Click to cancel and claim item!");
                } else {
                    lore.add(ChatColor.YELLOW + "▶ Click to purchase this item!");
                }
                lore.add(ChatColor.DARK_GRAY + "ID: " + auction.getId().toString());
                meta.setLore(lore);
                display.setItemMeta(meta);
            }

            inv.setItem(i - startIndex, display);
        }

        // Bottom Navigation Bar
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 45; slot < 54; slot++) {
            inv.setItem(slot, border);
        }

        if (currentPage > 1) {
            inv.setItem(45, createItem(Material.ARROW, ChatColor.YELLOW + "◀ Previous Page (" + (currentPage - 1) + ")"));
        }

        inv.setItem(49, createItem(Material.BOOK, ChatColor.GREEN + "Active Auctions: " + ChatColor.WHITE + all.size()));

        if (currentPage < totalPages) {
            inv.setItem(53, createItem(Material.ARROW, ChatColor.YELLOW + "Next Page (" + (currentPage + 1) + ") ▶"));
        }

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) return String.format("%02dh %02dm", hours, minutes);
        return String.format("%02dm %02ds", minutes, secs);
    }
}
