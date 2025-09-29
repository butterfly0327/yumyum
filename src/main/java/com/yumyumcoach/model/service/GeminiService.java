package com.yumyumcoach.model.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GeminiService {
    private static final String MODEL_NAME = "gemini-2.0-flash";
    private static final GeminiService INSTANCE = new GeminiService();

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private GeminiService() {
        this.httpClient = HttpClient.newBuilder().build();
        this.mapper = new ObjectMapper();
    }

    public static GeminiService getInstance() {
        return INSTANCE;
    }

    public String generateText(String apiKey, String prompt) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API 키가 필요합니다.");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("프롬프트가 필요합니다.");
        }

        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                MODEL_NAME, encodedKey);

        ObjectNode requestBody = mapper.createObjectNode();
        ObjectNode content = requestBody.putArray("contents").addObject();
        content.putArray("parts").addObject().put("text", prompt);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() >= 400) {
            String errorMessage = extractErrorMessage(httpResponse.body());
            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "외부 AI 서비스 요청이 실패했습니다. (HTTP " + httpResponse.statusCode() + ")";
            }
            throw new IOException(errorMessage);
        }

        JsonNode root = mapper.readTree(httpResponse.body());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IOException("AI 응답을 해석할 수 없습니다.");
        }
        return textNode.asText();
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = mapper.readTree(body);
            JsonNode error = node.path("error");
            if (!error.isMissingNode()) {
                JsonNode message = error.path("message");
                if (!message.isMissingNode() && !message.asText().isBlank()) {
                    return message.asText();
                }
            }
        } catch (IOException ignore) {
            // 응답이 JSON이 아닐 수 있으므로 무시합니다.
        }
        return null;
    }
}
