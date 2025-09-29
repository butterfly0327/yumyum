<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>식단 분석 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
</head>
<body data-page="analysis">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <h2 class="mb-4">주간 식단 분석</h2>
    <div class="row">
        <div class="col-md-6 mb-4">
            <div class="card p-3 h-100">
                <h4>주간 영양소 비율</h4>
                <div class="chart-wrapper">
                    <canvas id="nutritionPieChart"></canvas>
                </div>
            </div>
        </div>
        <div class="col-md-6 mb-4">
            <div class="card p-3 h-100">
                <h4>일별 영양 섭취량</h4>
                <div class="chart-wrapper">
                    <canvas id="dailyNutritionChart"></canvas>
                </div>
            </div>
        </div>
    </div>
    <div class="row" id="analysis-results-container"></div>
    <div class="row" id="z-score-results"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels@2.2.0"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/analysis.js"></script>
</body>
</html>
