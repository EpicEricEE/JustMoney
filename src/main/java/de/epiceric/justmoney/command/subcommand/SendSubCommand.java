package de.epiceric.justmoney.command.subcommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.model.BankAccount;

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
    public boolean onExecute(CommandSender sender, String label, String... args) {
        if (!sender.hasPermission("justmoney.send")) {
            sendMessage(sender, "§cYou don't have permission to use this command.");
            return true;
        }

        boolean isMultiWorld = plugin.getConfig().getBoolean("multi-world");
        if (args.length < 2 || args.length > 3 || (!isMultiWorld && args.length > 2)) {
            return false;
        }

        if (!(sender instanceof Player) && isMultiWorld && args.length < 3) {
            return false;
        }
        
        World world = null;
        if (isMultiWorld) {
            world = args.length == 3
                ? plugin.getServer().getWorld(args[2])
                : ((Player) sender).getWorld();

            if (world == null) {
                sendMessage(sender, "§cCould not find a world named §6{0}§c.", args[2]);
                return true;
            }
        }

        OfflinePlayer receiver = getOfflinePlayer(args[0]);
        if (receiver == null) {
            sendMessage(sender, "§cCould not find a player named §6{0}§c.", args[0]);
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (receiver.getName().equalsIgnoreCase(player.getName())) {
                sendMessage(sender, "§cYou cannot send money to yourself.");
                return true;
            }
        }
        
        double moneyAmount;
        try {
            moneyAmount = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            sendMessage(sender, "§cCould not parse the amount of money to send: §6{0}", args[1]);
            return true;
        }

        if (moneyAmount < 0) {
            sendMessage(sender, "§cYou cannot send a negative amount of money.");
            return true;
        } else if (moneyAmount == 0) {
            sendMessage(sender, "§cThe amount to be sent must be over {0}.", plugin.format(0));
            return true;
        }

        BankAccount from = sender instanceof Player
            ? plugin.getBankManager().getBankAccount((Player) sender) : null;
        BankAccount to = plugin.getBankManager().getBankAccount(receiver);

        if (isMultiWorld) {
            try {
                if (from != null) {
                    from.withdraw(world, moneyAmount);
                }
                to.deposit(world, moneyAmount);
            } catch (IllegalStateException ex) {
                sendMessage(sender, "§cYou don't have enough money.");
                return true;
            }
        } else {
            try {
                if (from != null) {
                    from.withdraw(moneyAmount);
                }
                to.deposit(moneyAmount);
            } catch (IllegalStateException ex) {
                sendMessage(sender, "§cYou don't have enough money.");
                return true;
            }
        }

        sendMessage(sender, "§aYou have sent §6{0}§a to §6{1}§a.",
                plugin.format(moneyAmount), receiver.getName());

        if (receiver.isOnline()) {
            if (sender instanceof Player) {
                sendMessage(receiver.getPlayer(), "§aYou have received §6{0}§a from §6{1}§a.",
                    plugin.format(moneyAmount), ((Player) sender).getName());
            } else {
                sendMessage(receiver.getPlayer(), "§aYou have received §6{0}§a.",
                    plugin.format(moneyAmount));
            }
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (!sender.hasPermission("justmoney.send")) {
            return Collections.emptyList();
        }

        // Suggest player
        if (args.length == 1) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(name -> !(sender instanceof Player && name.equalsIgnoreCase(((Player) sender).getName())))
                .collect(Collectors.toList());
        }

        // Suggest amount
        if (args.length == 2) {
            boolean isAmountValid = args[1].isEmpty();
            try {
                Double.parseDouble(args[1]);
                isAmountValid = true;
            } catch (NumberFormatException ignored) {
            }

            if (isAmountValid || args[1].equals(".")) {
                int decimals = plugin.getConfig().getInt("formatting.decimal-places");
                int dotIndex = args[1].indexOf(".");
                if (dotIndex == -1 || args[1].length() - dotIndex <= decimals) {
                    return IntStream.rangeClosed(0, 9)
                        .filter(num -> !(args[1].replaceAll("0\\.", "").isEmpty() && num == 0))
                        .mapToObj(num -> args[1] + String.valueOf(num))
                        .collect(Collectors.toList());
                } else {
                    return Arrays.asList(args[1]);
                }
            }
        }

        // Suggest world
        if (plugin.getConfig().getBoolean("multi-world") && args.length == 3) {
            return plugin.getServer().getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}