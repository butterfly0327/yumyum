package com.yumyumcoach.model.service;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dto.Comment;
import com.yumyumcoach.model.dto.Post;

public interface PostService {
    List<Post> findAll();

    Post create(Post post);

    Optional<Post> findById(long id);

    void update(Post post);

    void delete(long id);

    void toggleLike(long id);

    void addComment(long id, Comment comment);
}
