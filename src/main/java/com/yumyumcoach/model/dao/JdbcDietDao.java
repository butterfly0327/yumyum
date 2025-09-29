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
import java.util.stream.Collectors;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.DietRecord;
import com.yumyumcoach.model.dto.FoodItem;

public class JdbcDietDao implements DietDao {
    private static final JdbcDietDao INSTANCE = new JdbcDietDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcDietDao getInstance() {
        return INSTANCE;
    }

    private JdbcDietDao() {
    }

    @Override
    public List<DietRecord> findAll() {
        String sql = "SELECT id, record_date, meal_type FROM diet_records ORDER BY record_date DESC, id DESC";
        Map<Long, DietRecord> records = new LinkedHashMap<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DietRecord record = mapRecord(rs);
                records.put(record.getId(), record);
            }
            loadFoods(conn, records);
            return new ArrayList<>(records.values());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load diet records", e);
        }
    }

    @Override
    public DietRecord insert(DietRecord record) {
        String sql = "INSERT INTO diet_records (record_date, meal_type) VALUES (?, ?)";
        LocalDate date = record.getDate() != null ? record.getDate() : LocalDate.now();
        try (Connection conn = manager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDate(1, Date.valueOf(date));
                    ps.setString(2, record.getMealType());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            id = rs.getLong(1);
                        } else {
                            throw new DataAccessException("Failed to retrieve generated diet record id");
                        }
                    }
                }
                record.setId(id);
                record.setDate(date);
                insertFoods(conn, record);
                conn.commit();
                return record;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert diet record", e);
        }
    }

    @Override
    public Optional<DietRecord> findById(long id) {
        String sql = "SELECT id, record_date, meal_type FROM diet_records WHERE id = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DietRecord record = mapRecord(rs);
                    Map<Long, DietRecord> map = new LinkedHashMap<>();
                    map.put(record.getId(), record);
                    loadFoods(conn, map);
                    return Optional.of(record);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load diet record", e);
        }
    }

    @Override
    public void saveAll() {
        // No-op for JDBC implementation
    }

    @Override
    public boolean deleteFoodItem(long dietId, int foodIndex) {
        String deleteSql = "DELETE FROM diet_foods WHERE diet_id = ? AND order_index = ?";
        String shiftSql = "UPDATE diet_foods SET order_index = order_index - 1 WHERE diet_id = ? AND order_index > ?";
        String countSql = "SELECT COUNT(*) FROM diet_foods WHERE diet_id = ?";
        String deleteRecordSql = "DELETE FROM diet_records WHERE id = ?";
        try (Connection conn = manager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int deleted;
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, dietId);
                    ps.setInt(2, foodIndex);
                    deleted = ps.executeUpdate();
                }
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement ps = conn.prepareStatement(shiftSql)) {
                    ps.setLong(1, dietId);
                    ps.setInt(2, foodIndex);
                    ps.executeUpdate();
                }
                int remaining;
                try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                    ps.setLong(1, dietId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        remaining = rs.getInt(1);
                    }
                }
                if (remaining == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(deleteRecordSql)) {
                        ps.setLong(1, dietId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete diet food item", e);
        }
    }

    @Override
    public long nextId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM diet_records";
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("next_id");
            }
            return 1L;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute next diet record id", e);
        }
    }

    private DietRecord mapRecord(ResultSet rs) throws SQLException {
        DietRecord record = new DietRecord();
        record.setId(rs.getLong("id"));
        Date date = rs.getDate("record_date");
        if (date != null) {
            record.setDate(date.toLocalDate());
        }
        record.setMealType(rs.getString("meal_type"));
        return record;
    }

    private void insertFoods(Connection conn, DietRecord record) throws SQLException {
        if (record.getFoods() == null || record.getFoods().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO diet_foods (diet_id, order_index, food_code, food_name, energy, carbohydrate, protein, fat, weight) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 0;
            for (FoodItem item : record.getFoods()) {
                ps.setLong(1, record.getId());
                ps.setInt(2, index++);
                ps.setString(3, item.getCode());
                ps.setString(4, item.getName());
                ps.setDouble(5, item.getEnergy());
                ps.setDouble(6, item.getCarbohydrate());
                ps.setDouble(7, item.getProtein());
                ps.setDouble(8, item.getFat());
                ps.setString(9, item.getWeight());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void loadFoods(Connection conn, Map<Long, DietRecord> records) throws SQLException {
        if (records.isEmpty()) {
            return;
        }
        String placeholders = records.keySet().stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT diet_id, order_index, food_code, food_name, energy, carbohydrate, protein, fat, weight FROM diet_foods "
                + "WHERE diet_id IN (" + placeholders + ") ORDER BY diet_id, order_index";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (Long id : records.keySet()) {
                ps.setLong(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long dietId = rs.getLong("diet_id");
                    DietRecord record = records.get(dietId);
                    if (record == null) {
                        continue;
                    }
                    FoodItem item = new FoodItem();
                    item.setCode(rs.getString("food_code"));
                    item.setName(rs.getString("food_name"));
                    item.setEnergy(rs.getDouble("energy"));
                    item.setCarbohydrate(rs.getDouble("carbohydrate"));
                    item.setProtein(rs.getDouble("protein"));
                    item.setFat(rs.getDouble("fat"));
                    item.setWeight(rs.getString("weight"));
                    record.getFoods().add(item);
                }
            }
        }
    }
}
