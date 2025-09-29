package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.AuthStatus;
import com.yumyumcoach.model.service.AccountService;
import com.yumyumcoach.model.service.impl.AccountServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/auth/*")
public class AuthController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AccountService accountService = AccountServiceImpl.getInstance();

    public AuthController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || "/status".equals(path)) {
            HttpSession session = request.getSession(false);
            Account account = session != null ? (Account) session.getAttribute("loginUser") : null;
            AuthStatus status = new AuthStatus(account != null, account != null ? account.getUsername() : null);
            writeJson(response, status, mapper);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        switch (path) {
            case "/login" -> handleLogin(request, response);
            case "/logout" -> handleLogout(request, response);
            case "/register" -> handleRegister(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> body = mapper.readValue(request.getInputStream(), Map.class);
        String username = body.get("username");
        String password = body.get("password");
        Optional<Account> account = accountService.login(username, password);
        if (account.isPresent()) {
            request.getSession(true).setAttribute("loginUser", account.get());
            writeJson(response, new ApiResponse(true, "로그인 성공"), mapper);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "아이디 또는 비밀번호가 올바르지 않습니다."), mapper);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeJson(response, new ApiResponse(true, "로그아웃 되었습니다."), mapper);
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> body = mapper.readValue(request.getInputStream(), Map.class);
        Account account = new Account();
        account.setUsername(body.get("username"));
        account.setPassword(body.get("password"));
        account.setEmail(body.get("email"));
        boolean result = accountService.register(account);
        if (result) {
            writeJson(response, new ApiResponse(true, "회원가입이 완료되었습니다."), mapper);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            writeJson(response, new ApiResponse(false, "이미 존재하는 아이디입니다."), mapper);
        }
    }
}
