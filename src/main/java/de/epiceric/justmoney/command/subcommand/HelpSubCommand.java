package de.epiceric.justmoney.command.subcommand;

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
     * Sends a line for the help message to the given receiver.
     * <p>
     * Format (player): <code>§6/{label} {args} [&#60;world&#62;]: §f{description}</code>,<br>
     * Format (non-player): <code>§6/{label} {args} &#60;world&#62;: §f{description}</code>
     * 
     * @param sender the receiver of the message
     * @param label the used command label
     * @param args the command's arguments
     * @param description the command usage
     * @return the line
     * @since 1.0
     */
    private void sendCommand(CommandSender receiver, String label, String args, String description) {
        String command = label;
        if (!args.isEmpty()) {
            command += " " + args;
        }
        command += receiver instanceof Player ? " [<world>]" : " <world>";
        sendMessage(receiver, "§6/{0}: §f{1}", command, description);
    }

    @Override
    public boolean isPermitted(CommandSender sender) {
        return true;
    }

    @Override
    public boolean onExecute(Player player, String label, String... args) {
        sendMessage(player, "§e--------- §fHelp: JustMoney §e-----------------------");
        sendMessage(player, isMultiWorld()
                ? "§7Leave out the <world> parameter to use your current world."
                : "§7Your balance is shared across all worlds.");

        sendCommand(player, label, "", "Show your balance.");
        if (player.hasPermission("justmoney.view.other")) {
            sendCommand(player, label, "<player>", "Show a player's balance.");
        }
        if (player.hasPermission("justmoney.send")) {
            sendCommand(player, label, "send <player> <amount>", "Send money to a player.");
        }
        if (player.hasPermission("justmoney.set.self")) {
            sendCommand(player, label, "set <amount>", "Set your balance.");
        }
        if (player.hasPermission("justmoney.set.other")) {
            sendCommand(player, label, "set <player> <amount>", "Set a player's balance.");
        }
        sendMessage(player, "§6/{0} help: §f{1}", label, "Show this help message");

        return true;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String... args) {
        sendMessage(sender, "§e--------- §fHelp: JustMoney §e-----------------------");
        if (sender.hasPermission("justmoney.view.other")) {
            sendCommand(sender, label, "<player>", "Show a player's balance.");
        }
        if (sender.hasPermission("justmoney.send")) {
            sendCommand(sender, label, "send <player> <amount>", "Send money to a player.");
        }
        if (sender.hasPermission("justmoney.set.other")) {
            sendCommand(sender, label, "set <player> <amount>", "Set a player's balance.");
        }
        sendMessage(sender, "§6/{0} help: §f{1}", label, "Show this help message");

        return true;
    }
}