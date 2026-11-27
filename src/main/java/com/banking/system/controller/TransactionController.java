package com.banking.system.controller;

import com.banking.system.dto.TransactionDTO;
import com.banking.system.entity.Account;
import com.banking.system.dto.TransferDTO;
import com.banking.system.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AccountService accountService;

    public TransactionController(AccountService accountService){
        this.accountService=accountService;
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

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDTO request){
        accountService.transfer(request.getTransactionId(),request.getSenderId(),request.getReceiverId(),request.getAmount());

        return "Transfer successful";
    }

    @GetMapping("/account/{accountId}/transfers")
    public List<TransactionDTO> getTransferHistory(@PathVariable Long accountId){
        return accountService.getTransferHistory(accountId);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionDTO> getTransactionHistory(@PathVariable Long accountId){
        return accountService.getTransactionHistory(accountId);
    }
}
