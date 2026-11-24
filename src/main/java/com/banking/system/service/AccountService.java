package com.banking.system.service;

import com.banking.system.dto.TransactionDTO;
import com.banking.system.entity.Account;
import com.banking.system.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.banking.system.entity.Transaction;
import com.banking.system.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository){

        this.accountRepository=accountRepository;
        this.transactionRepository=transactionRepository;
    }

    public Account createAccount(String accountNumber, BigDecimal balance, String status){

        Account account = new Account(accountNumber, balance, status);
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount){

        Account account = accountRepository.findById(accountId).orElseThrow(()->new RuntimeException("Account not found"));

        if(amount.compareTo(BigDecimal.ZERO)<=0){
            throw new RuntimeException("Deposit amount to be greater than zero");
        }

        BigDecimal updateBalance = account.getBalance().add(amount);

        account.setBalance(updateBalance);

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction("TI-" +UUID.randomUUID(),saved,"Deposit",amount,"Success",LocalDateTime.now());

        transactionRepository.save(transaction);
        return saved;
    }

    @Transactional
    public Account withdrawal(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("No account found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount to be greater than zero");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        BigDecimal updateBalance = account.getBalance().subtract(amount);
        account.setBalance(updateBalance);

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction("TI-" + UUID.randomUUID(), saved, "Withdrawal", amount, "Success", LocalDateTime.now());
        transactionRepository.save(transaction);
        return saved;
    }
    public List<TransactionDTO> getTransactionHistory(Long accountId){

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        List<TransactionDTO> history = new ArrayList<>();

        for(Transaction transaction:transactions) {
            TransactionDTO dto = new TransactionDTO(transaction.getTransactionId(), transaction.getAccount().getAccountNumber(), transaction.getType(), transaction.getAmount(), transaction.getStatus(), transaction.getTimestamp()
            );


            history.add(dto);
//        return transactionRepository.findByAccountId(accountId);
        }
        return history;
    }
}