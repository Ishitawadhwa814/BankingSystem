package com.banking.system.service;

import com.banking.system.entity.Account;
import com.banking.system.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository=accountRepository;
    }

    public Account createAccount(String accountNumber, BigDecimal balance, String status){

        Account account = new Account(accountNumber, balance, status);
        return accountRepository.save(account);
    }
}
