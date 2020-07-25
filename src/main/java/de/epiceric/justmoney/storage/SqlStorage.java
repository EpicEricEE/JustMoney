package de.epiceric.justmoney.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.bukkit.World;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.model.BankAccount;

/**
 * An abstract bank account storage using SQL.
 * 
 * @since 1.0
 */
public abstract class SqlStorage implements BankStorage {
    private static final String QUERY_STORE = "REPLACE INTO justmoney (uuid, world_name, balance) VALUES (?,?,?)";
    private static final String QUERY_LOAD = "SELECT uuid, world_name, balance FROM justmoney";

    protected final JustMoney plugin;

    public SqlStorage(JustMoney plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a connection to the SQL database.
     * 
     * @return the connection
     * @throws SQLException when a connection could not be established
     * @since 1.0
     */
    protected abstract Connection getConnection() throws SQLException;

    @Override
    public CompletableFuture<Void> storeAccount(BankAccount account) {
        return CompletableFuture.runAsync(() -> {
            for(World world : plugin.getServer().getWorlds()) {
                try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(QUERY_STORE)) {
                    stmt.setString(1, account.getOwner().getUniqueId().toString());
                    stmt.setString(2, world.getName());
                    stmt.setDouble(3, account.getBalance(world));
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    throw new CompletionException(ex);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Collection<BankAccount>> getAccounts() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                initDatabase();
            } catch (SQLException ex) {
                throw new CompletionException(ex);
            }

            Map<UUID, Map<String, Double>> values = new HashMap<>();

            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(QUERY_LOAD);
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    if (!values.containsKey(uuid)) {
                        values.put(uuid, new HashMap<>());
                    }
                    values.get(uuid).put(rs.getString("world_name"), rs.getDouble("balance"));
                }

                List<BankAccount> result = new ArrayList<>();
                values.forEach((uuid, balances) -> result.add(new BankAccount(plugin, plugin.getServer().getOfflinePlayer(uuid), balances)));
                return result;
            } catch (SQLException ex) {
                throw new CompletionException(ex);
            }
        });
    }
    
    /**
     * Initalizes the SQL database.
     * 
     * @throws SQLException when the database fails to initialize
     * @since 1.0
     */
    protected void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // ? What is the maximum length of a world name? Does 255 suffice?
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS justmoney ("
                + "uuid VARCHAR(36) NOT NULL,"
                + "world_name VARCHAR(255) NOT NULL,"
                + "balance DOUBLE NOT NULL,"
                + "PRIMARY KEY (uuid, world_name))");
        } catch (SQLException ex) {
            throw new SQLException("Failed to initialize database", ex);
        }
    }
}