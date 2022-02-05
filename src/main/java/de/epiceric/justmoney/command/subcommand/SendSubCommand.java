package de.epiceric.justmoney.command.subcommand;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.exception.NotEnoughMoneyException;
import de.epiceric.justmoney.model.BankAccount;
import de.epiceric.justmoney.util.Util;

/**
 * The executor and tab completer for the "/money send ..." sub command.
 * 
 * @since 1.0
 */
public class SendSubCommand extends SubCommand {
    public SendSubCommand(JustMoney plugin) {
        super("send", plugin);
    }

    @Override
    public boolean isPermitted(CommandSender sender) {
        return sender instanceof Player && sender.hasPermission("justmoney.send");
    }

    @Override
    public boolean onExecute(Player player, String label, String... args) {
        if (args.length != 3 && args.length != 2) {
            return false;
        }

        if (!isMultiWorld() && args.length != 2) {
            return false;
        }

        if (!isPermitted(player)) {
            sendMessage(player, getErrorMessage("no-permission"));
            return true;
        }

        BankAccount receiver = plugin.getBankManager().getBankAccount(getOfflinePlayer(args[0]));
        if (receiver == null) {
            sendMessage(player, getErrorMessage("cannot-find-player"), args[0]);
            return true;
        }

        if (receiver.getOwner().getName().equals(player.getName())) {
            sendMessage(player, getErrorMessage("cannot-send-to-yourself"));
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            sendMessage(player, getErrorMessage("cannot-parse-amount"), args[1]);
            return true;
        }

        if (amount < 0) {
            sendMessage(player, getErrorMessage("cannot-send-negative"));
            return true;
        } else if (amount == 0) {
            sendMessage(player, getErrorMessage("cannot-send-zero"));
            return true;
        }

        BankAccount sender = plugin.getBankManager().getBankAccount(player);

        try {
            if (isMultiWorld()) {
                World world = player.getWorld();
                if (args.length == 3) {
                    world = plugin.getServer().getWorld(args[2]);
                }
                if (world == null) {
                    sendMessage(player, getErrorMessage("cannot-find-world"), args[2]);
                    return true;
                }
                sender.withdraw(world, amount);
                receiver.deposit(world, amount);
            } else {
                sender.withdraw(amount);
                receiver.deposit(amount);
            }

            sendMessage(player, getMessage("sent-money-to"),
                    plugin.format(amount), receiver.getOwner().getName());

            if (receiver.getOwner().isOnline()) {
                sendMessage(receiver.getOwner().getPlayer(), getMessage("received-money-from"),
                        plugin.format(amount), player.getName());
            }
        } catch (NotEnoughMoneyException ex) {
            sendMessage(player, getErrorMessage("not-enough-money"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(Player player, String... args) {
        switch (args.length) {
            case 1:
                return Util.completePlayer(plugin, player);
            case 2:
                int decimals = plugin.getConfig().getInt("formatting.decimal-places");
                return Util.completeAmount(decimals, args[1]);
            case 3:
                if (isMultiWorld()) {
                    return plugin.getServer().getWorlds().stream()
                        .map(World::getName)
                        .collect(Collectors.toList());
                }
        }
        return Collections.emptyList();
    }
}