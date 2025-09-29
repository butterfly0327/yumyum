package com.yumyumcoach.model.service.impl;

import java.util.Optional;

import com.yumyumcoach.model.dao.JdbcProfileDao;
import com.yumyumcoach.model.dao.ProfileDao;
import com.yumyumcoach.model.dto.Profile;
import com.yumyumcoach.model.service.ProfileService;

public class ProfileServiceImpl implements ProfileService {
    private static final ProfileService INSTANCE = new ProfileServiceImpl();
    private final ProfileDao profileDao = JdbcProfileDao.getInstance();

    public static ProfileService getInstance() {
        return INSTANCE;
    }

    private ProfileServiceImpl() {
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return profileDao.findByUsername(username);
    }

    @Override
    public void save(Profile profile) {
        profileDao.save(profile);
    }

    @Override
    public void delete(String username) {
        profileDao.delete(username);
    }
}
