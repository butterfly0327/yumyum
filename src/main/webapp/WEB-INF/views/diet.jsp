<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>식단 기록 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="diet">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-md-5">
            <div class="card p-3">
                <h3 class="mb-3">식단 기록 추가</h3>
                <form id="diet-record-form">
                    <div class="mb-3">
                        <label for="date" class="form-label">날짜</label>
                        <input type="date" class="form-control" id="date" required>
                    </div>
                    <div class="mb-3">
                        <label for="meal-type" class="form-label">식사 구분</label>
                        <select class="form-select" id="meal-type">
                            <option value="아침">아침</option>
                            <option value="점심">점심</option>
                            <option value="저녁">저녁</option>
                            <option value="간식">간식</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="food-name" class="form-label">음식 이름</label>
                        <input type="text" class="form-control" id="food-name" required>
                    </div>
                    <div class="row">
                        <div class="col">
                            <label for="calories" class="form-label">칼로리(kcal)</label>
                            <input type="number" class="form-control" id="calories" min="0" step="0.1" required>
                        </div>
                        <div class="col">
                            <label for="protein" class="form-label">단백질(g)</label>
                            <input type="number" class="form-control" id="protein" min="0" step="0.1" required>
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col">
                            <label for="carbohydrate" class="form-label">탄수화물(g)</label>
                            <input type="number" class="form-control" id="carbohydrate" min="0" step="0.1" required>
                        </div>
                        <div class="col">
                            <label for="fat" class="form-label">지방(g)</label>
                            <input type="number" class="form-control" id="fat" min="0" step="0.1" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 mt-4">기록 추가</button>
                </form>
            </div>
        </div>
        <div class="col-md-7">
            <div class="card p-3">
                <h3 class="mb-3">최근 식단 기록</h3>
                <div id="diet-records-container"></div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/diet.js"></script>
</body>
</html>
