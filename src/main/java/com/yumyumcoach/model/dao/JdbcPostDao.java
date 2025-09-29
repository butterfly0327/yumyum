package com.yumyumcoach.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.yumyumcoach.config.DataAccessException;
import com.yumyumcoach.config.DatabaseManager;
import com.yumyumcoach.model.dto.Comment;
import com.yumyumcoach.model.dto.Post;

public class JdbcPostDao implements PostDao {
    private static final JdbcPostDao INSTANCE = new JdbcPostDao();
    private final DatabaseManager manager = DatabaseManager.getInstance();

    public static JdbcPostDao getInstance() {
        return INSTANCE;
    }

    private JdbcPostDao() {
    }

    @Override
    public List<Post> findAll() {
        String sql = "SELECT id, author, title, category, content, created_at, likes FROM posts ORDER BY id DESC";
        Map<Long, Post> posts = new LinkedHashMap<>();
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Post post = mapPost(rs);
                posts.put(post.getId(), post);
            }
            loadComments(conn, posts);
            return new ArrayList<>(posts.values());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load posts", e);
        }
    }

    @Override
    public Optional<Post> findById(long id) {
        String sql = "SELECT id, author, title, category, content, created_at, likes FROM posts WHERE id = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Post post = mapPost(rs);
                    Map<Long, Post> map = new LinkedHashMap<>();
                    map.put(post.getId(), post);
                    loadComments(conn, map);
                    return Optional.of(post);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load post", e);
        }
    }

    @Override
    public Post save(Post post) {
        if (post.getId() == 0L) {
            return insert(post);
        }
        return update(post);
    }

    @Override
    public void delete(long id) {
        String deleteComments = "DELETE FROM post_comments WHERE post_id = ?";
        String deletePost = "DELETE FROM posts WHERE id = ?";
        try (Connection conn = manager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteComments)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(deletePost)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete post", e);
        }
    }

    @Override
    public void saveAll() {
        // No-op for JDBC implementation
    }

    @Override
    public void incrementLikes(long id) {
        String sql = "UPDATE posts SET likes = likes + 1 WHERE id = ?";
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to increment likes", e);
        }
    }

    @Override
    public void addComment(long postId, Comment comment) {
        String sql = "INSERT INTO post_comments (post_id, author, content, created_at) VALUES (?, ?, ?, ?)";
        Timestamp createdAt = toTimestamp(comment.getDate());
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setString(2, comment.getAuthor());
            ps.setString(3, comment.getContent());
            ps.setTimestamp(4, createdAt);
            ps.executeUpdate();
            comment.setDate(createdAt.toLocalDateTime().toString());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add comment", e);
        }
    }

    private Post insert(Post post) {
        String sql = "INSERT INTO posts (author, title, category, content, created_at, likes) VALUES (?, ?, ?, ?, ?, ?)";
        Timestamp createdAt = toTimestamp(post.getDate());
        try (Connection conn = manager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getAuthor());
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getCategory());
            ps.setString(4, post.getContent());
            ps.setTimestamp(5, createdAt);
            ps.setInt(6, post.getLikes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    post.setId(id);
                }
            }
            post.setDate(createdAt.toLocalDateTime().toString());
            return post;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert post", e);
        }
    }

    private Post update(Post post) {
        String sql = "UPDATE posts SET author = ?, title = ?, category = ?, content = ?, created_at = ?, likes = ? WHERE id = ?";
        Timestamp createdAt = toTimestamp(post.getDate());
        try (Connection conn = manager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, post.getAuthor());
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getCategory());
            ps.setString(4, post.getContent());
            ps.setTimestamp(5, createdAt);
            ps.setInt(6, post.getLikes());
            ps.setLong(7, post.getId());
            ps.executeUpdate();
            post.setDate(createdAt.toLocalDateTime().toString());
            return post;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update post", e);
        }
    }

    private void loadComments(Connection conn, Map<Long, Post> posts) throws SQLException {
        if (posts.isEmpty()) {
            return;
        }
        String placeholders = posts.keySet().stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT post_id, author, content, created_at FROM post_comments WHERE post_id IN (" + placeholders
                + ") ORDER BY created_at";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (Long postId : posts.keySet()) {
                ps.setLong(index++, postId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long postId = rs.getLong("post_id");
                    Post post = posts.get(postId);
                    if (post == null) {
                        continue;
                    }
                    Comment comment = new Comment();
                    comment.setAuthor(rs.getString("author"));
                    comment.setContent(rs.getString("content"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        comment.setDate(createdAt.toLocalDateTime().toString());
                    }
                    post.getComments().add(comment);
                }
            }
        }
    }

    private Post mapPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setAuthor(rs.getString("author"));
        post.setTitle(rs.getString("title"));
        post.setCategory(rs.getString("category"));
        post.setContent(rs.getString("content"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            post.setDate(createdAt.toLocalDateTime().toString());
        }
        post.setLikes(rs.getInt("likes"));
        return post;
    }

    private Timestamp toTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        try {
            return Timestamp.valueOf(LocalDateTime.parse(value));
        } catch (DateTimeParseException e) {
            try {
                return Timestamp.valueOf(LocalDate.parse(value).atStartOfDay());
            } catch (DateTimeParseException ignored) {
                return Timestamp.valueOf(LocalDateTime.now());
            }
        }
    }
}
