package com.yumyumcoach.model.dao;

import java.util.List;

import com.yumyumcoach.model.dto.ExerciseRecord;

public interface ExerciseDao {
    List<ExerciseRecord> findAll();

    void save(ExerciseRecord record);

    void saveAll();
}
