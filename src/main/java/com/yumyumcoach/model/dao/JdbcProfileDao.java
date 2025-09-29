package com.yumyumcoach.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.Profile;

public class JdbcProfileDao implements ProfileDao {
    private static final JdbcProfileDao INSTANCE = new JdbcProfileDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcProfileDao getInstance() {
        return INSTANCE;
    }

    private JdbcProfileDao() {
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        String sql = "SELECT username, name, height, weight, disease FROM profiles WHERE username = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProfile(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load profile", e);
        }
    }

    @Override
    public void save(Profile profile) {
        String sql = "INSERT INTO profiles (username, name, height, weight, disease) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE name = VALUES(name), height = VALUES(height), weight = VALUES(weight), disease = VALUES(disease)";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.getUsername());
            ps.setString(2, profile.getName());
            ps.setDouble(3, profile.getHeight());
            ps.setDouble(4, profile.getWeight());
            ps.setString(5, profile.getDisease());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save profile", e);
        }
    }

    @Override
    public void delete(String username) {
        String sql = "DELETE FROM profiles WHERE username = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete profile", e);
        }
    }

    private Profile mapProfile(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setUsername(rs.getString("username"));
        profile.setName(rs.getString("name"));
        profile.setHeight(rs.getDouble("height"));
        profile.setWeight(rs.getDouble("weight"));
        profile.setDisease(rs.getString("disease"));
        return profile;
    }
}
