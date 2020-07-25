package de.epiceric.justmoney;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

/**
 * The Vault economy hook.
 * 
 * @since 1.0
 */
public final class VaultEconomy implements Economy {
    private final JustMoney plugin;
    private final BankManager manager;

    VaultEconomy(JustMoney plugin) {
        this.plugin = plugin;
        this.manager = plugin.getBankManager();
    }

    /**
     * Gets the offline player with the given name.
     * 
     * @param name the name
     * @return the offline player or an empty optional if none exists
     */
    private Optional<OfflinePlayer> getOfflinePlayer(String name) {
        return Arrays.stream(plugin.getServer().getOfflinePlayers())
            .filter(player -> player.getName().equalsIgnoreCase(name))
            .findAny();
    }

    /**
     * Gets the world with the given name.
     * 
     * @param name the name
     * @return the world or an empty optional if none exists
     */
    private Optional<World> getWorld(String name) {
        return Optional.ofNullable(plugin.getServer().getWorld(name));
    }

    // Economy settings

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("formatting.sign");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("formatting.sign");
    }

    @Override
    public String format(double amount) {
        return plugin.format(amount);
    }

    @Override
    public int fractionalDigits() {
        return plugin.getConfig().getInt("formatting.decimal-places");
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "JustEconomy";
    }

    // Bank accounts are not supported

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "JustMoney does not support bank accounts");
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    // Player account handling

    @Override
    public boolean createPlayerAccount(String playerName) {
        return getOfflinePlayer(playerName).isPresent();
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return getOfflinePlayer(playerName).isPresent() && getWorld(worldName).isPresent();
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return getWorld(worldName).isPresent();
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return getOfflinePlayer(playerName)
            .map(player -> depositPlayer(player, amount))
            .orElse(new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to find player with name " + playerName));
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        double balance = manager.getBankAccount(player).deposit(amount);
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return getOfflinePlayer(playerName)
            .map(player -> depositPlayer(player, worldName, amount))
            .orElse(new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to find player with name " + playerName));
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return getWorld(worldName)
            .map(world -> {
                double balance = manager.getBankAccount(player).deposit(world, amount);
                return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
            })
            .orElse(new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to find world with name " + worldName));
    }

    @Override
    public double getBalance(String playerName) {
        return getOfflinePlayer(playerName)
            .map(player -> getBalance(player))
            .orElse(0.0);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return manager.getBankAccount(player).getBalance();
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getOfflinePlayer(playerName)
            .map(player -> getBalance(player, worldName))
            .orElse(0.0);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getWorld(worldName)
            .map(world -> manager.getBankAccount(player).getBalance(world))
            .orElse(0.0);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return getBalance(playerName, worldName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return getBalance(player, worldName) >= amount;
    }

    @Override
    public boolean hasAccount(String playerName) {
        return getOfflinePlayer(playerName).isPresent();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return getOfflinePlayer(playerName).isPresent() && getWorld(worldName).isPresent();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return getWorld(worldName).isPresent();
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return getOfflinePlayer(playerName)
            .map(player -> withdrawPlayer(player, amount))
            .orElse(new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to find player with name " + playerName));
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        double balance = getBalance(player);
        if (balance < amount) {
            return new EconomyResponse(0, balance, ResponseType.FAILURE, "Not enough money");
        }
        double newBalance = manager.getBankAccount(player).withdraw(amount);
        return new EconomyResponse(amount, newBalance, ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return getOfflinePlayer(playerName)
            .map(player -> withdrawPlayer(player, worldName, amount))
            .orElse(new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to find player with name " + playerName));
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return getWorld(worldName)
            .map(world -> {
                double balance = getBalance(player, worldName);
                if (balance < amount) {
                    return new EconomyResponse(0, balance, ResponseType.FAILURE, "Not enough money");
                }
                double newBalance = manager.getBankAccount(player).withdraw(world, amount);
                return new EconomyResponse(amount, newBalance, ResponseType.SUCCESS, null);
            })
            .orElse(new EconomyResponse(0, 0, ResponseType.FAILURE, "Failed to find world with name " + worldName));
    }
}