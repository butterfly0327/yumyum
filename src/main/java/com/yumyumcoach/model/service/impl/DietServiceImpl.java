package com.yumyumcoach.model.service.impl;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dao.DietDao;
import com.yumyumcoach.model.dao.JdbcDietDao;
import com.yumyumcoach.model.dto.DietRecord;
import com.yumyumcoach.model.service.DietService;

public class DietServiceImpl implements DietService {
    private static final DietService INSTANCE = new DietServiceImpl();
    private final DietDao dietDao = JdbcDietDao.getInstance();

    public static DietService getInstance() {
        return INSTANCE;
    }

    private DietServiceImpl() {
    }

    @Override
    public List<DietRecord> findAll() {
        return dietDao.findAll();
    }

    @Override
    public DietRecord addRecord(DietRecord record) {
        return dietDao.insert(record);
    }

    @Override
    public boolean deleteFoodItem(long dietId, int foodIndex) {
        return dietDao.deleteFoodItem(dietId, foodIndex);
    }

    @Override
    public Optional<DietRecord> findById(long id) {
        return dietDao.findById(id);
    }
}
