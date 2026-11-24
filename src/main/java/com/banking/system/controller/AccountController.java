package com.banking.system.controller;


import com.banking.system.entity.Account;
import com.banking.system.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController{

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService=accountService;
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account){

        return accountService.createAccount(
                account.getAccountNumber(),
                account.getBalance(),
                account.getStatus()
        );
    }

    @PostMapping("/{accountId}/deposit")
    public Account deposit(
            @PathVariable Long accountId,
            @RequestBody BigDecimal amount){

        return accountService.deposit(accountId,amount);
    }

    @PostMapping("/{accountId}/withdrawal")
    public Account withdrawal(
            @PathVariable Long accountId,
            @RequestBody BigDecimal amount){

        return accountService.withdrawal(accountId,amount);
    }

}
