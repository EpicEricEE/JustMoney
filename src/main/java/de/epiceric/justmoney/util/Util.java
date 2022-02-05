package de.epiceric.justmoney.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import de.epiceric.justmoney.JustMoney;

/**
 * Class containing static utility methods for the JustMoney plugin.
 * 
 * @since 1.0
 */
public class Util {
    private Util() {
    }

    /**
     * Gets the tab completions for the given amount argument.
     * 
     * @param decimals the configured amount of decimal places
     * @param arg the argument to complete
     * @return the tab completions
     * @since 1.0
     */
    public static List<String> completeAmount(int decimals, String arg) {
        try {
            if (!arg.isEmpty()) {
                // Empty string is valid and should not throw exception
                Double.parseDouble(arg);
            }

            int dotIndex = arg.indexOf(".");

            // Allow a maximum of {decimals} decimal places
            if (dotIndex == -1 || arg.length() - dotIndex <= decimals) {                
                return IntStream.rangeClosed(0, 9)
                    .filter(num -> !(arg.replace("0.", "").isEmpty() && num == 0))
                    .mapToObj(num -> arg + String.valueOf(num))
                    .collect(Collectors.toList());
            }
        } catch (NumberFormatException ignored) {
        }
        return Collections.emptyList();
    }

    /**
     * Gets the tab completions for a player name.
     * 
     * @param plugin an instance of the plugin
     * @param executor the player to be omitted from the completions
     * @return the tab completions
     * @since 1.2
     */
    public static List<String> completePlayer(JustMoney plugin, Player executor) {
        if (plugin.getConfig().getBoolean("offline-tab-completion")) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(name -> executor == null || !name.equalsIgnoreCase(executor.getName()))
                .collect(Collectors.toList());
        } else {
            return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> executor == null || !name.equalsIgnoreCase(executor.getName()))
                .collect(Collectors.toList());
        }
    }
}