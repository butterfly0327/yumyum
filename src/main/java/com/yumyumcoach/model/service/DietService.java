package com.yumyumcoach.model.service;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dto.DietRecord;

public interface DietService {
    List<DietRecord> findAll();

    DietRecord addRecord(DietRecord record);

    boolean deleteFoodItem(long dietId, int foodIndex);

    Optional<DietRecord> findById(long id);
}
