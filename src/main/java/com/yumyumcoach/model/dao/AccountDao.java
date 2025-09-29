package com.yumyumcoach.model.dao;

import java.util.List;
import java.util.Optional;

import com.yumyumcoach.model.dto.Account;

public interface AccountDao {
    Optional<Account> findByUsername(String username);

    void save(Account account);

    List<Account> findAll();

    void delete(String username);
}
