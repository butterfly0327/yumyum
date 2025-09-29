package com.yumyumcoach.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.ExerciseRecord;

public class JdbcExerciseDao implements ExerciseDao {
    private static final JdbcExerciseDao INSTANCE = new JdbcExerciseDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcExerciseDao getInstance() {
        return INSTANCE;
    }

    private JdbcExerciseDao() {
    }

    @Override
    public List<ExerciseRecord> findAll() {
        String sql = "SELECT username, record_date, calories FROM exercise_records ORDER BY record_date DESC, username";
        List<ExerciseRecord> records = new ArrayList<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ExerciseRecord record = new ExerciseRecord();
                record.setUsername(rs.getString("username"));
                Date date = rs.getDate("record_date");
                if (date != null) {
                    record.setDate(date.toLocalDate());
                }
                record.setCalories(rs.getDouble("calories"));
                records.add(record);
            }
            return records;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load exercise records", e);
        }
    }

    @Override
    public void save(ExerciseRecord record) {
        String sql = "INSERT INTO exercise_records (username, record_date, calories) VALUES (?, ?, ?)";
        LocalDate date = record.getDate() != null ? record.getDate() : LocalDate.now();
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getUsername());
            ps.setDate(2, Date.valueOf(date));
            ps.setDouble(3, record.getCalories());
            ps.executeUpdate();
            record.setDate(date);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save exercise record", e);
        }
    }

    @Override
    public void saveAll() {
        // No-op for JDBC implementation
    }
}
