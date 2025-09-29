package com.yumyumcoach.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/main")
public class MainController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = getActionParameter(request);
        switch (action) {
            case "landing" -> forward(request, response, "/WEB-INF/views/landing.jsp");
            case "diet" -> forward(request, response, "/WEB-INF/views/diet.jsp");
            case "challenge" -> forward(request, response, "/WEB-INF/views/challenge.jsp");
            case "community" -> forward(request, response, "/WEB-INF/views/community.jsp");
            case "aiCoach" -> forward(request, response, "/WEB-INF/views/ai-coach.jsp");
            case "analysis" -> forward(request, response, "/WEB-INF/views/analysis.jsp");
            case "exercise" -> forward(request, response, "/WEB-INF/views/exercise.jsp");
            case "login" -> forward(request, response, "/WEB-INF/views/login.jsp");
            case "register" -> forward(request, response, "/WEB-INF/views/register.jsp");
            case "mypage" -> forward(request, response, "/WEB-INF/views/mypage_view.jsp");
            case "mypageEdit" -> forward(request, response, "/WEB-INF/views/mypage_edit.jsp");
            default -> forward(request, response, "/WEB-INF/views/landing.jsp");
        }
    }
}
