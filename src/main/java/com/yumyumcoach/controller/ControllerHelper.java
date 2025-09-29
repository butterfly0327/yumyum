package com.yumyumcoach.controller;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ControllerHelper {
    default String getActionParameter(HttpServletRequest request) {
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "landing";
        }
        return action;
    }

    default void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    default void forward(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }

    default void writeJson(HttpServletResponse response, Object body, ObjectMapper mapper) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        mapper.writeValue(response.getWriter(), body);
    }
}
