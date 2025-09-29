package com.yumyumcoach.model.dao;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dto.DietRecord;
import com.yumyumcoach.model.dto.FoodItem;

public interface DietDao {
    List<DietRecord> findAll();

    DietRecord insert(DietRecord record);

    Optional<DietRecord> findById(long id);

    void saveAll();

    boolean deleteFoodItem(long dietId, int foodIndex);

    long nextId();
}
