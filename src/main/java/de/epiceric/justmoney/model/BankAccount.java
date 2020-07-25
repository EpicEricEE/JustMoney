package de.epiceric.justmoney.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.exception.NotEnoughMoneyException;

/**
 * Holds the balances of a player.
 * 
 * @since 1.0
 */
public final class BankAccount {
    private final JustMoney plugin;
    private final OfflinePlayer owner;
    private final Map<String, Double> balances = new HashMap<>();

    public BankAccount(JustMoney plugin, OfflinePlayer owner) {
        this(plugin, owner, plugin.getConfig().getDouble("start-balance"));
    }

    public BankAccount(JustMoney plugin, OfflinePlayer owner, double balance) {
        this.plugin = plugin;
        this.owner = owner;
        this.balances.put(getDefaultWorld().getName(), balance);
    }

    public BankAccount(JustMoney plugin, OfflinePlayer owner, Map<String, Double> balances) {
        this.plugin = plugin;
        this.owner = owner;
        this.balances.putAll(balances);
    }

    /**
     * Gets the config value of "multi-world".
     * 
     * @return whether multi-world is enabled
     * @since 1.0
     */
    private boolean isMultiWorld() {
        return plugin.getConfig().getBoolean("multi-world");
    }

    /**
     * Gets the server's default world.
     * 
     * @return the server's default world
     * @since 1.0
     */
    private World getDefaultWorld() {
        return plugin.getServer().getWorlds().get(0);
    }

    /**
     * Rounds the given balance to the configured decimal places.
     * 
     * @param balance the balance
     * @return the rounded balance
     */
    private double round(double balance) {
        int factor = (int) Math.pow(10, plugin.getConfig().getInt("formatting.decimal-places"));
        return Math.round(balance * factor) / factor;
    }

    /**
     * Gets this bank account's owner.
     * 
     * @return the owner of the bank account
     * @since 1.0
     */
    public OfflinePlayer getOwner() {
        return owner;
    }

    /**
     * Formats the account's balance in the given world.
     * 
     * @param world the world
     * @return the formatted balance
     * @since 1.0
     */
    public String formatBalance(World world) {
        return plugin.format(getBalance(world));
    }

    /**
     * Formats the account's balance in the default world.
     * 
     * @return the formatted balance
     * @since 1.0
     */
    public String formatBalance() {
        return formatBalance(getDefaultWorld());
    }

    /**
     * Gets the account's balance in the given world.
     * 
     * @param world the world
     * @return the balance
     * @since 1.0
     */
    public double getBalance(World world) {
        String worldName = isMultiWorld() ? world.getName() : getDefaultWorld().getName();
        return round(balances.getOrDefault(worldName, plugin.getConfig().getDouble("start-balance")));
    }

    /**
     * Gets the account's balance in the default world.
     * 
     * @return the balance
     * @since 1.0
     */
    public double getBalance() {
        return getBalance(getDefaultWorld());
    }

    /**
     * Sets the account's balance in the given world.
     * 
     * @param world the world
     * @param balance the balance to set
     * @return the new balance
     * @throws IllegalArgumentException when balance is negative
     * @since 1.0
     */
    public double setBalance(World world, double balance) throws IllegalArgumentException {
        if (balance < 0) {
            throw new IllegalArgumentException("The balance cannot be negative: " + balance);
        }
        String worldName = isMultiWorld() ? world.getName() : getDefaultWorld().getName();
        balances.put(worldName, round(balance));
        plugin.getStorage().storeAccount(this).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to store account", ex);
            return null;
        });
        return getBalance(world);
    }

    /**
     * Sets the account's balance in the default world.
     * 
     * @param balance the balance to set
     * @return the new balance
     * @throws IllegalArgumentException when balance is negative
     * @since 1.0
     */
    public double setBalance(double balance) throws IllegalArgumentException {
        return setBalance(getDefaultWorld(), balance);
    }

    /**
     * Adds the given amount to the account's balance in the given world.
     * 
     * @param world the world
     * @param amount the money to add
     * @return the new balance
     * @since 1.0
     */
    public double deposit(World world, double amount) {
        return setBalance(world, getBalance() + amount);
    }

    /**
     * Adds the given amount to the account's balance in the default world.
     * 
     * @param balance the money to add
     * @return the new balance
     * @since 1.0
     */
    public double deposit(double balance) {
        return deposit(getDefaultWorld(), balance);
    }

    /**
     * Removes the given amount from the account's balance in the given world.
     * 
     * @param world the world
     * @param amount the amount to withdraw
     * @return the new balance
     * @throws NotEnoughMoneyException when the account does not have enough money
     * @since 1.0
     */
    public double withdraw(World world, double amount) throws NotEnoughMoneyException {
        try {
            return setBalance(world, getBalance() - amount);
        } catch (IllegalArgumentException ex) {
            throw new NotEnoughMoneyException("The account does not have enough money", ex);
        }
    }

    /**
     * Removes the given amount from the account's balance in the default world.
     * 
     * @param amount the amount to withdraw
     * @return the new balance
     * @throws NotEnoughMoneyException when the account does not have enough money
     * @since 1.0
     */
    public double withdraw(double amount) throws NotEnoughMoneyException {
        return withdraw(getDefaultWorld(), amount);
    }
}