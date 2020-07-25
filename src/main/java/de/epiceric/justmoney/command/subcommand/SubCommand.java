package de.epiceric.justmoney.command.subcommand;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
     * Gets whether the multi world config options is enabled.
     * 
     * @return whether "multi-world" is enabled in the config
     * @since 1.0
     */
    protected final boolean isMultiWorld() {
        return plugin.getConfig().getBoolean("multi-world");
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
     * @param sender the command sender
     * @return whether the player is permitted
     * @since 1.0
     */
    public abstract boolean isPermitted(CommandSender sender);

    /**
     * Called when the sub command is executed by a player.
     * <p>
     * The first argument in {@code args} represents the argument after the sub command!
     * 
     * @param player the player who ran the command
     * @param label the used command
     * @param args the command arguments
     * @return whether the command was executed successfully
     * @since 1.0
     */
    public boolean onExecute(Player player, String label, String... args) {
        return false;
    }

    /**
     * Called when the sub command is executed by a non-player command sender.
     * <p>
     * The first argument in {@code args} represents the argument after the sub command!
     * 
     * @param sender the command sender
     * @param label the used command
     * @param args the command arguments
     * @return whether the command was executed successfully
     * @since 1.0
     */
    public boolean onExecute(CommandSender sender, String label, String... args) {
        return false;
    }

    /**
     * Called when tab completions are requested by a player.
     * <p>
     * The first argument in {@code args} represents the argument after the sub command!
     * <p>
     * The completions are automatically filtered by comparing the first characters.
     * 
     * @param player the player command sender
     * @param args the command arguments
     * @return a list of all possible completions
     * @since 1.0
     */
    public List<String> onTabComplete(Player player, String... args) {
        return Collections.emptyList();
    }

    /**
     * Called when tab completions are requested by a non-player command sender.
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
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return Collections.emptyList();
    }
}