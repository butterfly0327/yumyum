package com.yumyumcoach.model.service;

import java.util.List;

import com.yumyumcoach.model.dto.ExerciseRecord;

public interface ExerciseService {
    List<ExerciseRecord> findAll();

    void save(ExerciseRecord record);
}
