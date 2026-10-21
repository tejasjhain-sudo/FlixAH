package gg.flixstudios.auction.gui;

import gg.flixstudios.auction.FlixAH;
import gg.flixstudios.auction.model.AuctionItem;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

/**
 * Handles clicks inside the Auction House GUI.
 */
public class GUIListener implements Listener {
    private final FlixAH plugin;

    public GUIListener(FlixAH plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.contains("Auction House")) return;

        event.setCancelled(true); // Prevent taking display items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int slot = event.getRawSlot();

        // Previous Page
        if (slot == 45 && clicked.getType().name().contains("ARROW")) {
            int page = parsePage(title);
            AuctionGUI.open(player, plugin, Math.max(1, page - 1));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            return;
        }

        // Next Page
        if (slot == 53 && clicked.getType().name().contains("ARROW")) {
            int page = parsePage(title);
            AuctionGUI.open(player, plugin, page + 1);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            return;
        }

        // Item Slot Clicks (0-44)
        if (slot >= 0 && slot < 45) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            List<String> lore = meta.getLore();
            if (lore == null || lore.isEmpty()) return;

            // Extract UUID from last lore line
            String lastLine = lore.get(lore.size() - 1);
            if (!lastLine.contains("ID: ")) return;

            try {
                UUID auctionId = UUID.fromString(ChatColor.stripColor(lastLine.replace("ID: ", "").trim()));
                AuctionItem item = plugin.getAuctionManager().getListing(auctionId).orElse(null);
                if (item == null) {
                    player.sendMessage(ChatColor.RED + "This auction is no longer available.");
                    player.closeInventory();
                    return;
                }

                if (item.getSellerUuid().equals(player.getUniqueId())) {
                    // Seller wants to cancel
                    if (plugin.getAuctionManager().cancelListing(player, auctionId)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.0f);
                        AuctionGUI.open(player, plugin, parsePage(title));
                    }
                } else {
                    // Buyer wants to purchase
                    if (plugin.getAuctionManager().purchaseItem(player, auctionId)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
                        player.closeInventory();
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private int parsePage(String title) {
        try {
            int start = title.lastIndexOf("(Page ") + 6;
            int end = title.lastIndexOf(")");
            return Integer.parseInt(title.substring(start, end));
        } catch (Exception e) {
            return 1;
        }
    }
}
