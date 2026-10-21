package gg.flixstudios.auction.model;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents a single auction listing on the market.
 */
public class AuctionItem {
    private final UUID id;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack item;
    private final double price;
    private final long listedAt;
    private final long expiresAt;

    public AuctionItem(UUID id, UUID sellerUuid, String sellerName, ItemStack item, double price, long listedAt, long expiresAt) {
        this.id = id;
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.listedAt = listedAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public double getPrice() {
        return price;
    }

    public long getListedAt() {
        return listedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public long getRemainingSeconds() {
        return Math.max(0, (expiresAt - System.currentTimeMillis()) / 1000L);
    }
}
