<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>운동 코치 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="exercise">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-lg-6">
            <div class="card p-3 mb-4">
                <h4 class="mb-3">Gemini API 키</h4>
                <form id="api-key-form" class="row g-2">
                    <div class="col-12">
                        <input type="password" id="api-key-input" class="form-control" placeholder="Gemini API 키를 입력하세요" required>
                    </div>
                    <div class="col-12 d-grid">
                        <button type="submit" class="btn btn-outline-secondary">API 키 설정</button>
                    </div>
                </form>
                <p class="small text-muted mt-2" id="api-key-status">API 키는 질문을 전송할 때만 사용되며 서버에 저장되지 않습니다.</p>
            </div>
            <div class="card p-3 mb-4">
                <h3 class="mb-3">AI 운동 코치</h3>
                <form id="exercise-form">
                    <div class="mb-3">
                        <label for="exercise-prompt" class="form-label">운동 내용을 입력하세요</label>
                        <textarea class="form-control" id="exercise-prompt" rows="5" placeholder="예: 30분 동안 빠르게 걷기" required></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="exercise-date" class="form-label">운동 날짜</label>
                        <input type="date" class="form-control" id="exercise-date" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">AI에게 질문하기</button>
                </form>
            </div>
            <div class="card p-3">
                <h4>AI 응답</h4>
                <div id="ai-response" class="min-vh-25">
                    <p class="text-muted" id="ai-placeholder">여기에 AI의 답변이 표시됩니다.</p>
                </div>
                <div id="save-section" class="mt-3 d-none">
                    <p>예상 소모 칼로리: <strong id="calories-to-save">0</strong> kcal</p>
                    <button class="btn btn-success w-100" id="save-exercise-btn">운동 기록 저장</button>
                </div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card p-3 h-100">
                <h3 class="mb-3">이번 주 소모 칼로리</h3>
                <p class="text-end fw-bold">총합: <span id="total-weekly-calories">0 kcal</span></p>
                <canvas id="weekly-calories-chart"></canvas>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/exercise.js"></script>
</body>
</html>
