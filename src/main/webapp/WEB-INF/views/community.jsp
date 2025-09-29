<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="root" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>커뮤니티 - YumYumCoach</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${root}/resources/css/style.css">
</head>
<body data-page="community">
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<div class="container mt-4">
    <div class="row">
        <div class="col-lg-4 mb-4">
            <div class="card p-3">
                <h3 class="mb-3">게시글 작성</h3>
                <form id="post-form">
                    <div class="mb-3">
                        <label for="post-title" class="form-label">제목</label>
                        <input type="text" class="form-control" id="post-title" required>
                    </div>
                    <div class="mb-3">
                        <label for="post-category" class="form-label">카테고리</label>
                        <select class="form-select" id="post-category">
                            <option value="자유">자유</option>
                            <option value="질문">질문</option>
                            <option value="후기">후기</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="post-content" class="form-label">내용</label>
                        <textarea class="form-control" id="post-content" rows="5" required></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">등록</button>
                </form>
            </div>
        </div>
        <div class="col-lg-8">
            <div class="card p-3">
                <h3 class="mb-3">게시글 목록</h3>
                <div id="posts-container"></div>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="editModal" tabindex="-1" aria-labelledby="editModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="editModalLabel">게시글 수정</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <form id="edit-post-form">
                    <input type="hidden" id="edit-post-id">
                    <div class="mb-3">
                        <label for="edit-title" class="form-label">제목</label>
                        <input type="text" class="form-control" id="edit-title" required>
                    </div>
                    <div class="mb-3">
                        <label for="edit-category" class="form-label">카테고리</label>
                        <select class="form-select" id="edit-category">
                            <option value="자유">자유</option>
                            <option value="질문">질문</option>
                            <option value="후기">후기</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="edit-content" class="form-label">내용</label>
                        <textarea class="form-control" id="edit-content" rows="5" required></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                <button type="button" class="btn btn-primary" id="save-edit-btn">저장</button>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${root}/resources/js/common.js"></script>
<script src="${root}/resources/js/community.js"></script>
</body>
</html>
