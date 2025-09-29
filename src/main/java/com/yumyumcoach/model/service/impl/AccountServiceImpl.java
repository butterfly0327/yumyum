package com.yumyumcoach.model.service.impl;

import java.util.Optional;

import com.yumyumcoach.model.dao.AccountDao;
import com.yumyumcoach.model.dao.JdbcAccountDao;
import com.yumyumcoach.model.dto.Account;
import com.yumyumcoach.model.service.AccountService;

public class AccountServiceImpl implements AccountService {
    private static final AccountService INSTANCE = new AccountServiceImpl();
    private final AccountDao accountDao = JdbcAccountDao.getInstance();

    public static AccountService getInstance() {
        return INSTANCE;
    }

    private AccountServiceImpl() {
    }

    @Override
    public Optional<Account> login(String username, String password) {
        return accountDao.findByUsername(username)
                .filter(account -> account.getPassword().equals(password));
    }

    @Override
    public boolean register(Account account) {
        Optional<Account> existing = accountDao.findByUsername(account.getUsername());
        if (existing.isPresent()) {
            return false;
        }
        accountDao.save(account);
        return true;
    }

    @Override
    public void update(Account account) {
        accountDao.save(account);
    }

    @Override
    public void delete(String username) {
        accountDao.delete(username);
    }
}
