package com.yumyumcoach.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseManager {
    private static final DatabaseManager INSTANCE = new DatabaseManager();

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    private String url;
    private String username;
    private String password;
    private boolean initialized;

    private DatabaseManager() {
    }

    public synchronized void initialize(String url, String username, String password) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        if (initialized) {
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MySQL JDBC driver not found", e);
        }
        this.url = url;
        this.username = username;
        this.password = password;
        this.initialized = true;
    }

    public Connection getConnection() {
        if (!initialized) {
            throw new IllegalStateException("DatabaseManager is not initialized");
        }
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to obtain database connection", e);
        }
    }

    public synchronized void shutdown() {
        initialized = false;
    }
}
