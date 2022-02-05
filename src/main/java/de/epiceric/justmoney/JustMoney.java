package de.epiceric.justmoney;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import de.epiceric.justmoney.command.CommandManager;
import de.epiceric.justmoney.model.BankAccount;
import de.epiceric.justmoney.storage.BankStorage;
import de.epiceric.justmoney.storage.FileStorage;
import de.epiceric.justmoney.storage.sql.MySqlStorage;
import de.epiceric.justmoney.storage.sql.SqLiteStorage;
import net.milkbowl.vault.economy.Economy;

/**
 * Main class of the JustMoney Bukkit plugin.
 * 
 * @since 1.0
 */
public class JustMoney extends JavaPlugin {
    private BankStorage storage;

    /**
     * Connects to the Spiget API to check for latest uploaded version.
     * 
     * @return a future that completes when a result has been obtained
     * @since 1.0
     */
    private CompletableFuture<Void> checkUpdate() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                getLogger().info("Checking for updates...");

                URL url = new URL("https://api.spiget.org/v2/resources/81941/versions?size=1&page=1&sort=-releaseDate");
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "JustMoney/UpdateChecker");

                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonElement element = JsonParser.parseReader(reader);

                if (element.isJsonArray()) {
                    JsonObject result = element.getAsJsonArray().get(0).getAsJsonObject();
                    String version = result.get("name").getAsString();
                    
                    if (!version.equals(getDescription().getVersion())) {
                        getLogger().warning("Version " + version + " of JustMoney is available. You are running version " + getDescription().getVersion());
                        getLogger().warning("Download here: " + getDescription().getWebsite());
                    } else {
                        getLogger().info("You are running the latest version (" + getDescription().getVersion() + ")");
                    }
                } else {
                    getLogger().severe("Failed to check for updates: Unexpected Result: " + element.toString());
                }
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to check for updates", ex);
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();

        String storageType = getConfig().getString("storage-type");
        switch (storageType.toLowerCase()) {
            case "sqlite":
                storage = new SqLiteStorage(this);
                break;
            case "mysql":
                storage = new MySqlStorage(this);
                break;
            case "flatfile":
                storage = new FileStorage(this);
                break;
            default:
                getLogger().warning("Invalid storage type: " + storageType);
                getLogger().warning("Using flatfile storage as fallback");
                storage = new FileStorage(this);
        }

        storage.getAccounts()
            .thenAccept(getBankManager()::loadAccounts)
            .exceptionally(ex -> {
                getLogger().log(Level.SEVERE, "Failed to load bank accounts", ex);
                getLogger().log(Level.SEVERE, "Plugin will be disabled");
                getServer().getPluginManager().disablePlugin(this);
                return null;
            });
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            Economy economy = new VaultEconomy(this);
            getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Normal);
        }

        CommandManager command = new CommandManager(this);
        getCommand("money").setExecutor(command);
        getCommand("money").setTabCompleter(command);

        Metrics metrics = new Metrics(this, 8256);
        metrics.addCustomChart(new SimplePie("storage_type", () -> getStorage().getTypeName()));
        metrics.addCustomChart(new SimplePie("multi_world", () ->
                getConfig().getBoolean("multi-world") ? "Enabled" : "Disabled"));

        checkUpdate();
    }

    /**
     * Gets the singleton bank manager instance.
     * 
     * @return the bank manager
     * @since 1.0
     * @see BankManager#get(JustMoney)
     */
    public BankManager getBankManager() {
        return BankManager.get(this);
    }

    /**
     * Formats the given balance in a more user friendly currency format.
     * 
     * @param balance the balance to format
     * @return the formatted string
     * @since 1.0
     * @see BankAccount#formatBalance()
     */
    public String format(double balance) {
        int decimalPlaces = getConfig().getInt("formatting.decimal-places");
        String value = String.format("%,." + decimalPlaces + "f", balance);
        String format =  getConfig().getString("formatting.format");
        return format.replace("{value}", value).replace("{sign}", getConfig().getString("formatting.sign"));
    }

    /**
     * Gets an instance of the bank storage implementation.
     * 
     * @return the bank storage
     * @since 1.0
     */
    public BankStorage getStorage() {
        return storage;
    }
}
