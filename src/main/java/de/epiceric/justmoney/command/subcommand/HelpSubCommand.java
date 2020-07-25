package de.epiceric.justmoney.command.subcommand;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;

/**
 * The executor for the "/money help" sub command.
 * 
 * @since 1.0
 */
public class HelpSubCommand extends SubCommand {
    public HelpSubCommand(JustMoney plugin) {
        super("help", plugin);
    }

    /**
     * Constructs a line for the help message.
     * <p>
     * Format: <code>§6/{label} {args} [&#60;world&#62;]: §f{description}</code>
     * 
     * @param label the used command label
     * @param args the command's arguments
     * @param appendWorld whether {@code [<world>]} is appended to the command
     * @param description the help message
     * @return the line
     * @since 1.0
     */
    private String createLine(String label, String args, boolean appendWorld, String description) {
        String command = label;
        if (!args.isEmpty()) {
            command += " " + args;
        }
        if (appendWorld) {
            command += " [<world>]";
        }
        return MessageFormat.format("§6/{0}: §f{1}", command, description);
    }

    @Override
    public boolean isPermitted(CommandSender sender) {
        return true;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String... args) {
        boolean isPlayer = sender instanceof Player;
        boolean isMultiWorld = plugin.getConfig().getBoolean("multi-world");
        
        sender.sendMessage("§e--------- §fHelp: JustMoney §e-----------------------");

        if (isPlayer) {
            sender.sendMessage(isMultiWorld
                ? "§7Omit the <world> parameter to use your current world."
                : "§7Your balance is shared across all worlds.");
        }

        // Show "/money [<world>]" command
        if (isPlayer) {
           sender.sendMessage(createLine(label, "", isMultiWorld, "Show your current balance."));
        }

        // Show "/money <player> [<world>]" command
        if (sender.hasPermission("justmoney.view.other")) {
            if (isPlayer || !isMultiWorld) {
                sender.sendMessage(createLine(label, "<player>", isMultiWorld, "Show the balance of a player."));
            } else if (isMultiWorld) {
                sender.sendMessage(createLine(label, "<player> <world>", false, "Show the balance of a player."));
            }
        }

        // Show "/money send <player> <amount> [<world>]" command
        if (sender.hasPermission("justmoney.send")) {
            if (isPlayer || !isMultiWorld) {
                sender.sendMessage(createLine(label, "send <player> <amount>", isMultiWorld, "Send money to a player."));
            } else if (isMultiWorld) {
                sender.sendMessage(createLine(label, "send <player> <amount> <world>", false, "Send money to a player."));
            }
        }

        // Show "/money set <amount> [<world>]" command
        if (isPlayer) {
            if (sender.hasPermission("justmoney.set.self")) {
                sender.sendMessage(createLine(label, "set <amount>", isMultiWorld, "Set your balance."));
            }
        }

        // Show "/money set <player> <amount> [<world>]" command
        if (sender.hasPermission("justmoney.set.other")) {
            if (isPlayer || !isMultiWorld) {
                sender.sendMessage(createLine(label, "set <player> <amount>", isMultiWorld, "Set the balance of a player."));
            } else if (isMultiWorld) {
                sender.sendMessage(createLine(label, "set <player> <amount> <world>", false, "Set the balance of a player."));
            }
        }

        // Show "/money help" command
        sender.sendMessage(createLine(label, "help", false, "Show this help message."));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return Collections.emptyList();
    }
}