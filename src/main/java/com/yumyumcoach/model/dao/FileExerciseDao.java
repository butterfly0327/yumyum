package com.yumyumcoach.model.dao;

import java.util.List;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.ExerciseRecord;

public class FileExerciseDao implements ExerciseDao {
    private static final FileExerciseDao INSTANCE = new FileExerciseDao();
    private final DataStore store = DataStore.getInstance();

    public static FileExerciseDao getInstance() {
        return INSTANCE;
    }

    private FileExerciseDao() {
    }

    @Override
    public List<ExerciseRecord> findAll() {
        return store.getExerciseRecords();
    }

    @Override
    public void save(ExerciseRecord record) {
        store.getExerciseRecords().add(record);
        store.saveExercise();
    }

    @Override
    public void saveAll() {
        store.saveExercise();
    }
}
