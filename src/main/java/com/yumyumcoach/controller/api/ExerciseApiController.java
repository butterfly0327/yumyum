package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.ExerciseRecord;
import com.yumyumcoach.model.service.ExerciseService;
import com.yumyumcoach.model.service.impl.ExerciseServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/exercise-records")
public class ExerciseApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExerciseService exerciseService = ExerciseServiceImpl.getInstance();

    public ExerciseApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        List<ExerciseRecord> records = exerciseService.findAll();
        if (username != null && !username.isBlank()) {
            records = records.stream()
                    .filter(record -> username.equals(record.getUsername()))
                    .collect(Collectors.toList());
        }
        writeJson(response, records, mapper);
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
        ExerciseRecord record = mapper.readValue(request.getInputStream(), ExerciseRecord.class);
        if (record.getDate() == null) {
            Map<String, Object> body = mapper.convertValue(record, Map.class);
            Object date = body.get("date");
            if (date != null) {
                record.setDate(LocalDate.parse(String.valueOf(date)));
            } else {
                record.setDate(LocalDate.now());
            }
        }
        record.setUsername(account.getUsername());
        exerciseService.save(record);
        writeJson(response, new ApiResponse(true, "저장되었습니다."), mapper);
    }
}
