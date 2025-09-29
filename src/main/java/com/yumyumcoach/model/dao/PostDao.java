package com.yumyumcoach.model.dao;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dto.Comment;
import com.yumyumcoach.model.dto.Post;

public interface PostDao {
    List<Post> findAll();

    Optional<Post> findById(long id);

    Post save(Post post);

    void delete(long id);

    void saveAll();

    void incrementLikes(long id);

    void addComment(long postId, Comment comment);
}
