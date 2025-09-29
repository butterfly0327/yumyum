package com.yumyumcoach.model.dto;

import java.util.ArrayList;
import java.util.List;

public class FollowInfo {
    private List<String> following = new ArrayList<>();
    private List<String> followers = new ArrayList<>();

    public FollowInfo() {
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
}
