package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.DietRecord;
import com.yumyumcoach.model.service.DietService;
import com.yumyumcoach.model.service.impl.DietServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/diet/*")
public class DietApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DietService dietService = DietServiceImpl.getInstance();

    public DietApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        writeJson(response, dietService.findAll(), mapper);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DietRecord record = mapper.readValue(request.getInputStream(), DietRecord.class);
        if (record.getDate() == null) {
            Map<String, Object> body = mapper.convertValue(record, Map.class);
            Object date = body.get("date");
            if (date != null) {
                record.setDate(LocalDate.parse(String.valueOf(date)));
            }
        }
        DietRecord saved = dietService.addRecord(record);
        writeJson(response, saved, mapper);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || !path.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String[] segments = path.split("/");
        if (segments.length < 4) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long dietId = Long.parseLong(segments[1]);
        int foodIndex = Integer.parseInt(segments[3]);
        boolean result = dietService.deleteFoodItem(dietId, foodIndex);
        if (result) {
            writeJson(response, new ApiResponse(true, "삭제되었습니다."), mapper);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(response, new ApiResponse(false, "삭제할 항목을 찾을 수 없습니다."), mapper);
        }
    }
}
