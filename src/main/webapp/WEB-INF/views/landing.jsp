<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>YumYumCoach - 건강 식단</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="landing">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
    <div class="container mt-4">
        <div class="row">
            <div class="col-md-6">
                <div class="card p-3 h-100">
                    <h4>오늘의 식단 요약</h4>
                    <div id="daily-summary" class="progress-container"></div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card p-3 h-100">
                    <h4>영양 목표</h4>
                    <ul id="nutrition-goals" class="list-group list-group-flush">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span>일일 권장 칼로리</span>
                            <span class="badge bg-primary rounded-pill">2000 kcal</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span>단백질</span>
                            <span class="badge bg-info rounded-pill">100 g</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span>탄수화물</span>
                            <span class="badge bg-warning rounded-pill">250 g</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span>지방</span>
                            <span class="badge bg-danger rounded-pill">65 g</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card p-3">
                    <h4>오늘의 식단 상세</h4>
                    <table class="table">
                        <thead>
                        <tr>
                            <th>음식</th>
                            <th>칼로리</th>
                            <th>단백질</th>
                            <th>탄수화물</th>
                            <th>지방</th>
                        </tr>
                        </thead>
                        <tbody id="daily-details"></tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="row mt-4">
            <div class="col-md-6">
                <div class="card p-3 h-100">
                    <h4>주간 통계</h4>
                    <div class="row" id="weekly-stats"></div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card p-3 h-100">
                    <h4>주간 영양 섭취 현황</h4>
                    <div class="chart-container">
                        <canvas id="nutritionChart"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
    <script src="${root}/resources/js/common.js"></script>
    <script src="${root}/resources/js/script.js"></script>
</body>
</html>
