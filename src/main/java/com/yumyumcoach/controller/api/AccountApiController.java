package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.service.AccountService;
import com.yumyumcoach.model.service.ProfileService;
import com.yumyumcoach.model.service.impl.AccountServiceImpl;
import com.yumyumcoach.model.service.impl.ProfileServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/account")
public class AccountApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AccountService accountService = AccountServiceImpl.getInstance();
    private final ProfileService profileService = ProfileServiceImpl.getInstance();

    public AccountApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account account = currentAccount(request);
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }
        Map<String, String> body = mapper.readValue(request.getInputStream(), Map.class);
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (!account.getPassword().equals(currentPassword)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, new ApiResponse(false, "현재 비밀번호가 올바르지 않습니다."), mapper);
            return;
        }
        if (newPassword != null && !newPassword.isBlank()) {
            account.setPassword(newPassword);
            accountService.update(account);
        }
        writeJson(response, new ApiResponse(true, "정보가 수정되었습니다."), mapper);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account account = currentAccount(request);
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }
        Map<String, String> body = mapper.readValue(request.getInputStream(), Map.class);
        String currentPassword = body.get("currentPassword");
        if (!account.getPassword().equals(currentPassword)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, new ApiResponse(false, "비밀번호가 올바르지 않습니다."), mapper);
            return;
        }
        accountService.delete(account.getUsername());
        profileService.delete(account.getUsername());
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeJson(response, new ApiResponse(true, "회원 탈퇴가 완료되었습니다."), mapper);
    }

    private Account currentAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (Account) session.getAttribute("loginUser") : null;
    }
}
