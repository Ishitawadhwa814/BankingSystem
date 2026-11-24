package com.banking.system.service;

import com.banking.system.entity.Account;
import com.banking.system.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public Account deposit(Long accountId, BigDecimal amount){

        Account account = accountRepository.findById(accountId).orElseThrow(()->new RuntimeException("Account not found"));

        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new RuntimeException("Deposit amount to be greater than zero");
        }

        BigDecimal updateBalance = account.getBalance().add(amount);

        account.setBalance(updateBalance);

        return accountRepository.save(account);
    }

    public Account withdrawal(Long accountId, BigDecimal amount){
        Account account = accountRepository.findById(accountId).orElseThrow(()->new RuntimeException("No account found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount to be greater than zero");
        }

        if(account.getBalance().compareTo(amount)<0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient balance");
        }

        BigDecimal updateBalance = account.getBalance().subtract(amount);
        account.setBalance(updateBalance);

        return accountRepository.save(account);



    };

}
