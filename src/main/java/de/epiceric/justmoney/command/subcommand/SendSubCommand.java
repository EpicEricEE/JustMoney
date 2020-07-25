package de.epiceric.justmoney.command.subcommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
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
        return sender.hasPermission("justmoney.send");
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
            sendMessage(player, "§cYou don't have permission to execute this command.");
            return true;
        }

        BankAccount receiver = plugin.getBankManager().getBankAccount(getOfflinePlayer(args[0]));
        if (receiver == null) {
            sendMessage(player, "§cCould not find a player named §6{0}§c.", args[0]);
            return true;
        }

        if (receiver.getOwner().getName().equals(player.getName())) {
            sendMessage(player, "§cYou cannot send money to yourself.");
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            sendMessage(player, "§cFailed to parse amount §6{0}§c.", args[1]);
            return true;
        }

        if (amount < 0) {
            sendMessage(player, "§cYou cannot send a negative amount of money.");
            return true;
        } else if (amount == 0) {
            sendMessage(player, "§cYou cannot send money with a value of zero.");
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
                    sendMessage(player, "§cCould not find a world named §6{0}§c.", args[2]);
                    return true;
                }
                sender.withdraw(world, amount);
                receiver.deposit(world, amount);
            } else {
                sender.withdraw(amount);
                receiver.deposit(amount);
            }

            sendMessage(player, "§aYou have sent §6{0}§a to §6{1}§a.",
                    plugin.format(amount), receiver.getOwner().getName());

            if (receiver.getOwner().isOnline()) {
                sendMessage(receiver.getOwner().getPlayer(), "§aYou have received §6{0}§a from §6{1}§a.",
                        plugin.format(amount), player.getName());
            }
        } catch (NotEnoughMoneyException ex) {
            sendMessage(player, "§aYou don't have enough money.");
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
        
        if (!isPermitted(sender)) {
            sendMessage(sender, "§cYou don't have permission to execute this command.");
            return true;
        }

        BankAccount receiver = plugin.getBankManager().getBankAccount(getOfflinePlayer(args[0]));
        if (receiver == null) {
            sendMessage(sender, "§cCould not find a player named §6{0}§c.", args[0]);
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            sendMessage(sender, "§cFailed to parse amount §6{0}§c.", args[1]);
            return true;
        }

        if (amount < 0) {
            sendMessage(sender, "§cYou cannot send a negative amount of money.");
            return true;
        } else if (amount == 0) {
            sendMessage(sender, "§cYou cannot send money with a value of zero.");
            return true;
        }

        if (isMultiWorld()) {
            World world = plugin.getServer().getWorld(args[2]);
            if (world == null) {
                sendMessage(sender, "§cCould not find a world named §6{0}§c.", args[2]);
                return true;
            }
            receiver.deposit(world, amount);
        } else {
            receiver.deposit(amount);
        }

        sendMessage(sender, "§aYou have sent §6{0}§a to §6{1}§a.",
                plugin.format(amount), receiver.getOwner().getName());

        if (receiver.getOwner().isOnline()) {
            sendMessage(receiver.getOwner().getPlayer(), "§aYou have received §6{0}§a.",
                    plugin.format(amount));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(Player player, String... args) {
        if (args.length == 1) {
            // Remove player's name from completions
            return this.onTabComplete((CommandSender) player, args).stream()
                .filter(name -> !name.equalsIgnoreCase(player.getName()))
                .collect(Collectors.toList());
        }
        return this.onTabComplete((CommandSender) player, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        switch (args.length) {
            case 1:
                return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.toList());
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