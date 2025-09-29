package com.yumyumcoach.model.dto;

import java.time.LocalDate;

public class ExerciseRecord {
    private LocalDate date;
    private String username;
    private double calories;

    public ExerciseRecord() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }
}
