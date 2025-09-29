package com.yumyumcoach.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.Challenge;

public class JdbcChallengeDao implements ChallengeDao {
    private static final JdbcChallengeDao INSTANCE = new JdbcChallengeDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcChallengeDao getInstance() {
        return INSTANCE;
    }

    private JdbcChallengeDao() {
    }

    @Override
    public List<Challenge> findAll() {
        String sql = "SELECT id, title, type, target, duration, description, start_date, creator FROM challenges ORDER BY id DESC";
        List<Challenge> challenges = new ArrayList<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                challenges.add(mapChallenge(rs));
            }
            return challenges;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load challenges", e);
        }
    }

    @Override
    public Optional<Challenge> findById(long id) {
        String sql = "SELECT id, title, type, target, duration, description, start_date, creator FROM challenges WHERE id = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapChallenge(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load challenge", e);
        }
    }

    @Override
    public Challenge save(Challenge challenge) {
        if (challenge.getId() == 0L) {
            return insert(challenge);
        }
        return update(challenge);
    }

    @Override
    public void delete(long id) {
        String deleteParticipants = "DELETE FROM challenge_participants WHERE challenge_id = ?";
        String deleteChallenge = "DELETE FROM challenges WHERE id = ?";
        try (Connection conn = manager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteParticipants)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteChallenge)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete challenge", e);
        }
    }

    @Override
    public void saveAll() {
        // No-op for JDBC implementation
    }

    @Override
    public Map<Long, List<String>> participants() {
        String sql = "SELECT challenge_id, username FROM challenge_participants ORDER BY challenge_id";
        Map<Long, List<String>> map = new LinkedHashMap<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long challengeId = rs.getLong("challenge_id");
                String username = rs.getString("username");
                map.computeIfAbsent(challengeId, key -> new ArrayList<>()).add(username);
            }
            return map;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load challenge participants", e);
        }
    }

    @Override
    public void updateParticipants(long challengeId, List<String> usernames) {
        String deleteSql = "DELETE FROM challenge_participants WHERE challenge_id = ?";
        String insertSql = "INSERT INTO challenge_participants (challenge_id, username) VALUES (?, ?)";
        try (Connection conn = manager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, challengeId);
                    ps.executeUpdate();
                }
                if (usernames != null && !usernames.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        for (String username : usernames) {
                            ps.setLong(1, challengeId);
                            ps.setString(2, username);
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
            throw new DataAccessException("Failed to update challenge participants", e);
        }
    }

    @Override
    public void saveParticipants() {
        // No-op for JDBC implementation
    }

    @Override
    public long nextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM challenges";
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("next_id");
            }
            return 1L;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute next challenge id", e);
        }
    }

    private Challenge insert(Challenge challenge) {
        String sql = "INSERT INTO challenges (title, type, target, duration, description, start_date, creator) VALUES (?, ?, ?, ?, ?, ?, ?)";
        LocalDate startDate = challenge.getStartDate() != null ? challenge.getStartDate() : LocalDate.now();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, challenge.getTitle());
            ps.setString(2, challenge.getType());
            ps.setDouble(3, challenge.getTarget());
            ps.setInt(4, challenge.getDuration());
            ps.setString(5, challenge.getDescription());
            ps.setDate(6, Date.valueOf(startDate));
            ps.setString(7, challenge.getCreator());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    challenge.setId(rs.getLong(1));
                }
            }
            challenge.setStartDate(startDate);
            return challenge;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert challenge", e);
        }
    }

    private Challenge update(Challenge challenge) {
        String sql = "UPDATE challenges SET title = ?, type = ?, target = ?, duration = ?, description = ?, start_date = ?, creator = ? WHERE id = ?";
        LocalDate startDate = challenge.getStartDate() != null ? challenge.getStartDate() : LocalDate.now();
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, challenge.getTitle());
            ps.setString(2, challenge.getType());
            ps.setDouble(3, challenge.getTarget());
            ps.setInt(4, challenge.getDuration());
            ps.setString(5, challenge.getDescription());
            ps.setDate(6, Date.valueOf(startDate));
            ps.setString(7, challenge.getCreator());
            ps.setLong(8, challenge.getId());
            ps.executeUpdate();
            challenge.setStartDate(startDate);
            return challenge;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update challenge", e);
        }
    }

    private Challenge mapChallenge(ResultSet rs) throws SQLException {
        Challenge challenge = new Challenge();
        challenge.setId(rs.getLong("id"));
        challenge.setTitle(rs.getString("title"));
        challenge.setType(rs.getString("type"));
        challenge.setTarget(rs.getDouble("target"));
        challenge.setDuration(rs.getInt("duration"));
        challenge.setDescription(rs.getString("description"));
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            challenge.setStartDate(startDate.toLocalDate());
        }
        challenge.setCreator(rs.getString("creator"));
        return challenge;
    }
}
