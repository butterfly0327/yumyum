package com.yumyumcoach.controller.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.AiCoachResponse;
import com.yumyumcoach.model.dto.AiPromptRequest;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.service.GeminiService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/ai/coach")
public class AiCoachApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;

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
            String aiText = geminiService.generateText(promptRequest.getApiKey(), promptRequest.getPrompt());
            AiCoachResponse body = new AiCoachResponse(true, "success", aiText);
            writeJson(response, body, mapper);
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
}
