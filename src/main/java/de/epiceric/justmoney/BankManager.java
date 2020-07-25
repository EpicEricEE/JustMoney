package de.epiceric.justmoney;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import de.epiceric.justmoney.model.BankAccount;

/**
 * Class for bank account access.
 * 
 * @since 1.0
 */
public class BankManager {
    private static BankManager instance;

    /**
     * Gets the singleton bank manager instance.
     * 
     * @param plugin an instance of the JustMoney plugin
     * @return the bank manager
     * @since 1.0
     * @see JustMoney#getBankManager()
     */
    public static BankManager get(JustMoney plugin) {
        if (instance == null) {
            instance = new BankManager(plugin);
        }
        return instance;
    }

    private final JustMoney plugin;
    private final Map<UUID, BankAccount> bankAccounts = new HashMap<>();

    private BankManager(JustMoney plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the bank account for the given player.
     * <p>
     * If none exists yet, a new one with the configured starting balance is created.
     * 
     * @param owner the player whose account to get
     * @return a bank account
     * @since 1.0
     */
    public BankAccount getBankAccount(OfflinePlayer owner) {
        if (owner == null) {
            return null;
        }

        if (!bankAccounts.containsKey(owner.getUniqueId())) {
            bankAccounts.put(owner.getUniqueId(), new BankAccount(plugin, owner));
        }
        return bankAccounts.get(owner.getUniqueId());
    }

    /**
     * Loads the given bank accounts so their balance is stored.
     * 
     * @param accounts the collection of accounts to load
     * @since 1.0
     */
    protected void loadAccounts(Collection<BankAccount> accounts) {
        accounts.forEach(account -> bankAccounts.put(account.getOwner().getUniqueId(), account));
        plugin.getLogger().info(accounts.size() + " bank accounts have been loaded");
    }
}