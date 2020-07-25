package de.epiceric.justmoney.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                    .filter(num -> !(arg.replaceAll("0\\.", "").isEmpty() && num == 0))
                    .mapToObj(num -> arg + String.valueOf(num))
                    .collect(Collectors.toList());
            }
        } catch (NumberFormatException ignored) {
        }
        return Collections.emptyList();
    }
}