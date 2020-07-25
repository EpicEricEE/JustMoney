package de.epiceric.justmoney.storage.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.storage.SqlStorage;

/**
 * An SQL bank account storage using a MySQL database.
 * 
 * @since 1.0
 */
public class MySqlStorage extends SqlStorage {
    public MySqlStorage(JustMoney plugin) {
        super(plugin);
    }

    @Override
    public String getTypeName() {
        return "MySQL";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            String hostname = plugin.getConfig().getString("mysql.hostname");
            int port = plugin.getConfig().getInt("mysql.port");
            String database = plugin.getConfig().getString("mysql.database");
            String username = plugin.getConfig().getString("mysql.username");
            String password = plugin.getConfig().getString("mysql.password");

            return DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s?useSSL=false",
                hostname, port, database), username, password);
        } catch (ReflectiveOperationException ex) {
            throw new SQLException("Failed to initialize MySQL driver");
        }
    }
}