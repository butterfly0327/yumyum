package com.yumyumcoach.model.dao;

import java.util.Optional;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.Profile;

public class FileProfileDao implements ProfileDao {
    private static final FileProfileDao INSTANCE = new FileProfileDao();
    private final DataStore store = DataStore.getInstance();

    public static FileProfileDao getInstance() {
        return INSTANCE;
    }

    private FileProfileDao() {
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return store.findProfile(username);
    }

    @Override
    public void save(Profile profile) {
        store.getProfiles().put(profile.getUsername(), profile);
        store.saveProfiles();
    }

    @Override
    public void delete(String username) {
        store.getProfiles().remove(username);
        store.saveProfiles();
    }
}
