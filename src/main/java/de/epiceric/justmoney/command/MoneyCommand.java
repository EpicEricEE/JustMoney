package de.epiceric.justmoney.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.command.subcommand.HelpSubCommand;
import de.epiceric.justmoney.command.subcommand.SendSubCommand;
import de.epiceric.justmoney.command.subcommand.SetSubCommand;
import de.epiceric.justmoney.command.subcommand.SubCommand;
import de.epiceric.justmoney.model.BankAccount;
import de.epiceric.justmoney.util.Util;

/**
 * The executor and tab completer for the "/money" command.
 * 
 * @since 1.0
 */
public class MoneyCommand extends SubCommand {
    private final SubCommand helpCommand;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public MoneyCommand(JustMoney plugin) {
        super("", plugin);
        this.helpCommand = new HelpSubCommand(plugin);
        this.subCommands.add(new SendSubCommand(plugin));
        this.subCommands.add(new SetSubCommand(plugin));
        this.subCommands.add(helpCommand);
    }

    @Override
    public boolean isPermitted(CommandSender sender) {
        return true;
    }

    @Override
    public boolean onExecute(Player player, String label, String... args) {
        boolean hasPermissionViewOther = player.hasPermission("justmoney.view.other");

        switch (args.length) {
            case 0: {
                BankAccount account = plugin.getBankManager().getBankAccount(player);
                String message = getMessage(isMultiWorld()
                    ? "view-your-balance-current-world"
                    : "view-your-balance");

                sendMessage(player, message, account.formatBalance(player.getWorld()));
                return true;
            }
            case 1: {
                World world = plugin.getServer().getWorld(args[0]);
                OfflinePlayer accountOwner = getOfflinePlayer(args[0]);

                // try to parse argument as world
                if (world != null && isMultiWorld()) {
                    BankAccount account = plugin.getBankManager().getBankAccount(player);
                    sendMessage(player, getMessage("view-your-balance-in-world"),
                            world.getName(), account.formatBalance(world));
                    return true;
                }

                // try to parse argument as player
                if (accountOwner != null && hasPermissionViewOther) {
                    BankAccount account = plugin.getBankManager().getBankAccount(accountOwner);

                    String message = getMessage(isMultiWorld()
                        ? "view-player-balance-current-world"
                        : "view-player-balance");

                    sendMessage(player, message, accountOwner.getName(), account.formatBalance(player.getWorld()));
                    return true;
                }

                // the argument is neither a player nor a world
                if (isMultiWorld() || hasPermissionViewOther) {
                    String message = getMessage(hasPermissionViewOther
                        ? isMultiWorld()
                            ? "cannot-find-player-or-world"
                            : "cannot-find-player"
                        : "cannot-find-world");

                    sendMessage(player, message, args[0]);
                    return true;
                }

                break;
            }
            case 2: {
                if (isMultiWorld() && hasPermissionViewOther) {
                    OfflinePlayer accountOwner = getOfflinePlayer(args[0]);
                    World world = plugin.getServer().getWorld(args[1]);

                    if (accountOwner == null) {
                        sendMessage(player, getMessage("cannot-find-player"), args[0]);
                    } else if (world == null) {
                        sendMessage(player, getMessage("cannot-find-world"), args[1]);
                    } else {
                        BankAccount account = plugin.getBankManager().getBankAccount(accountOwner);
                        sendMessage(player, getMessage("view-player-balance-in-world"),
                                accountOwner.getName(), world.getName(), account.formatBalance(world));
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String... args) {
        boolean hasPermissionViewOther = sender.hasPermission("justmoney.view.other");

        switch (args.length) {
            case 0:
                return false;
            case 1: {
                if (isMultiWorld()) {
                    // a command sender does not have a default world to use
                    return false;
                }
                
                OfflinePlayer accountOwner = getOfflinePlayer(args[0]);

                // the argument has to be parsed as a player
                if (accountOwner != null && hasPermissionViewOther) {
                    BankAccount account = plugin.getBankManager().getBankAccount(accountOwner);
                    sendMessage(sender, getMessage("view-player-balance"),
                            accountOwner.getName(), account.formatBalance());
                    return true;
                }

                if (accountOwner == null) {
                    sendMessage(sender, getErrorMessage("cannot-find-player"), args[0]);
                    return true;
                }

                break;
            }
            case 2: {
                if (isMultiWorld() && hasPermissionViewOther) {
                    OfflinePlayer accountOwner = getOfflinePlayer(args[0]);
                    World world = plugin.getServer().getWorld(args[1]);

                    if (accountOwner == null) {
                        sendMessage(sender, getErrorMessage("cannot-find-player"), args[0]);
                    } else if (world == null) {
                        sendMessage(sender, getErrorMessage("cannot-find-world"), args[1]);
                    } else {
                        BankAccount account = plugin.getBankManager().getBankAccount(accountOwner);
                        sendMessage(sender, getMessage("view-player-balance-in-world"),
                                accountOwner.getName(), world.getName(), account.formatBalance(world));
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(Player player, String... args) {
        boolean hasPermissionViewOther = player.hasPermission("justmoney.view.other");

        Set<String> worlds = plugin.getServer().getWorlds().stream()
            .map(World::getName)
            .collect(Collectors.toSet());

        List<String> players = Util.completePlayer(plugin, null);

        switch (args.length) {
            case 1: {
                List<String> result = new ArrayList<>();
                if (isMultiWorld()) result.addAll(worlds);
                if (hasPermissionViewOther) result.addAll(players);
                return result;
            }
            case 2: {
                if (isMultiWorld() && !worlds.contains(args[0])) {
                    return new ArrayList<>(worlds);
                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        switch (args.length) {
            case 1: {
                if (sender.hasPermission("justmoney.view.other") && isMultiWorld()) {
                    return Util.completePlayer(plugin, null);
                }
            }
            case 2: {
                if (isMultiWorld()) {
                    return plugin.getServer().getWorlds().stream()
                        .map(World::getName)
                        .collect(Collectors.toList());
                }
            }
        }

        return Collections.emptyList();
    }
}