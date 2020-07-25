package de.epiceric.justmoney.storage.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.epiceric.justmoney.JustMoney;
import de.epiceric.justmoney.storage.SqlStorage;

/**
 * An SQL bank account storage using an SQLite database.
 * 
 * @since 1.0
 */
public class SqLiteStorage extends SqlStorage {
    private final File databaseFile;

    public SqLiteStorage(JustMoney plugin) {
        super(plugin);
        this.databaseFile = new File(plugin.getDataFolder(), "data.db");
    }

    @Override
    public String getTypeName() {
        return "SQLite";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
        } catch (ReflectiveOperationException ex) {
            throw new SQLException("Failed to initialize SQLite driver");
        }
    }

    @Override
    protected void initDatabase() throws SQLException {
        try {
            databaseFile.getParentFile().mkdirs();
            databaseFile.createNewFile();
        } catch (IOException ex) {
            throw new SQLException("Failed to create database file", ex);
        }
        super.initDatabase();
    }
}