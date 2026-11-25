package com.banking.system.service;

import com.banking.system.dto.TransactionDTO;
import com.banking.system.entity.Account;
import com.banking.system.entity.FailedTransaction;
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
    private final FailedTransactionService failedTransactionService;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository,FailedTransactionService failedTransactionService){

        this.accountRepository=accountRepository;
        this.transactionRepository=transactionRepository;
        this.failedTransactionService=failedTransactionService;
    }

    public Account createAccount(String accountNumber, BigDecimal balance, String status){

        Account account = new Account(accountNumber, balance, status);
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount){

        Account account = accountRepository.findById(accountId).orElse(null);

        if(account==null){
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction=new Transaction("TI-" + UUID.randomUUID(),null,null,"Deposit",amount==null?BigDecimal.ZERO:amount,"FAILED",now);
            transaction.setRequestedAccountId(accountId);
            transaction.setFailureReason("Account not found");

            FailedTransaction failedTransaction=new FailedTransaction(transaction,"Account_Not_Found","Account does not exist",now);

            failedTransactionService.saveFailed(failedTransaction);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
        }

        if(!"ACTIVE".equalsIgnoreCase(account.getStatus())){
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction=new Transaction("TI-" +UUID.randomUUID(),account,null,"Deposit",amount==null?BigDecimal.ZERO:amount,"FAILED",now);
            transaction.setFailureReason("Account is inactive");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"Account_inactive","Cannot deposit into an inactive account", now);

            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account is inactive");
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO)<=0){
            LocalDateTime now=LocalDateTime.now();

            Transaction transaction = new Transaction("TI-"+UUID.randomUUID(),account,null,"Deposit",amount==null?BigDecimal.ZERO:amount,"FAILED",now);
            transaction.setFailureReason("Invalid deposit amount");

            FailedTransaction failedTransaction = new FailedTransaction(
                    transaction,
                    "INVALID_AMOUNT", "Deposit amount must be greater than zero",
                    now
            );
            failedTransactionService.saveFailed(failedTransaction);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Deposit amount must be greater than zero");
        }


        BigDecimal updateBalance = account.getBalance().add(amount);

        account.setBalance(updateBalance);

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction("TI-" +UUID.randomUUID(),saved,null,"Deposit",amount,"SUCCESS",LocalDateTime.now());

        transactionRepository.save(transaction);
        return saved;
    }


    @Transactional
    public Account withdrawal(Long accountId, BigDecimal amount) {

        Account account = accountRepository.findById(accountId).orElse(null);

        if (account == null) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction("TI-" + UUID.randomUUID(), null, null, "Withdrawal", amount==null?BigDecimal.ZERO:amount, "FAILED", now
            );

            transaction.setRequestedAccountId(accountId);
            transaction.setFailureReason("Account not found");

            FailedTransaction failedTransaction = new FailedTransaction(
                    transaction,
                    "ACCOUNT_NOT_FOUND",
                    "Account does not exist",
                    now
            );

            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(
                    "TI-" + UUID.randomUUID(),
                    account,
                    null,
                    "Withdrawal",
                    amount==null?BigDecimal.ZERO:amount,
                    "FAILED",
                    now
            );

            transaction.setFailureReason("Account is inactive");

            FailedTransaction failedTransaction = new FailedTransaction(
                    transaction,
                    "ACCOUNT_INACTIVE",
                    "Cannot withdraw from an inactive account",
                    now
            );

            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account is inactive"
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(
                    "TI-" + UUID.randomUUID(),
                    account,
                    null,
                    "Withdrawal",
                    amount == null ? BigDecimal.ZERO : amount,
                    "FAILED",
                    now
            );

            transaction.setFailureReason("Invalid withdrawal amount");

            FailedTransaction failedTransaction = new FailedTransaction(
                    transaction,
                    "INVALID_AMOUNT",
                    "Withdrawal amount must be greater than zero",
                    now
            );

            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Withdrawal amount must be greater than zero"
            );
        }

        if (account.getBalance().compareTo(amount) < 0) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(
                    "TI-" + UUID.randomUUID(),
                    account,
                    null,
                    "Withdrawal",
                    amount,
                    "FAILED",
                    now
            );

            transaction.setFailureReason("Insufficient balance");
            FailedTransaction failedTransaction = new FailedTransaction(
                    transaction,
                    "INSUFFICIENT_BALANCE",
                    "Insufficient balance in account",
                    now
            );
            failedTransactionService.saveFailed(failedTransaction);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient balance"
            );
        }

        BigDecimal updateBalance = account.getBalance().subtract(amount);
        account.setBalance(updateBalance);

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction(
                "TI-" + UUID.randomUUID(),
                saved,
                null,
                "Withdrawal",
                amount,
                "SUCCESS",
                LocalDateTime.now()
        );
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
        }
        return history;
    }

    @Transactional
    public void transfer(Long senderId, Long receiverId, BigDecimal amount){

        Account sender=accountRepository.findById(senderId).orElse(null);
        Account receiver = accountRepository.findById(receiverId).orElse(null);

        if(sender==null || receiver==null){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction("TI-" + UUID.randomUUID(),sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setRequestedAccountId(sender==null ? senderId:receiverId);

            transaction.setFailureReason("Sender or receiver account not found");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"ACCOUNT_NOT_FOUND","Sender or receiver account does not exist",now);
            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Sender or receiver account not found");


        }
        if(!"ACTIVE".equalsIgnoreCase(sender.getStatus()) || !"ACTIVE".equalsIgnoreCase(receiver.getStatus())){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction("TI-" + UUID.randomUUID(),sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Sender or receiver account is inactive");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"ACCOUNT_INACTIVE","Both accounts must be active for a transfer",now);
            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Both accounts must be active");
        }
        if(amount==null || amount.compareTo(BigDecimal.ZERO)<=0){
            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction("TI-" + UUID.randomUUID(),sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Invalid transfer amount");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"INVALID_AMOUNT","Transfer amount must be greater than zero",now);
            failedTransactionService.saveFailed(failedTransaction);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Transfer amount must be greater than zero");
        }
        if(senderId.equals(receiverId)){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction("TI-" + UUID.randomUUID(),sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Sender and receiver accounts are same");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"SAME_ACCOUNT_TRANSFER","Cannot transfer money to same account",now);
            failedTransactionService.saveFailed(failedTransaction);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot transfer to same account");
        }
        if(sender.getBalance().compareTo(amount)<0){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction("TI-" + UUID.randomUUID(),sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Insufficient balance");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"INSUFFICIENT_BALANCE","Insufficient balance in sender account",now);
            failedTransactionService.saveFailed(failedTransaction);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient balance");
        }
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction("TI-" + UUID.randomUUID(),sender,receiver,"TRANSFER",amount,"SUCCESS",LocalDateTime.now());
        transactionRepository.save(transaction);
    }
}