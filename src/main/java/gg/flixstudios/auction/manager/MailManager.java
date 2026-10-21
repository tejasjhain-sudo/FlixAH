package gg.flixstudios.auction.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores items for players when their inventory is full or auctions expire offline.
 */
public class MailManager {
    private final Map<UUID, List<ItemStack>> playerMail = new ConcurrentHashMap<>();

    public void addMail(UUID playerUuid, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        playerMail.computeIfAbsent(playerUuid, k -> Collections.synchronizedList(new ArrayList<>())).add(item.clone());
    }

    public List<ItemStack> getMail(UUID playerUuid) {
        return playerMail.getOrDefault(playerUuid, Collections.emptyList());
    }

    public boolean claimAll(Player player) {
        List<ItemStack> mail = playerMail.get(player.getUniqueId());
        if (mail == null || mail.isEmpty()) {
            return false;
        }

        Iterator<ItemStack> iterator = mail.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            if (overflow.isEmpty()) {
                iterator.remove();
            } else {
                break; // Inventory became full
            }
        }

        if (mail.isEmpty()) {
            playerMail.remove(player.getUniqueId());
        }
        return true;
    }
}
