package de.epiceric.justmoney.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

import org.bukkit.World;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.model.BankAccount;

/**
 * A bank account storage using flat files.
 * 
 * @since 1.0
 */
public class FileStorage implements BankStorage {
    private final JustMoney plugin;
    private final File dataFolder;

    public FileStorage(JustMoney plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
    }

    @Override
    public String getTypeName() {
        return "Flat File";
    }

    @Override
    public CompletableFuture<Void> storeAccount(BankAccount account) {
        return CompletableFuture.runAsync(() -> {
            File file = new File(dataFolder, account.getOwner().getUniqueId().toString());
            file.getParentFile().mkdirs();

            try {
                file.createNewFile();  
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }

            try (FileWriter writer = new FileWriter(file)) {
                for (World world : plugin.getServer().getWorlds()) {
                    writer.write(world.getName() + ":" + account.getBalance(world) + "\n");
                }
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<Collection<BankAccount>> getAccounts() {
        return CompletableFuture.supplyAsync(() -> {
            if (!dataFolder.exists()) {
                // No bank accounts stored
                return Collections.emptyList();
            }

            List<BankAccount> result = new ArrayList<>();

            File[] files = dataFolder.listFiles();
            for (File file : files) {
                loadAccount(file).ifPresent(result::add);
            }

            return result;
        });
    }

    /**
     * Loads the bank account of the given file.
     * 
     * @param file the file
     * @return the bank account or an empty optional if it fails to load
     */
    private Optional<BankAccount> loadAccount(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() == 0) {
                return Optional.empty();
            }

            if (file.getName().length() != 36) {
                // File name length does not match UUID length
                return Optional.empty();
            }

            UUID uuid = UUID.fromString(file.getName());
            Map<String, Double> balances = new HashMap<>();

            for (String line : lines) {
                // World name might have a colon, so split on last occurrence
                int lastColonIndex = line.lastIndexOf(":");

                if (lastColonIndex == -1) {
                    // No colon in line
                    continue;
                }

                if (lastColonIndex == line.length() - 1) {
                    // Line ends with colon
                    continue;
                }

                String worldName = line.substring(0, lastColonIndex);
                String balanceStr = line.substring(lastColonIndex + 1);
                double balance = 0;

                try {
                    balance = Double.parseDouble(balanceStr);
                } catch (NumberFormatException ex) {
                    plugin.getLogger().severe("Failed to parse balance stored for player " + uuid.toString() + " in world " + worldName + ": " + balanceStr);
                }
                
                if (balance < 0) {
                    plugin.getLogger().warning("Negative balance stored for player " + uuid.toString() + " in world " + worldName);
                    balance = 0;
                }

                balances.put(worldName, balance);
            }

            if (balances.isEmpty()) {
                return Optional.empty();
            }
            
            BankAccount account = new BankAccount(plugin, plugin.getServer().getOfflinePlayer(uuid), balances);
            return Optional.of(account);
        } catch (IOException | IllegalArgumentException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load balance for player " + file.getName(), ex);
        }

        return Optional.empty();
    }
}