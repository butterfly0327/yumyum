<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>프로필 수정 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="mypage-edit">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-lg-6">
            <div class="card p-3 mb-4">
                <h3 class="mb-3">계정 정보 수정</h3>
                <form id="account-form">
                    <div class="mb-3">
                        <label for="username" class="form-label">아이디</label>
                        <input type="text" class="form-control" id="username" readonly>
                    </div>
                    <div class="mb-3">
                        <label for="currentPassword" class="form-label">현재 비밀번호</label>
                        <input type="password" class="form-control" id="currentPassword" required>
                    </div>
                    <div class="mb-3">
                        <label for="newPassword" class="form-label">새 비밀번호</label>
                        <input type="password" class="form-control" id="newPassword">
                    </div>
                    <div class="d-flex justify-content-between">
                        <button type="submit" class="btn btn-primary">정보 수정</button>
                        <button type="button" class="btn btn-danger" id="delete-account-btn">회원 탈퇴</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card p-3">
                <h3 class="mb-3">프로필 정보</h3>
                <form id="profile-form">
                    <div class="mb-3">
                        <label for="name" class="form-label">이름</label>
                        <input type="text" class="form-control" id="name">
                    </div>
                    <div class="mb-3">
                        <label for="height" class="form-label">키</label>
                        <input type="number" class="form-control" id="height" step="0.1">
                    </div>
                    <div class="mb-3">
                        <label for="weight" class="form-label">몸무게</label>
                        <input type="number" class="form-control" id="weight" step="0.1">
                    </div>
                    <div class="mb-3">
                        <label for="disease" class="form-label">질환 정보</label>
                        <input type="text" class="form-control" id="disease">
                    </div>
                    <button type="submit" class="btn btn-success w-100">프로필 저장</button>
                </form>
                <p id="message" class="mt-3 text-center"></p>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/mypage_edit.js"></script>
</body>
</html>
