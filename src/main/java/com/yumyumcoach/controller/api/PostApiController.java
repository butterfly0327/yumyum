package com.yumyumcoach.controller.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yumyumcoach.controller.ControllerHelper;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.dto.ApiResponse;
import com.yumyumcoach.model.dto.Comment;
import com.yumyumcoach.model.dto.Post;
import com.yumyumcoach.model.service.PostService;
import com.yumyumcoach.model.service.impl.PostServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/posts/*")
public class PostApiController extends HttpServlet implements ControllerHelper {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PostService postService = PostServiceImpl.getInstance();

    public PostApiController() {
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Post> posts = postService.findAll();
        writeJson(response, posts, mapper);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.isEmpty()) {
            createPost(request, response);
            return;
        }
        String[] segments = path.split("/");
        if (segments.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long id = Long.parseLong(segments[1]);
        if (segments.length == 3 && "like".equals(segments[2])) {
            postService.toggleLike(id);
            writeJson(response, new ApiResponse(true, "좋아요"), mapper);
        } else if (segments.length == 3 && "comment".equals(segments[2])) {
            addComment(request, response, id);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.split("/").length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long id = Long.parseLong(path.split("/")[1]);
        Optional<Post> existing = postService.findById(id);
        if (existing.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(response, new ApiResponse(false, "게시글을 찾을 수 없습니다."), mapper);
            return;
        }
        if (!isOwner(request, existing.get())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(response, new ApiResponse(false, "수정 권한이 없습니다."), mapper);
            return;
        }
        Post post = mapper.readValue(request.getInputStream(), Post.class);
        post.setId(id);
        post.setAuthor(existing.get().getAuthor());
        postService.update(post);
        writeJson(response, new ApiResponse(true, "수정되었습니다."), mapper);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || path.split("/").length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long id = Long.parseLong(path.split("/")[1]);
        Optional<Post> existing = postService.findById(id);
        if (existing.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(response, new ApiResponse(false, "게시글을 찾을 수 없습니다."), mapper);
            return;
        }
        if (!isOwner(request, existing.get())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(response, new ApiResponse(false, "삭제 권한이 없습니다."), mapper);
            return;
        }
        postService.delete(id);
        writeJson(response, new ApiResponse(true, "삭제되었습니다."), mapper);
    }

    private void createPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Account account = session != null ? (Account) session.getAttribute("loginUser") : null;
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }
        Post post = mapper.readValue(request.getInputStream(), Post.class);
        post.setAuthor(account.getUsername());
        Post saved = postService.create(post);
        writeJson(response, saved, mapper);
    }

    private void addComment(HttpServletRequest request, HttpServletResponse response, long postId) throws IOException {
        HttpSession session = request.getSession(false);
        Account account = session != null ? (Account) session.getAttribute("loginUser") : null;
        if (account == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, new ApiResponse(false, "로그인이 필요합니다."), mapper);
            return;
        }
        Map<String, String> body = mapper.readValue(request.getInputStream(), Map.class);
        Comment comment = new Comment();
        comment.setAuthor(account.getUsername());
        comment.setContent(body.get("content"));
        comment.setDate(body.getOrDefault("date", ""));
        postService.addComment(postId, comment);
        writeJson(response, new ApiResponse(true, "댓글이 등록되었습니다."), mapper);
    }

    private boolean isOwner(HttpServletRequest request, Post post) {
        HttpSession session = request.getSession(false);
        Account account = session != null ? (Account) session.getAttribute("loginUser") : null;
        if (account == null) {
            return false;
        }
        if ("admin".equals(account.getUsername())) {
            return true;
        }
        return account.getUsername().equals(post.getAuthor());
    }
}
