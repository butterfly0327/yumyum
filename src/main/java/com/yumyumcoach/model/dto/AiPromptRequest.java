package com.yumyumcoach.model.dto;

public class AiPromptRequest {
    private String apiKey;
    private String prompt;

    public AiPromptRequest() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
