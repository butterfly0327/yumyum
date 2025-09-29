<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>마이페이지 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="mypage-view">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-lg-6">
            <div class="card p-3 mb-4">
                <h3 class="mb-3">기본 정보</h3>
                <ul class="list-group">
                    <li class="list-group-item d-flex justify-content-between"><span>이름</span><strong id="view-name">-</strong></li>
                    <li class="list-group-item d-flex justify-content-between"><span>키</span><strong id="view-height">-</strong></li>
                    <li class="list-group-item d-flex justify-content-between"><span>몸무게</span><strong id="view-weight">-</strong></li>
                    <li class="list-group-item d-flex justify-content-between"><span>질환 정보</span><strong id="view-disease">-</strong></li>
                </ul>
                <a href="${root}/main?action=mypageEdit" class="btn btn-primary mt-3">프로필 수정</a>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card p-3">
                <h3 class="mb-3">팔로잉 목록</h3>
                <div id="following-list" class="list-group"></div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/mypage_view.js"></script>
</body>
</html>
