package gg.flixstudios.auction.manager;

import gg.flixstudios.auction.FlixAH;
import gg.flixstudios.auction.model.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core auction house engine handling listings, purchases, and expiration checks.
 */
public class AuctionManager {
    private final FlixAH plugin;
    private final Map<UUID, AuctionItem> activeAuctions = new ConcurrentHashMap<>();

    public AuctionManager(FlixAH plugin) {
        this.plugin = plugin;
    }

    /**
     * Lists an item on the auction house with atomic inventory removal.
     */
    public boolean createListing(Player seller, ItemStack item, double price) {
        if (item == null || item.getType().isAir() || price <= 0) {
            return false;
        }

        // Check active listings cap
        long currentCount = activeAuctions.values().stream()
                .filter(a -> a.getSellerUuid().equals(seller.getUniqueId()))
                .count();
        int maxAllowed = plugin.getConfig().getInt("settings.max-listings-default", 5);
        if (currentCount >= maxAllowed) {
            seller.sendMessage(plugin.getMessage("messages.max-listings-reached", "{max}", String.valueOf(maxAllowed)));
            return false;
        }

        // Take item from main hand
        ItemStack toList = item.clone();
        seller.getInventory().setItemInMainHand(null);

        long hours = plugin.getConfig().getLong("settings.listing-duration-hours", 24L);
        long expiresAt = System.currentTimeMillis() + (hours * 3600L * 1000L);

        AuctionItem auction = new AuctionItem(
                UUID.randomUUID(),
                seller.getUniqueId(),
                seller.getName(),
                toList,
                price,
                System.currentTimeMillis(),
                expiresAt
        );

        activeAuctions.put(auction.getId(), auction);
        return true;
    }

    /**
     * Buys an auction item atomically, transferring funds and delivering the item.
     */
    public synchronized boolean purchaseItem(Player buyer, UUID auctionId) {
        AuctionItem auction = activeAuctions.get(auctionId);
        if (auction == null || auction.isExpired()) {
            return false;
        }

        if (auction.getSellerUuid().equals(buyer.getUniqueId())) {
            buyer.sendMessage(plugin.getMessage("messages.cannot-buy-own"));
            return false;
        }

        EconomyManager eco = plugin.getEconomyManager();
        if (!eco.has(buyer, auction.getPrice())) {
            buyer.sendMessage(plugin.getMessage("messages.cannot-afford", "{price}", eco.format(auction.getPrice())));
            return false;
        }

        // Withdraw from buyer
        if (!eco.withdraw(buyer, auction.getPrice())) {
            return false;
        }

        // Remove from market immediately to prevent concurrent purchase
        activeAuctions.remove(auctionId);

        // Deposit to seller
        eco.deposit(Bukkit.getOfflinePlayer(auction.getSellerUuid()), auction.getPrice());

        // Deliver item or put in mail
        HashMap<Integer, ItemStack> overflow = buyer.getInventory().addItem(auction.getItem());
        if (!overflow.isEmpty()) {
            overflow.values().forEach(item -> plugin.getMailManager().addMail(buyer.getUniqueId(), item));
            buyer.sendMessage(plugin.getMessage("messages.inventory-full"));
        }

        // Notify parties
        buyer.sendMessage(plugin.getMessage("messages.item-purchased-buyer",
                "{item}", getItemName(auction.getItem()),
                "{price}", eco.format(auction.getPrice())));

        Player seller = Bukkit.getPlayer(auction.getSellerUuid());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage(plugin.getMessage("messages.item-sold-seller",
                    "{item}", getItemName(auction.getItem()),
                    "{buyer}", buyer.getName(),
                    "{price}", eco.format(auction.getPrice())));
        }

        return true;
    }

    /**
     * Cancels an active auction and returns the item to the seller.
     */
    public synchronized boolean cancelListing(Player player, UUID auctionId) {
        AuctionItem auction = activeAuctions.get(auctionId);
        if (auction == null) return false;

        if (!auction.getSellerUuid().equals(player.getUniqueId()) && !player.hasPermission("flixah.admin")) {
            return false;
        }

        activeAuctions.remove(auctionId);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(auction.getItem());
        if (!overflow.isEmpty()) {
            overflow.values().forEach(item -> plugin.getMailManager().addMail(player.getUniqueId(), item));
        }

        player.sendMessage(plugin.getMessage("messages.auction-cancelled"));
        return true;
    }

    /**
     * Checks and moves expired items to mail.
     */
    public void processExpirations() {
        Iterator<Map.Entry<UUID, AuctionItem>> it = activeAuctions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, AuctionItem> entry = it.next();
            if (entry.getValue().isExpired()) {
                plugin.getMailManager().addMail(entry.getValue().getSellerUuid(), entry.getValue().getItem());
                it.remove();
            }
        }
    }

    public List<AuctionItem> getActiveListings() {
        return new ArrayList<>(activeAuctions.values());
    }

    public Optional<AuctionItem> getListing(UUID id) {
        return Optional.ofNullable(activeAuctions.get(id));
    }

    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }
}
