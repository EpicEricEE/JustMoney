package de.epiceric.justmoney.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.command.subcommand.GiveSubCommand;
import de.epiceric.justmoney.command.subcommand.HelpSubCommand;
import de.epiceric.justmoney.command.subcommand.SendSubCommand;
import de.epiceric.justmoney.command.subcommand.SetSubCommand;
import de.epiceric.justmoney.command.subcommand.SubCommand;
import de.epiceric.justmoney.command.subcommand.TakeSubCommand;

/**
 * The main executor and tab completer for the "/money" command.
 * 
 * @since 1.0
 */
public class CommandManager implements TabExecutor {
    private final MoneyCommand command;
    private final SubCommand helpCommand;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public CommandManager(JustMoney plugin) {
        this.command = new MoneyCommand(plugin);
        this.helpCommand = new HelpSubCommand(plugin);
        this.subCommands.add(new SendSubCommand(plugin));
        this.subCommands.add(new SetSubCommand(plugin));
        this.subCommands.add(new GiveSubCommand(plugin));
        this.subCommands.add(new TakeSubCommand(plugin));
        this.subCommands.add(helpCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Execute sub command if available
        if (args.length > 0) {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().isEmpty()) {
                    continue;
                }
                
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (!subCommand.onExecute(player, label, Arrays.copyOfRange(args, 1, args.length))) {
                            helpCommand.onExecute(player, label, new String[0]);
                        }
                    } else {
                        if (!subCommand.onExecute(sender, label, Arrays.copyOfRange(args, 1, args.length))) {
                            helpCommand.onExecute(sender, label, new String[0]);
                        }
                    }
                    return true;
                }
            }
        }

        // Execute main command if no sub command was run
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!this.command.onExecute(player, label, args)) {
                helpCommand.onExecute(player, label, new String[0]);
            }
        } else {
            if (!this.command.onExecute(sender, label, args)) {
                helpCommand.onExecute(sender, label, new String[0]);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        // Suggest sub commands
        if (args.length == 1) {
            result.addAll(subCommands.stream()
                .filter(cmd -> cmd.isPermitted(sender))
                .map(SubCommand::getName)
                .collect(Collectors.toList()));
        }

        // Suggest sub command suggestions
        boolean isHandledBySubCommand = false;
        if (args.length > 1) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            for (SubCommand subCommand : subCommands) {
                if (subCommand.isPermitted(sender) && args[0].equalsIgnoreCase(subCommand.getName())) {
                    if (sender instanceof Player) {
                        result.addAll(subCommand.onTabComplete((Player) sender, subArgs));
                    } else {
                        result.addAll(subCommand.onTabComplete(sender, subArgs));
                    }
                    isHandledBySubCommand = true;
                    break;
                }
            }
        }

        // Suggest main command suggestions if not handled otherwise
        if (!isHandledBySubCommand) {
            if (sender instanceof Player) {
                result.addAll(this.command.onTabComplete((Player) sender, args));
            } else {
                result.addAll(this.command.onTabComplete(sender, args));
            }
        }

        return result.stream()
            .filter(str -> str.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}