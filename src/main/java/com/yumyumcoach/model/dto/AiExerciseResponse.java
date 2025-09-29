package com.yumyumcoach.model.dto;

public class AiExerciseResponse extends ApiResponse {
    private Integer calories;
    private String rawResponse;

    public AiExerciseResponse() {
    }

    public AiExerciseResponse(boolean success, String message, Integer calories, String rawResponse) {
        super(success, message);
        this.calories = calories;
        this.rawResponse = rawResponse;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
