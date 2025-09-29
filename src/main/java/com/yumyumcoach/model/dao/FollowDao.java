package com.yumyumcoach.model.dao;

import java.util.Map;
import java.util.Optional;

import com.yumyumcoach.model.dto.FollowInfo;

public interface FollowDao {
    Map<String, FollowInfo> findAll();

    Optional<FollowInfo> findByUsername(String username);

    void save(String username, FollowInfo info);

    void saveAll();
}
