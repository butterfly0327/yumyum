<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI 코치 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
</head>
<body data-page="ai-coach">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-lg-7">
            <div class="card p-3 mb-4">
                <h3 class="mb-3">AI 식단 코치</h3>
                <div id="chat-container" class="chat-container"></div>
                <form id="chat-form" class="mt-3">
                    <div class="input-group">
                        <input type="text" id="user-input" class="form-control" placeholder="질문을 입력하세요" required>
                        <button class="btn btn-primary" id="send-btn">전송</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="col-lg-5">
            <div class="card p-3 mb-4">
                <h4 class="mb-3">오늘의 AI 추천</h4>
                <ul id="recommendation-list" class="list-group"></ul>
            </div>
            <div class="card p-3">
                <h4 class="mb-3">빠른 질문</h4>
                <div class="d-grid gap-2">
                    <button class="btn btn-outline-primary quick-question-btn" data-question="오늘 추천 식단을 알려줘">오늘 추천 식단</button>
                    <button class="btn btn-outline-primary quick-question-btn" data-question="고단백 저칼로리 식단을 추천해줘">고단백 저칼로리</button>
                    <button class="btn btn-outline-primary quick-question-btn" data-question="간식으로 건강한 것을 추천해줘">건강 간식</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/ai-coach.js"></script>
</body>
</html>
