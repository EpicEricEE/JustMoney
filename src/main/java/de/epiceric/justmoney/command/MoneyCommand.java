package de.epiceric.justmoney.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.command.subcommand.HelpSubCommand;
import de.epiceric.justmoney.command.subcommand.SendSubCommand;
import de.epiceric.justmoney.command.subcommand.SetSubCommand;
import de.epiceric.justmoney.command.subcommand.SubCommand;
import de.epiceric.justmoney.model.BankAccount;

/**
 * The executor and tab completer for the "/money" command.
 * 
 * @since 1.0
 */
public class MoneyCommand extends SubCommand implements TabExecutor {
    private final SubCommand helpCommand;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public MoneyCommand(JustMoney plugin) {
        super("", plugin);
        this.helpCommand = new HelpSubCommand(plugin);
        this.subCommands.add(new SendSubCommand(plugin));
        this.subCommands.add(new SetSubCommand(plugin));
        this.subCommands.add(helpCommand);
    }

    // Executor and tab completion from SubCommand class

    @Override
    public boolean onExecute(CommandSender sender, String label, String... args) {
        OfflinePlayer player = null;
        World world = null;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                return false;
            }

            player = (Player) sender;
            world = ((Player) sender).getWorld();
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                if (plugin.getConfig().getBoolean("multi-world")) {
                    return false;
                }

                if (!sender.hasPermission("justmoney.view.other")) {
                    return false;
                }

                player = getOfflinePlayer(args[0]);

                if (player == null) {
                    sendMessage(sender, "§cCould not find a player with name §6{0}§c.", args[0]);
                    return true;
                }
            } else {
                world = plugin.getServer().getWorld(args[0]);

                if (world == null) {
                    if (sender.hasPermission("justmoney.view.other")) {
                        player = getOfflinePlayer(args[0]);
                        world = ((Player) sender).getWorld();
                    } else {
                        sendMessage(sender, "§cCould not find a world with name §6{0}§c.", args[0]);    
                        return true;
                    }
                }

                if (player == null) {
                    sendMessage(sender, "§cCould not find a player or world with name §6{0}§c.", args[0]);
                    return true;
                }
            }
        } else if (args.length == 2) {
            if (!sender.hasPermission("justmoney.view.other")) {
                return false;
            }

            player = getOfflinePlayer(args[0]);
            world = plugin.getServer().getWorld(args[1]);

            if (player == null) {
                sendMessage(sender, "§cCould not find a player with name §6{0}§c.", args[0]);
                return true;
            }
            
            if (world == null) {
                sendMessage(sender, "§cCould not find a world with name §6{0}§c.", args[1]);
                return true;
            }
        } else {
            return false;
        }

        BankAccount account = plugin.getBankManager().getBankAccount(player);
        boolean customPlayer = !player.getUniqueId().equals(((Player) sender).getUniqueId());

        if (plugin.getConfig().getBoolean("multi-world")) {
            boolean customWorld = world != null
                    && !((Player) sender).getWorld().getName().equalsIgnoreCase(world.getName());

            if (customWorld) {
                if (customPlayer) {
                    sendMessage(sender, "§aYour balance in world §6{0}§a is §6{1}§a.",
                            world.getName(), account.formatBalance(world));
                } else {
                    sendMessage(sender, "§aThe current balance of §6{0}§a in the world §6{1}§a is §6{2}§a.",
                            player.getName(), world.getName(), account.formatBalance(world));
                }
            } else {
                if (customPlayer) {
                    sendMessage(sender, "§aThe current balance of §6{0}§a in the current world is §6{0}§a.",
                            player.getName(), account.formatBalance(world));
                } else {
                    sendMessage(sender, "§aYour balance in the current world is §6{0}§a.",
                            account.formatBalance(world));
                }
            }
        } else {
            if (customPlayer) {
                sendMessage(sender, "§aThe current balance of §6{0}§a is §6{1}§a.", player.getName(), account.formatBalance());
            } else {
                sendMessage(sender, "§aYour current balance is §6{0}§a.", account.formatBalance());
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        boolean isMultiWorld = plugin.getConfig().getBoolean("multi-world");
        boolean allowViewOther = sender.hasPermission("justmoney.view.other");

        // Suggest player
        if (allowViewOther && args.length == 1) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toList());
        }

        // Suggest world
        if (isMultiWorld && (args.length == 1 || (allowViewOther && args.length == 2))) {
            return plugin.getServer().getWorlds().stream()
                .map(World::getName)
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    // Executor and tab completer for Bukkit

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().isEmpty()) {
                    continue;
                }
                
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    if (!subCommand.onExecute(sender, label, Arrays.copyOfRange(args, 1, args.length))) {
                        helpCommand.onExecute(sender, label, new String[0]);
                    }
                    return true;
                }
            }
        }

        if (!this.onExecute(sender, label, args)) {
            helpCommand.onExecute(sender, label, new String[0]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        // Suggest sub commands
        if (args.length == 1) {
            result.addAll(subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));
        }

        // Suggest sub command suggestions
        if (args.length > 1) {
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    result.addAll(subCommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length)));
                }
                break;
            }
        }

        return result.stream()
            .filter(str -> str.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}