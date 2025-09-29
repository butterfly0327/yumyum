package com.yumyumcoach.controller.api;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.Profile;
import com.yumyumcoach.model.service.ProfileService;
import com.yumyumcoach.model.service.impl.ProfileServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/profile")
public class ProfileApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProfileService profileService = ProfileServiceImpl.getInstance();

    public ProfileApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account account = currentAccount(request);
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }
        Profile profile = profileService.findByUsername(account.getUsername())
                .orElseGet(() -> new Profile(account.getUsername(), "", 0, 0, ""));
        writeJson(response, profile, mapper);
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
        Profile profile = mapper.readValue(request.getInputStream(), Profile.class);
        profile.setUsername(account.getUsername());
        profileService.save(profile);
        writeJson(response, new ApiResponse(true, "저장되었습니다."), mapper);
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
        profileService.delete(account.getUsername());
        writeJson(response, new ApiResponse(true, "삭제되었습니다."), mapper);
    }

    private Account currentAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (Account) session.getAttribute("loginUser") : null;
    }
}
