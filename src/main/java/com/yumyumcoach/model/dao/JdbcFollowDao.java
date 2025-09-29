package com.yumyumcoach.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.FollowInfo;

public class JdbcFollowDao implements FollowDao {
    private static final JdbcFollowDao INSTANCE = new JdbcFollowDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcFollowDao getInstance() {
        return INSTANCE;
    }

    private JdbcFollowDao() {
    }

    @Override
    public Map<String, FollowInfo> findAll() {
        String sql = "SELECT follower_username, followee_username FROM follows";
        Map<String, FollowInfo> map = new LinkedHashMap<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String follower = rs.getString("follower_username");
                String followee = rs.getString("followee_username");
                FollowInfo followerInfo = map.computeIfAbsent(follower, key -> new FollowInfo());
                followerInfo.getFollowing().add(followee);
                FollowInfo followeeInfo = map.computeIfAbsent(followee, key -> new FollowInfo());
                followeeInfo.getFollowers().add(follower);
            }
            return map;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load follow relationships", e);
        }
    }

    @Override
    public Optional<FollowInfo> findByUsername(String username) {
        FollowInfo info = new FollowInfo();
        String followingSql = "SELECT followee_username FROM follows WHERE follower_username = ? ORDER BY followee_username";
        String followersSql = "SELECT follower_username FROM follows WHERE followee_username = ? ORDER BY follower_username";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(followingSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    info.getFollowing().add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load following list", e);
        }
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(followersSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    info.getFollowers().add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load followers list", e);
        }
        if (info.getFollowing().isEmpty() && info.getFollowers().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(info);
    }

    @Override
    public void save(String username, FollowInfo info) {
        String deleteSql = "DELETE FROM follows WHERE follower_username = ?";
        String insertSql = "INSERT INTO follows (follower_username, followee_username) VALUES (?, ?)";
        List<String> following = info.getFollowing();
        try (Connection conn = manager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                }
                if (following != null && !following.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        for (String followee : following) {
                            ps.setString(1, username);
                            ps.setString(2, followee);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save follow information", e);
        }
    }

    @Override
    public void saveAll() {
        // No-op for JDBC implementation
    }
}
