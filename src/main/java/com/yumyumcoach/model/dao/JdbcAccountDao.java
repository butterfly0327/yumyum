package com.yumyumcoach.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.Account;

public class JdbcAccountDao implements AccountDao {
    private static final JdbcAccountDao INSTANCE = new JdbcAccountDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcAccountDao getInstance() {
        return INSTANCE;
    }

    private JdbcAccountDao() {
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        String sql = "SELECT username, password, email FROM accounts WHERE username = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAccount(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find account by username", e);
        }
    }

    @Override
    public void save(Account account) {
        String sql = "INSERT INTO accounts (username, password, email) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE password = VALUES(password), email = VALUES(email)";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ps.setString(3, account.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save account", e);
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT username, password, email FROM accounts ORDER BY username";
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load accounts", e);
        }
    }

    @Override
    public void delete(String username) {
        String sql = "DELETE FROM accounts WHERE username = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete account", e);
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setUsername(rs.getString("username"));
        account.setPassword(rs.getString("password"));
        account.setEmail(rs.getString("email"));
        return account;
    }
}
