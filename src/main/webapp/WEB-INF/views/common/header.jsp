<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.loginUser}" />

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="${root}/main?action=landing">YumYumCoach</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <%-- Corrected Korean characters --%>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=landing">홈</a></li>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=diet">식단</a></li>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=challenge">챌린지</a></li>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=community">게시판</a></li>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=aiCoach">AI 코치</a></li>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=analysis">분석</a></li>
                <li class="nav-item"><a class="nav-link" href="${root}/main?action=exercise">운동</a></li>
            </ul>
            <div class="d-flex" id="auth-buttons" data-username="${currentUser != null ? currentUser.username : ''}">
                <c:choose>
                    <c:when test="${currentUser != null}">
                        <%-- Changed the text to "마이페이지" for clarity as it links to mypage --%>
                        <a href="${root}/main?action=mypage" class="btn btn-outline-light me-2">마이페이지</a>
                        <form id="logout-form" method="post" action="${root}/api/auth/logout" class="d-inline">
                            <%-- Corrected the "로그아웃" (Logout) button text --%>
                            <button type="submit" class="btn btn-outline-light">로그아웃</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <a href="${root}/main?action=login" class="btn btn-outline-light me-2">로그인</a>
                        <a href="${root}/main?action=register" class="btn btn-primary">회원가입</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>
<script>
    // This part remains the same, it correctly uses JSTL expressions
    window.appConfig = {
        contextPath: '${root}',
        username: '${currentUser != null ? currentUser.username : ''}'
    };
</script>
