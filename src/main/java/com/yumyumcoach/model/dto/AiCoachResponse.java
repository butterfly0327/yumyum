package com.yumyumcoach.model.dto;

public class AiCoachResponse extends ApiResponse {
    private String content;

    public AiCoachResponse() {
    }

    public AiCoachResponse(boolean success, String message, String content) {
        super(success, message);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
