package com.yumyumcoach.model.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.DietRecord;

public class FileDietDao implements DietDao {
    private static final FileDietDao INSTANCE = new FileDietDao();
    private final DataStore store = DataStore.getInstance();
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    public static FileDietDao getInstance() {
        return INSTANCE;
    }

    private FileDietDao() {
        long max = store.getDietRecords().stream().mapToLong(DietRecord::getId).max().orElse(System.currentTimeMillis());
        sequence.set(max + 1);
    }

    @Override
    public List<DietRecord> findAll() {
        return store.getDietRecords();
    }

    @Override
    public DietRecord insert(DietRecord record) {
        record.setId(nextId());
        if (record.getDate() == null) {
            record.setDate(LocalDate.now());
        }
        store.getDietRecords().add(record);
        store.saveDiet();
        return record;
    }

    @Override
    public Optional<DietRecord> findById(long id) {
        return store.getDietRecords().stream().filter(r -> r.getId() == id).findFirst();
    }

    @Override
    public void saveAll() {
        store.saveDiet();
    }

    @Override
    public boolean deleteFoodItem(long dietId, int foodIndex) {
        Optional<DietRecord> recordOpt = findById(dietId);
        if (recordOpt.isEmpty()) {
            return false;
        }
        DietRecord record = recordOpt.get();
        if (foodIndex < 0 || foodIndex >= record.getFoods().size()) {
            return false;
        }
        record.getFoods().remove(foodIndex);
        if (record.getFoods().isEmpty()) {
            store.getDietRecords().remove(record);
        }
        store.saveDiet();
        return true;
    }

    @Override
    public long nextId() {
        return sequence.getAndIncrement();
    }
}
