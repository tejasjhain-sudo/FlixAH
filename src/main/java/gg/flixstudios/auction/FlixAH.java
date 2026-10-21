package gg.flixstudios.auction;

import gg.flixstudios.auction.command.AHCommand;
import gg.flixstudios.auction.gui.GUIListener;
import gg.flixstudios.auction.manager.AuctionManager;
import gg.flixstudios.auction.manager.EconomyManager;
import gg.flixstudios.auction.manager.MailManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * FlixAH - Clean, high-performance Auction House for PaperMC.
 * Developed by FlixStudios (Tejas).
 */
public class FlixAH extends JavaPlugin {
    private static FlixAH instance;

    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private MailManager mailManager;

    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config and messages
        saveDefaultConfig();
        loadMessages();

        // Setup Vault Economy
        this.economyManager = new EconomyManager();
        if (!economyManager.setupEconomy()) {
            getLogger().severe("Vault economy provider not found! Disabling FlixAH...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        this.mailManager = new MailManager();
        this.auctionManager = new AuctionManager(this);

        // Register commands & listeners
        if (getCommand("ah") != null) {
            getCommand("ah").setExecutor(new AHCommand(this));
        }
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Schedule expiration check every 60 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            auctionManager.processExpirations();
        }, 1200L, 1200L);

        getLogger().info("FlixAH v" + getDescription().getVersion() + " by FlixStudios enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.processExpirations();
        }
        getLogger().info("FlixAH has been disabled.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path, String... replacements) {
        String msg = messagesConfig.getString(path, "&cMessage not found: " + path);
        String prefix = messagesConfig.getString("prefix", "&8[&dFlix&bAH&8] &r");

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public static FlixAH getInstance() {
        return instance;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public MailManager getMailManager() {
        return mailManager;
    }
}
