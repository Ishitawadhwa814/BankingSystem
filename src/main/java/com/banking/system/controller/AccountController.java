package com.banking.system.controller;

import com.banking.system.entity.Account;
import com.banking.system.service.AccountService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
public class AccountController{

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService=accountService;
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account){

        return accountService.createAccount(account.getAccountNumber(), account.getBalance(), account.getStatus());
    }


}
