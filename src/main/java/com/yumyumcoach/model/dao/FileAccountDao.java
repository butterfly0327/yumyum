package com.yumyumcoach.model.dao;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.config.DataStore;
import com.yumyumcoach.model.dto.Account;

public class FileAccountDao implements AccountDao {
    private static final FileAccountDao INSTANCE = new FileAccountDao();
    private final DataStore store = DataStore.getInstance();

    public static FileAccountDao getInstance() {
        return INSTANCE;
    }

    private FileAccountDao() {
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return store.findAccount(username);
    }

    @Override
    public void save(Account account) {
        synchronized (store.getAccounts()) {
            store.findAccount(account.getUsername()).ifPresentOrElse(existing -> {
                existing.setPassword(account.getPassword());
                existing.setEmail(account.getEmail());
            }, () -> store.getAccounts().add(account));
        }
        store.saveAccounts();
    }

    @Override
    public List<Account> findAll() {
        return store.getAccounts();
    }

    @Override
    public void delete(String username) {
        synchronized (store.getAccounts()) {
            store.getAccounts().removeIf(acc -> username.equals(acc.getUsername()));
        }
        store.saveAccounts();
    }
}
