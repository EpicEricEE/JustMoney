package de.epiceric.justmoney.command.subcommand;

import java.util.Optional;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.model.BankAccount;

/**
 * The executor and tab completer for the "/money give ..." sub command.
 * 
 * @since 1.1
 */
public class GiveSubCommand extends SetSubCommand {
    public GiveSubCommand(JustMoney plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public boolean onExecute(Player player, String label, String... args) {
        if (args.length > 3 || args.length < 1) {
            return false;
        }

        if (!isMultiWorld() && args.length > 2) {
            return false;
        }

        if (!isPermitted(player)) {
            sendMessage(player, getErrorMessage("no-permission"));
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("justmoney.set.self")) {
                return false;
            }

            BankAccount account = plugin.getBankManager().getBankAccount(player);
            try {
                double amount = Double.parseDouble(args[0]);

                if (amount < 0) {
                    sendMessage(player, getErrorMessage("cannot-give-negative"));
                    return true;
                }

                account.deposit(player.getWorld(), amount);
                if (isMultiWorld()) {
                    sendMessage(player, getMessage("give-your-balance-current-world"), plugin.format(amount));
                } else {
                    sendMessage(player, getMessage("give-your-balance"), plugin.format(amount));
                }
            } catch (NumberFormatException ex) {
                sendMessage(player, getErrorMessage("cannot-parse-amount"), args[1]);
            }
            return true;
        }

        int amountIndex;
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            amountIndex = 1;
        } catch (NumberFormatException ex) {
            if (args.length == 3) {
                sendMessage(player, getErrorMessage("cannot-parse-amount"), args[1]);
                return true;
            }

            try {
                amount = Double.parseDouble(args[0]);
                amountIndex = 0;
            } catch (NumberFormatException ex2) {
                sendMessage(player, getErrorMessage("cannot-parse-amount"), args[0] + " / " + args[1]);
                return true;
            }    
        }

        if (amountIndex == 0 && !player.hasPermission("justmoney.set.self")) {
            return false;
        }

        if (amountIndex == 1 && !player.hasPermission("justmoney.set.other")) {
            return false;
        }

        BankAccount account = plugin.getBankManager().getBankAccount(
                amountIndex == 0 ? player: getOfflinePlayer(args[0]));

        if (account == null) {
            sendMessage(player, getErrorMessage("cannot-find-player"), args[0]);
            return true;
        }

        if (amount < 0) {
            sendMessage(player, getErrorMessage("cannot-give-negative"));
            return true;
        }

        Optional<Player> accountOwner = Optional.ofNullable(account.getOwner().getPlayer());
        final double _amount = amount; // For use in enclosing scope

        if (isMultiWorld()) {
            World world = args.length == 3
                ? plugin.getServer().getWorld(args[2])
                : args.length == 2
                    ? plugin.getServer().getWorld(args[1])
                    : player.getWorld();

            if (world == null) {
                sendMessage(player, getErrorMessage("cannot-find-world"), args[args.length - 1]);
                return true;
            }

            account.deposit(world, amount);

            if (account.getOwner().getName().equals(player.getName())) {
                sendMessage(player, getMessage("give-your-balance-in-world"),
                        world.getName(), plugin.format(amount));
            } else {
                sendMessage(player, getMessage("give-player-balance-in-world"),
                        account.getOwner().getName(), world.getName(), plugin.format(amount));

                accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("give-your-balance-in-world"),
                        world.getName(), plugin.format(_amount)));
            }
            
        } else {
            account.deposit(amount);

            if (account.getOwner().getName().equals(player.getName())) {
                sendMessage(player, getMessage("give-your-balance"), plugin.format(amount));
            } else {
                sendMessage(player, getMessage("give-player-balance"),
                        account.getOwner().getName(), plugin.format(amount));

                accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("give-your-balance"),
                        plugin.format(_amount)));
            }
        }

        return true;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String... args) {
        if (isMultiWorld() && args.length != 3) {
            return false;
        }

        if (!isMultiWorld() && args.length != 2) {
            return false;
        }

        if (!sender.hasPermission("justmoney.set.other")) {
            sendMessage(sender, getErrorMessage("no-permission"));
            return true;
        }

        BankAccount account = plugin.getBankManager().getBankAccount(getOfflinePlayer(args[0]));
        if (account == null) {
            sendMessage(sender, getErrorMessage("cannot-find-player"), args[0]);
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            sendMessage(sender, getErrorMessage("cannot-parse-amount"), args[1]);
            return true;
        }

        if (amount < 0) {
            sendMessage(sender, getErrorMessage("cannot-give-negative"));
            return true;
        }

        Optional<Player> accountOwner = Optional.ofNullable(account.getOwner().getPlayer());

        if (isMultiWorld()) {
            World world = plugin.getServer().getWorld(args[2]);
            if (world == null) {
                sendMessage(sender, getErrorMessage("cannot-find-world"), args[2]);
                return true;
            }

            account.deposit(world, amount);

            sendMessage(sender, getMessage("give-player-balance-in-world"),
                    account.getOwner().getName(), world.getName(), plugin.format(amount));

            accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("give-your-balance-in-world"),
                    world.getName(), plugin.format(amount)));
        } else {
            account.deposit(amount);

            sendMessage(sender, getMessage("give-player-balance"),
                    account.getOwner().getName(), plugin.format(amount));

            accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("give-your-balance"),
                    plugin.format(amount)));
        }

        return true;
    }
}