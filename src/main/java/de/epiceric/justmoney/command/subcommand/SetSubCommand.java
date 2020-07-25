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
 * The executor and tab completer for the "/money set ..." sub command.
 * 
 * @since 1.0
 */
public class SetSubCommand extends SubCommand {
    public SetSubCommand(JustMoney plugin) {
        super("set", plugin);
    }

    @Override
    public boolean isPermitted(CommandSender sender) {
        return sender.hasPermission("justmoney.set.self") || sender.hasPermission("justmoney.set.other");
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String... args) {
        if (!isPermitted(sender)) {
            sendMessage(sender, "§cYou don't have permission to use this command.");
            return true;
        }

        boolean isMultiWorld = plugin.getConfig().getBoolean("multi-world");
        if (args.length > 3 || (!isMultiWorld) && args.length > 2) {
            return false;
        }

        if ((args.length < 2 || (args.length < 3 && isMultiWorld)) && !(sender instanceof Player)) {
            return false;
        }

        boolean hasPlayer;
        double newBalance;
        if (args.length == 1) {
            // /money <amount>
            hasPlayer = false;
            try {
                newBalance = Double.parseDouble(args[0]);
            } catch (NumberFormatException ex) {
                return false;
            }
        } else {
            try {
                // /money set <player> <amount> [<world>]
                newBalance = Double.parseDouble(args[1]);
                hasPlayer = true;
            } catch (NumberFormatException ex) {
                try {
                    // /money set <amount> [<world>]
                    newBalance = Double.parseDouble(args[0]);
                    hasPlayer = false;
                } catch (NumberFormatException ex2) {
                    return false;
                }
            }
        }

        if (!hasPlayer) {
            if (!(sender instanceof Player)) {
                return false;
            }

            if (!sender.hasPermission("justmoney.set.self")) {
                sendMessage(sender, "§cYou don't have permission to use this command.");
                return true;
            }
        } else if (!sender.hasPermission("justmoney.set.other")) {
            sendMessage(sender, "§cYou don't have permission to use this command.");
            return true;
        }

        OfflinePlayer player = hasPlayer ? getOfflinePlayer(args[0]) : (Player) sender;
        if (player == null) {
            sendMessage(sender, "§cCould not find a player named §6{0}§c.", args[0]);
            return true;
        }

        BankAccount account = plugin.getBankManager().getBankAccount(player);

        if (newBalance < 0) {
            sendMessage(sender, "§cYou cannot set a negative balance.");
            return true;
        }

        if (isMultiWorld) {
            World world = args.length > 2
                ? plugin.getServer().getWorld(args[2])
                : ((Player) sender).getWorld();

            if (world == null) {
                sendMessage(sender, "§cCould not find a world named §6{0}§c.", args[2]);
                return true;
            }

            account.setBalance(world, newBalance);
        } else {
            account.setBalance(newBalance);
        }

        if (!hasPlayer || (sender instanceof Player && args[0].equals(((Player) sender).getName()))) {
            sendMessage(sender, "§aYour balance has been set to §6{0}§a.", plugin.format(newBalance));
        } else {
            sendMessage(sender, "§aThe balance of §6{0}§a has been set to §6{1}§a.", args[0], plugin.format(newBalance));
            if (player.isOnline()) {
                sendMessage(player.getPlayer(), "§aYour balance has been set to §6{0}§a.", plugin.format(newBalance));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (!sender.hasPermission("justmoney.set.self") && !sender.hasPermission("justmoney.set.other")) {
            return Collections.emptyList();
        }

        List<String> offlinePlayerNames = Arrays.stream(plugin.getServer().getOfflinePlayers())
            .map(OfflinePlayer::getName)
            .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
            .collect(Collectors.toList());

        // Suggest player
        if (sender.hasPermission("justmoney.set.other") && args.length == 1) {
            if (!offlinePlayerNames.isEmpty()) {
                return offlinePlayerNames;
            }
        }

        // Suggest amount
        boolean isAmountSet = false;
        try {
            Double.parseDouble(args[0]);
            isAmountSet = true;
        } catch (NumberFormatException ignored) {
        }

        if ((sender instanceof Player && sender.hasPermission("justmoney.set.self") && args.length == 1)
                || sender.hasPermission("justmoney.set.other") && args.length == 2) {
            String amount = args[args.length - 1];
            boolean isAmountValid = false;
            try {
                Double.parseDouble(amount);
                isAmountValid = true;
            } catch (NumberFormatException ignored) {
            }

            if ((amount.isEmpty() && !isAmountSet) || isAmountValid || amount.equals(".")) {
                int decimals = plugin.getConfig().getInt("formatting.decimal-places");
                int dotIndex = amount.indexOf(".");
                if (dotIndex == -1 || amount.length() - dotIndex <= decimals) {
                    return IntStream.rangeClosed(0, 9)
                        .filter(num -> !(amount.replaceAll("0\\.", "").isEmpty() && num == 0))
                        .mapToObj(num -> amount + String.valueOf(num))
                        .collect(Collectors.toList());
                } else {
                    return Arrays.asList(amount);
                }
            }
        }

        // Suggest world
        if (plugin.getConfig().getBoolean("multi-world") && (args.length == 2 || args.length == 3)) {
            if (args.length == 3 || isAmountSet) {
                return plugin.getServer().getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}