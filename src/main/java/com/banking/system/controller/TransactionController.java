package com.banking.system.controller;

import com.banking.system.dto.TransactionDTO;
import com.banking.system.entity.Transaction;
import com.banking.system.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AccountService accountService;

    public TransactionController(AccountService accountService){
        this.accountService=accountService;
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionDTO> getTransactionHistory(@PathVariable Long accountId){
        return accountService.getTransactionHistory(accountId);
    }
}
