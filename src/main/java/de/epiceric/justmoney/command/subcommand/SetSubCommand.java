package de.epiceric.justmoney.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.model.BankAccount;
import de.epiceric.justmoney.util.Util;

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
                double newBalance = Double.parseDouble(args[0]);

                if (newBalance < 0) {
                    sendMessage(player, getErrorMessage("cannot-set-negative"));
                    return true;
                }

                account.setBalance(player.getWorld(), newBalance);
                if (isMultiWorld()) {
                    sendMessage(player, getMessage("set-your-balance-current-world"), plugin.format(newBalance));
                } else {
                    sendMessage(player, getMessage("set-your-balance"), plugin.format(newBalance));
                }
            } catch (NumberFormatException ex) {
                sendMessage(player, getErrorMessage("cannot-parse-balance"), args[1]);
            }
            return true;
        }

        int newBalanceIndex;
        double newBalance;
        try {
            newBalance = Double.parseDouble(args[1]);
            newBalanceIndex = 1;
        } catch (NumberFormatException ex) {
            if (args.length == 3) {
                sendMessage(player, getErrorMessage("cannot-parse-balance"), args[1]);
                return true;
            }

            try {
                newBalance = Double.parseDouble(args[0]);
                newBalanceIndex = 0;
            } catch (NumberFormatException ex2) {
                sendMessage(player, getErrorMessage("cannot-parse-balance"), args[0] + " / " + args[1]);
                return true;
            }    
        }

        if (newBalanceIndex == 0 && !player.hasPermission("justmoney.set.self")) {
            return false;
        }

        if (newBalanceIndex == 1 && !player.hasPermission("justmoney.set.other")) {
            return false;
        }

        BankAccount account = plugin.getBankManager().getBankAccount(
                newBalanceIndex == 0 ? player: getOfflinePlayer(args[0]));

        if (account == null) {
            sendMessage(player, getErrorMessage("cannot-find-player"), args[0]);
            return true;
        }

        if (newBalance < 0) {
            sendMessage(player, getErrorMessage("cannot-set-negative"));
            return true;
        }

        Optional<Player> accountOwner = Optional.ofNullable(account.getOwner().getPlayer());
        final double _newBalance = newBalance; // For use in enclosing scope

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

            account.setBalance(world, newBalance);

            if (account.getOwner().getName().equals(player.getName())) {
                sendMessage(player, getMessage("set-your-balance-in-world"),
                        world.getName(), plugin.format(newBalance));
            } else {
                sendMessage(player, getMessage("set-player-balance-in-world"),
                        account.getOwner().getName(), world.getName(), plugin.format(newBalance));

                accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("set-your-balance-in-world"),
                        world.getName(), plugin.format(_newBalance)));
            }
            
        } else {
            account.setBalance(newBalance);

            if (account.getOwner().getName().equals(player.getName())) {
                sendMessage(player, getMessage("set-your-balance"), plugin.format(newBalance));
            } else {
                sendMessage(player, getMessage("set-player-balance"),
                        account.getOwner().getName(), plugin.format(newBalance));

                accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("set-your-balance"),
                        plugin.format(_newBalance)));
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

        double newBalance;
        try {
            newBalance = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            sendMessage(sender, getErrorMessage("cannot-parse-balance"), args[1]);
            return true;
        }

        if (newBalance < 0) {
            sendMessage(sender, getErrorMessage("cannot-set-negative"));
            return true;
        }

        Optional<Player> accountOwner = Optional.ofNullable(account.getOwner().getPlayer());

        if (isMultiWorld()) {
            World world = plugin.getServer().getWorld(args[2]);
            if (world == null) {
                sendMessage(sender, getErrorMessage("cannot-find-world"), args[2]);
                return true;
            }

            account.setBalance(world, newBalance);

            sendMessage(sender, getMessage("set-player-balance-in-world"),
                    account.getOwner().getName(), world.getName(), plugin.format(newBalance));

            accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("set-your-balance-in-world"),
                    world.getName(), plugin.format(newBalance)));
        } else {
            account.setBalance(newBalance);

            sendMessage(sender, getMessage("set-player-balance"),
                    account.getOwner().getName(), plugin.format(newBalance));

            accountOwner.ifPresent(owner -> sendMessage(owner, getMessage("set-your-balance"),
                    plugin.format(newBalance)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(Player player, String... args) {
        List<String> worlds = plugin.getServer().getWorlds().stream()
            .map(World::getName)
            .collect(Collectors.toList());

        boolean hasPermissionSetSelf = player.hasPermission("justmoney.set.self");
        boolean hasPermissionSetOther = player.hasPermission("justmoney.set.other");
        int decimals = plugin.getConfig().getInt("formatting.decimal-places");

        switch (args.length) {
            case 1: {
                List<String> result = new ArrayList<>();
                if (hasPermissionSetSelf) {
                    result.addAll(Arrays.stream(plugin.getServer().getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList()));
                }
                if (hasPermissionSetOther && result.isEmpty()) {
                    // Only add amount completions if nothing else has been added
                    result.addAll(Util.completeAmount(decimals, args[0]));
                }
                return result;
            }
            case 2: {
                if (hasPermissionSetSelf && !hasPermissionSetOther) {
                    return isMultiWorld() ? worlds : Collections.emptyList();
                }

                if (!hasPermissionSetSelf && hasPermissionSetOther) {
                    return Util.completeAmount(decimals, args[1]);
                }

                if (!hasPermissionSetOther && isMultiWorld()) {
                    return worlds;
                }

                if (hasPermissionSetOther) {
                    try {
                        Double.parseDouble(args[0]);
    
                        if (getOfflinePlayer(args[0]) != null) {
                            // The player name also is a valid amount
                            if (!isMultiWorld() || worlds.stream().noneMatch(world -> world.toLowerCase().startsWith(args[1].toLowerCase()))) {
                                // If no world is being entered, suggest entering an amount
                                return Util.completeAmount(decimals, args[1]);
                            }
                            return worlds;
                        }

                        if (isMultiWorld()) {
                            return worlds;
                        }
                    } catch (NumberFormatException ex) {
                        return Util.completeAmount(decimals, args[1]);
                    }
                }                
            }
            case 3: {
                // Only suggest worlds if not already entered before
                if (hasPermissionSetOther && isMultiWorld() && !worlds.contains(args[1])) {
                    return worlds;
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (!sender.hasPermission("justmoney.set.other")) {
            return Collections.emptyList();
        }

        switch (args.length) {
            case 1:
                return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.toList());
            case 2:
                return Util.completeAmount(plugin.getConfig().getInt("formatting.decimal-places"), args[1]);
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