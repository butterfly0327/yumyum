<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>챌린지 관리 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="challenge">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-lg-4">
            <div class="card p-3">
                <h3 class="mb-3">챌린지 생성</h3>
                <form id="create-challenge-form">
                    <div class="mb-3">
                        <label for="challenge-title" class="form-label">챌린지 제목</label>
                        <input type="text" class="form-control" id="challenge-title" required>
                    </div>
                    <div class="mb-3">
                        <label for="challenge-type" class="form-label">목표 유형</label>
                        <select class="form-select" id="challenge-type">
                            <option value="칼로리">칼로리</option>
                            <option value="단백질">단백질</option>
                            <option value="탄수화물">탄수화물</option>
                            <option value="지방">지방</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="target-value" class="form-label">하루 목표량</label>
                        <input type="number" class="form-control" id="target-value" min="0" step="0.1" required>
                    </div>
                    <div class="mb-3">
                        <label for="duration" class="form-label">기간(일)</label>
                        <input type="number" class="form-control" id="duration" min="1" max="90" required>
                    </div>
                    <div class="mb-3">
                        <label for="challenge-description" class="form-label">설명</label>
                        <textarea class="form-control" id="challenge-description" rows="3"></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">챌린지 생성</button>
                </form>
            </div>
        </div>
        <div class="col-lg-8">
            <div class="card p-3">
                <h3 class="mb-3">진행 중인 챌린지</h3>
                <div id="ongoing-challenges"></div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/challenge.js"></script>
</body>
</html>
