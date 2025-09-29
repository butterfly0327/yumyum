package com.yumyumcoach.model.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DietRecord {
    private long id;
    private LocalDate date;
    private String mealType;
    private List<FoodItem> foods = new ArrayList<>();

    public DietRecord() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public List<FoodItem> getFoods() {
        return foods;
    }

    public void setFoods(List<FoodItem> foods) {
        this.foods = foods;
    }
}
