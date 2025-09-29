package com.yumyumcoach.model.service.impl;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dao.JdbcPostDao;
import com.yumyumcoach.model.dao.PostDao;
import com.yumyumcoach.model.dto.Comment;
import com.yumyumcoach.model.dto.Post;
import com.yumyumcoach.model.service.PostService;

public class PostServiceImpl implements PostService {
    private static final PostService INSTANCE = new PostServiceImpl();
    private final PostDao postDao = JdbcPostDao.getInstance();

    public static PostService getInstance() {
        return INSTANCE;
    }

    private PostServiceImpl() {
    }

    @Override
    public List<Post> findAll() {
        return postDao.findAll();
    }

    @Override
    public Post create(Post post) {
        return postDao.save(post);
    }

    @Override
    public Optional<Post> findById(long id) {
        return postDao.findById(id);
    }

    @Override
    public void update(Post post) {
        postDao.save(post);
    }

    @Override
    public void delete(long id) {
        postDao.delete(id);
    }

    @Override
    public void toggleLike(long id) {
        postDao.incrementLikes(id);
    }

    @Override
    public void addComment(long id, Comment comment) {
        postDao.addComment(id, comment);
    }
}
