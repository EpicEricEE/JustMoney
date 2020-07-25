package de.epiceric.justmoney.command.subcommand;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import de.epiceric.justmoney.JustMoney;

/**
 * A sub command for the "/money" command.
 * 
 * @since 1.0
 */
public abstract class SubCommand {
    protected final JustMoney plugin;
    private final String name;

    public SubCommand(String name, JustMoney plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    /**
     * Sends a formatted message to the given receiver.
     * <p>
     * The message is formatted via placeholders as <code>{0}</code> and <code>{1}</code>.
     * 
     * @param receiver the receiver of the message
     * @param message the message
     * @param args the arguments to fill the placeholders
     * @since 1.0
     */
    protected final void sendMessage(CommandSender receiver, String message, Object... args) {
        receiver.sendMessage(MessageFormat.format(message, args));
    }

    /**
     * Gets the offline player with the given name.
     * 
     * @param name the name
     * @return the offline player or {@code null} if none exists
     * @since 1.0
     */
    protected final OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(plugin.getServer().getOfflinePlayers())
            .filter(player -> player.getName().equalsIgnoreCase(name))
            .findAny().orElse(null);
    }

    /**
     * Gets the name of the sub command.
     * 
     * @return the name
     * @since 1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Gets whether the given command sender is allowed to use the sub command.
     * <p>
     * This is used for checking whether the command's tab completions 
     * should contain this subcommand.
     * 
     * @param sender
     * @return whether the player is permitted
     * @since 1.0
     */
    public abstract boolean isPermitted(CommandSender sender);

    /**
     * Called when the sub command is executed.
     * <p>
     * The first argument in {@code args} represents the argument after the sub command!
     * 
     * @param sender the command sender
     * @param label the used command
     * @param args the command arguments
     * @return whether the command was executed successfully
     * @since 1.0
     */
    public abstract boolean onExecute(CommandSender sender, String label, String... args);

    /**
     * Called when tab completions are requested.
     * <p>
     * The first argument in {@code args} represents the argument after the sub command!
     * <p>
     * The completions are automatically filtered by comparing the first characters.
     * 
     * @param sender the command sender
     * @param args the command arguments
     * @return a list of all possible completions
     * @since 1.0
     */
    public abstract List<String> onTabComplete(CommandSender sender, String... args);
}