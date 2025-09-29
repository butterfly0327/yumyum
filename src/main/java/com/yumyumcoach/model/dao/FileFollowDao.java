package com.yumyumcoach.model.dao;

import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.FollowInfo;

public class FileFollowDao implements FollowDao {
    private static final FileFollowDao INSTANCE = new FileFollowDao();
    private final DataStore store = DataStore.getInstance();

    public static FileFollowDao getInstance() {
        return INSTANCE;
    }

    private FileFollowDao() {
    }

    @Override
    public Map<String, FollowInfo> findAll() {
        return store.getFollowMap();
    }

    @Override
    public Optional<FollowInfo> findByUsername(String username) {
        return Optional.ofNullable(store.getFollowMap().get(username));
    }

    @Override
    public void save(String username, FollowInfo info) {
        store.getFollowMap().put(username, info);
        store.saveFollows();
    }

    @Override
    public void saveAll() {
        store.saveFollows();
    }
}
