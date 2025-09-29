package com.yumyumcoach.model.service;

import java.util.Optional;

import com.yumyumcoach.model.dto.Account;

public interface AccountService {
    Optional<Account> login(String username, String password);

    boolean register(Account account);

    void update(Account account);

    void delete(String username);
}
