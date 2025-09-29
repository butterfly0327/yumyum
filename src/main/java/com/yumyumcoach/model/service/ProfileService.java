package com.yumyumcoach.model.service;

import java.util.Optional;

import com.yumyumcoach.model.dto.Profile;

public interface ProfileService {
    Optional<Profile> findByUsername(String username);

    void save(Profile profile);

    void delete(String username);
}
