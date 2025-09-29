package com.yumyumcoach.model.dao;

import java.util.Optional;

import com.yumyumcoach.model.dto.Profile;

public interface ProfileDao {
    Optional<Profile> findByUsername(String username);

    void save(Profile profile);

    void delete(String username);
}
