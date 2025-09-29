package com.yumyumcoach.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yumyumcoach.model.dao.FollowDao;
import com.yumyumcoach.model.dao.JdbcFollowDao;
import com.yumyumcoach.model.dto.FollowInfo;
import com.yumyumcoach.model.service.FollowService;

public class FollowServiceImpl implements FollowService {
    private static final FollowService INSTANCE = new FollowServiceImpl();
    private final FollowDao followDao = JdbcFollowDao.getInstance();

    public static FollowService getInstance() {
        return INSTANCE;
    }

    private FollowServiceImpl() {
    }

    @Override
    public Map<String, FollowInfo> findAll() {
        return followDao.findAll();
    }

    @Override
    public FollowInfo toggleFollow(String follower, String followee, boolean follow) {
        FollowInfo followerInfo = followDao.findByUsername(follower).orElseGet(FollowInfo::new);
        FollowInfo followeeInfo = followDao.findByUsername(followee).orElseGet(FollowInfo::new);

        List<String> following = new ArrayList<>(followerInfo.getFollowing());
        List<String> followers = new ArrayList<>(followeeInfo.getFollowers());

        if (follow) {
            if (!following.contains(followee)) {
                following.add(followee);
            }
            if (!followers.contains(follower)) {
                followers.add(follower);
            }
        } else {
            following.remove(followee);
            followers.remove(follower);
        }

        followerInfo.setFollowing(following);
        followeeInfo.setFollowers(followers);

        followDao.save(follower, followerInfo);
        followDao.save(followee, followeeInfo);

        return followDao.findByUsername(follower).orElse(followerInfo);
    }
}
