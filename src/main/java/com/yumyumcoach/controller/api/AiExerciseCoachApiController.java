package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.AiExerciseResponse;
import com.yumyumcoach.model.dto.AiPromptRequest;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.service.GeminiService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/ai/exercise")
public class AiExerciseCoachApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private final ObjectMapper mapper = new ObjectMapper();
    private final GeminiService geminiService = GeminiService.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = session != null ? (Account) session.getAttribute("loginUser") : null;
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }

        AiPromptRequest promptRequest = mapper.readValue(request.getInputStream(), AiPromptRequest.class);
        if (promptRequest.getApiKey() == null || promptRequest.getApiKey().isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, new ApiResponse(false, "API 키를 입력해주세요."), mapper);
            return;
        }
        if (promptRequest.getPrompt() == null || promptRequest.getPrompt().isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, new ApiResponse(false, "프롬프트를 입력해주세요."), mapper);
            return;
        }

        try {
            String aiText = geminiService.generateText(promptRequest.getApiKey(), promptRequest.getPrompt()).trim();
            Integer calories = extractCalories(aiText);
            if (calories != null) {
                AiExerciseResponse body = new AiExerciseResponse(true, "success", calories, aiText);
                writeJson(response, body, mapper);
            } else {
                AiExerciseResponse body = new AiExerciseResponse(false,
                        "AI가 칼로리 값을 반환하지 않았습니다. 질문을 더 구체적으로 작성해보세요.", null, aiText);
                writeJson(response, body, mapper);
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, new ApiResponse(false, e.getMessage()), mapper);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, new ApiResponse(false, "AI 요청이 중단되었습니다. 잠시 후 다시 시도해주세요."), mapper);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            writeJson(response, new ApiResponse(false, e.getMessage()), mapper);
        }
    }

    private Integer extractCalories(String aiText) {
        if (aiText == null || aiText.isBlank()) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(aiText);
        StringBuilder digits = new StringBuilder();
        while (matcher.find()) {
            digits.append(matcher.group());
        }
        if (digits.length() == 0) {
            return null;
        }
        try {
            return Integer.valueOf(digits.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
