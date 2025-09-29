package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.FollowInfo;
import com.yumyumcoach.model.service.FollowService;
import com.yumyumcoach.model.service.impl.FollowServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/follows")
public class FollowApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final FollowService followService = FollowServiceImpl.getInstance();

    public FollowApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, FollowInfo> result = followService.findAll();
        writeJson(response, result, mapper);
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
        Map<String, String> body = mapper.readValue(request.getInputStream(), Map.class);
        String followee = body.get("followee");
        String action = body.getOrDefault("action", "follow");
        boolean follow = "follow".equalsIgnoreCase(action);
        FollowInfo info = followService.toggleFollow(account.getUsername(), followee, follow);
        writeJson(response, new ApiResponse(true, follow ? "팔로우했습니다." : "언팔로우했습니다."), mapper);
    }
}
