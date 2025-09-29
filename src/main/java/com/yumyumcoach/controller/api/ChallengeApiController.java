package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.Challenge;
import com.yumyumcoach.model.service.ChallengeService;
import com.yumyumcoach.model.service.impl.ChallengeServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/challenges/*")
public class ChallengeApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChallengeService challengeService = ChallengeServiceImpl.getInstance();

    public ChallengeApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path != null && path.contains("participants")) {
            writeJson(response, challengeService.participants(), mapper);
            return;
        }
        writeJson(response, challengeService.findAll(), mapper);
    }

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
        String path = request.getPathInfo();
        if (path != null && path.split("/").length == 3 && "join".equals(path.split("/")[2])) {
            long id = Long.parseLong(path.split("/")[1]);
            challengeService.join(id, account.getUsername());
            writeJson(response, new ApiResponse(true, "참여 완료"), mapper);
            return;
        }
        Challenge challenge = mapper.readValue(request.getInputStream(), Challenge.class);
        challenge.setCreator(account.getUsername());
        if (challenge.getStartDate() == null) {
            challenge.setStartDate(LocalDate.now());
        }
        Challenge saved = challengeService.create(challenge);
        writeJson(response, saved, mapper);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Account account = session != null ? (Account) session.getAttribute("loginUser") : null;
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }
        String path = request.getPathInfo();
        if (path == null || path.split("/").length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long id = Long.parseLong(path.split("/")[1]);
        Optional<Challenge> challenge = challengeService.findById(id);
        if (challenge.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(response, new ApiResponse(false, "챌린지를 찾을 수 없습니다."), mapper);
            return;
        }
        if (!account.getUsername().equals(challenge.get().getCreator()) && !"admin".equals(account.getUsername())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(response, new ApiResponse(false, "삭제 권한이 없습니다."), mapper);
            return;
        }
        challengeService.delete(id);
        writeJson(response, new ApiResponse(true, "삭제되었습니다."), mapper);
    }
}
