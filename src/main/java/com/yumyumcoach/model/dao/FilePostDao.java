package com.yumyumcoach.model.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.Comment;
import com.yumyumcoach.model.dto.Post;

public class FilePostDao implements PostDao {
    private static final FilePostDao INSTANCE = new FilePostDao();
    private final DataStore store = DataStore.getInstance();
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    public static FilePostDao getInstance() {
        return INSTANCE;
    }

    private FilePostDao() {
        long max = store.getPosts().stream().mapToLong(Post::getId).max().orElse(System.currentTimeMillis());
        sequence.set(max + 1);
    }

    @Override
    public List<Post> findAll() {
        store.getPosts().sort(Comparator.comparingLong(Post::getId).reversed());
        return store.getPosts();
    }

    @Override
    public Optional<Post> findById(long id) {
        return store.getPosts().stream().filter(post -> post.getId() == id).findFirst();
    }

    @Override
    public Post save(Post post) {
        if (post.getId() == 0L) {
            post.setId(sequence.getAndIncrement());
            store.getPosts().add(post);
        } else {
            findById(post.getId()).ifPresent(existing -> {
                existing.setTitle(post.getTitle());
                existing.setCategory(post.getCategory());
                existing.setContent(post.getContent());
                existing.setDate(post.getDate());
                existing.setLikes(post.getLikes());
                existing.setComments(post.getComments());
            });
        }
        store.savePosts();
        return post;
    }

    @Override
    public void delete(long id) {
        store.getPosts().removeIf(post -> post.getId() == id);
        store.savePosts();
    }

    @Override
    public void saveAll() {
        store.savePosts();
    }

    @Override
    public void incrementLikes(long id) {
        findById(id).ifPresent(post -> {
            post.setLikes(post.getLikes() + 1);
            store.savePosts();
        });
    }

    @Override
    public void addComment(long postId, Comment comment) {
        findById(postId).ifPresent(post -> {
            post.getComments().add(comment);
            store.savePosts();
        });
    }
}
