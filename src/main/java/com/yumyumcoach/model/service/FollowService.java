package com.yumyumcoach.model.service;

import java.util.Map;

import com.yumyumcoach.model.dto.FollowInfo;

public interface FollowService {
    Map<String, FollowInfo> findAll();

    FollowInfo toggleFollow(String follower, String followee, boolean follow);
}
