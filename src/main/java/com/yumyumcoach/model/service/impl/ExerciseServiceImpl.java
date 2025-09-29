package com.yumyumcoach.model.service.impl;

import java.util.List;

import com.yumyumcoach.model.dao.ExerciseDao;
import com.yumyumcoach.model.dao.JdbcExerciseDao;
import com.yumyumcoach.model.dto.ExerciseRecord;
import com.yumyumcoach.model.service.ExerciseService;

public class ExerciseServiceImpl implements ExerciseService {
    private static final ExerciseService INSTANCE = new ExerciseServiceImpl();
    private final ExerciseDao exerciseDao = JdbcExerciseDao.getInstance();

    public static ExerciseService getInstance() {
        return INSTANCE;
    }

    private ExerciseServiceImpl() {
    }

    @Override
    public List<ExerciseRecord> findAll() {
        return exerciseDao.findAll();
    }

    @Override
    public void save(ExerciseRecord record) {
        exerciseDao.save(record);
    }
}
