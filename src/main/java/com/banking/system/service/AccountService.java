package com.banking.system.service;

import com.banking.system.dto.TransactionDTO;
import com.banking.system.entity.*;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.CustomerRepository;
import com.banking.system.repository.TransactionAuditRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import com.banking.system.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FailedTransactionService failedTransactionService;
    private final CustomerRepository customerRepository;
    private final TransactionAuditRepository transactionAuditRepository;


    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, FailedTransactionService failedTransactionService,CustomerRepository customerRepository, TransactionAuditRepository transactionAuditRepository){

        this.accountRepository=accountRepository;
        this.transactionRepository=transactionRepository;
        this.failedTransactionService=failedTransactionService;
        this.customerRepository=customerRepository;
        this.transactionAuditRepository = transactionAuditRepository;

    }

    public Account createAccount(String accountNumber, BigDecimal balance, String status,Long customerId){

        Customer customer  = customerRepository.findById(customerId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found"));

        Account account = new Account(accountNumber, balance, status);
        account.setCustomer(customer);
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount){

        long start = System.nanoTime();

        String transactionId = "TI-" + UUID.randomUUID();

        logger.info("Deposit request received | Transaction ID: {} | Account ID: {} | Amount: {} | Thread: {}",transactionId,accountId,amount,Thread.currentThread().getName());

        Account account = accountRepository.findByIdForUpdate(accountId).orElse(null);

        if(account==null){
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction=new Transaction(transactionId,null,null,"Deposit",amount==null?BigDecimal.ZERO:amount,"FAILED",now);
            transaction.setRequestedAccountId(accountId);
            transaction.setFailureReason("Account not found");

            FailedTransaction failedTransaction=new FailedTransaction(transaction,"ACCOUNT_NOT_FOUND","Account does not exist",now);

            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Deposit rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"ACCOUNT_NOT_FOUND",Thread.currentThread().getName());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
        }

        if(!"ACTIVE".equalsIgnoreCase(account.getStatus())){
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction=new Transaction(transactionId,account,null,"Deposit",amount==null?BigDecimal.ZERO:amount,"FAILED",now);
            transaction.setFailureReason("Account is inactive");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"ACCOUNT_INACTIVE","Cannot deposit into an inactive account", now);

            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Deposit rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"ACCOUNT_INACTIVE",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account is inactive");
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO)<=0){
            LocalDateTime now=LocalDateTime.now();

            Transaction transaction = new Transaction(transactionId,account,null,"Deposit",amount==null?BigDecimal.ZERO:amount,"FAILED",now);
            transaction.setFailureReason("Invalid deposit amount");

            FailedTransaction failedTransaction = new FailedTransaction(
                    transaction,
                    "INVALID_AMOUNT", "Deposit amount must be greater than zero",
                    now
            );
            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Deposit rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"INVALID_AMOUNT",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Deposit amount must be greater than zero");
        }


        BigDecimal updateBalance = account.getBalance().add(amount);

        account.setBalance(updateBalance);

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction(transactionId,saved,null,"Deposit",amount,"SUCCESS",LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        long duration = (System.nanoTime()-start)/1_000_000;

        TransactionAudit audit = new TransactionAudit(savedTransaction,LocalDateTime.now(),Thread.currentThread().getName(),duration);
        transactionAuditRepository.save(audit);

        logger.info("Deposit completed | Transaction ID: {} | Account ID:{} | Amount: {} | Result: SUCCESS | Duration: {} ms | Thread: {}",transactionId,accountId,amount,duration,Thread.currentThread().getName());
        return saved;
    }


    @Transactional
    public Account withdrawal(Long accountId, BigDecimal amount) {

        long start = System.nanoTime();

        String transactionId = "TI-" + UUID.randomUUID();

        logger.info("Withdrawal request received | Transaction ID: {} | Account ID:{} | Amount: {} | Thread: {}",transactionId,accountId,amount,Thread.currentThread().getName());

        Account account = accountRepository.findByIdForUpdate(accountId).orElse(null);

        if (account == null) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(transactionId, null, null, "Withdrawal", amount==null?BigDecimal.ZERO:amount, "FAILED", now
            );

            transaction.setRequestedAccountId(accountId);
            transaction.setFailureReason("Account not found");

            FailedTransaction failedTransaction = new FailedTransaction(transaction, "ACCOUNT_NOT_FOUND", "Account does not exist", now
            );

            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Withdrawal rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"ACCOUNT_NOT_FOUND",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(transactionId, account, null, "Withdrawal", amount==null?BigDecimal.ZERO:amount, "FAILED", now
            );

            transaction.setFailureReason("Account is inactive");

            FailedTransaction failedTransaction = new FailedTransaction(transaction, "ACCOUNT_INACTIVE", "Cannot withdraw from an inactive account", now
            );

            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Withdrawal rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"ACCOUNT_INACTIVE",Thread.currentThread().getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is inactive");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(transactionId, account, null, "Withdrawal", amount == null ? BigDecimal.ZERO : amount, "FAILED", now
            );

            transaction.setFailureReason("Invalid withdrawal amount");

            FailedTransaction failedTransaction = new FailedTransaction(transaction, "INVALID_AMOUNT", "Withdrawal amount must be greater than zero", now);

            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Withdrawal rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"INVALID_AMOUNT",Thread.currentThread().getName());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Withdrawal amount must be greater than zero"
            );
        }

        if (account.getBalance().compareTo(amount) < 0) {
            LocalDateTime now = LocalDateTime.now();

            Transaction transaction = new Transaction(transactionId, account, null, "Withdrawal", amount, "FAILED", now
            );

            transaction.setFailureReason("Insufficient balance");
            FailedTransaction failedTransaction = new FailedTransaction(transaction, "INSUFFICIENT_BALANCE", "Insufficient balance in account", now
            );
            failedTransactionService.saveFailed(failedTransaction);
            logger.warn("Withdrawal rejected | Transaction ID: {} | Account ID: {} | Reason: {} | Thread: {}",transactionId,accountId,"INSUFFICIENT_BALANCE",Thread.currentThread().getName());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"
            );
        }

        BigDecimal updateBalance = account.getBalance().subtract(amount);
        account.setBalance(updateBalance);

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction(transactionId, saved, null, "Withdrawal", amount, "SUCCESS", LocalDateTime.now()
        );
        Transaction savedTransaction = transactionRepository.save(transaction);
        long duration = (System.nanoTime()-start)/1_000_000;

        TransactionAudit audit = new TransactionAudit(savedTransaction,LocalDateTime.now(),Thread.currentThread().getName(),duration);
        transactionAuditRepository.save(audit);

        logger.info("Withdrawal completed | Transaction ID: {} | Account ID: {} | Amount: {} | Result: SUCCESS | Thread: {}",transactionId,accountId,amount,Thread.currentThread().getName());
        return saved;
    }

    public List<TransactionDTO> getTransactionHistory(Long accountId){

        List<Transaction> transactions = transactionRepository.findByAccount_IdOrReceiverAccount_Id(accountId,accountId);
        List<TransactionDTO> history = new ArrayList<>();

        for(Transaction transaction:transactions) {

            String accountNumber;
            String debitOrCredit;

            if (transaction.getAccount() != null && transaction.getAccount().getId().equals(accountId)) {

                accountNumber = transaction.getAccount().getAccountNumber();
            }else if(transaction.getReceiverAccount()!=null){
                accountNumber = transaction.getReceiverAccount().getAccountNumber();
            }else{
                continue;
            }
            if("Deposit".equalsIgnoreCase(transaction.getType())){
                debitOrCredit="CREDIT";
            }else if("Withdrawal".equalsIgnoreCase(transaction.getType())){
                debitOrCredit="DEBIT";
            } else if ("Transfer".equalsIgnoreCase((transaction.getType()))) {
                if(transaction.getAccount()!=null && transaction.getAccount().getId().equals(accountId)){
                    debitOrCredit="DEBIT";
                }else{
                    debitOrCredit="CREDIT";
                }
            }else{
                debitOrCredit="UNKNOWN";
            }
            TransactionDTO dto = new TransactionDTO(transaction.getTransactionId(), accountNumber, transaction.getType(), transaction.getAmount(), transaction.getStatus(), transaction.getTimestamp(),debitOrCredit
            );
            history.add(dto);
        }
        return history;
    }

    public List<TransactionDTO> getTransferHistory(Long accountId){
        List<Transaction> transactions = transactionRepository.findByAccount_IdAndTypeIgnoreCaseOrReceiverAccount_IdAndTypeIgnoreCase(accountId,"TRANSFER",accountId,"TRANSFER");

        List<TransactionDTO> history = new ArrayList<>();

        for(Transaction transaction:transactions){
             String accountNumber;
             String debitOrCredit;

             if(transaction.getAccount()!=null && transaction.getAccount().getId().equals(accountId)){
                 accountNumber=transaction.getAccount().getAccountNumber();
                 debitOrCredit="DEBIT";
             }else if(transaction.getReceiverAccount() != null && transaction.getReceiverAccount().getId().equals(accountId)) {
                accountNumber = transaction.getReceiverAccount().getAccountNumber();
                debitOrCredit = "CREDIT";
            }else{
                 continue;
             }

             TransactionDTO dto = new TransactionDTO(transaction.getTransactionId(),accountNumber,transaction.getType(),transaction.getAmount(),transaction.getStatus(),transaction.getTimestamp(),debitOrCredit);
             history.add(dto);
        }
        return history;
    }
    @Transactional
    public void transfer(String transactionId, Long senderId, Long receiverId, BigDecimal amount){

        if(transactionId==null || transactionId.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Transaction ID cannot be empty");
        }

        if(senderId==null || receiverId==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Sender ID and receiver ID cannot be null");
        }

        logger.info("Transfer request received | Transaction ID: {} | Sender ID: {} | Receiver ID: {} | Thread: {}",transactionId,senderId,receiverId,Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        if(transactionRepository.existsByTransactionId(transactionId)){
            LocalDateTime now = LocalDateTime.now();
            FailedTransaction failed = new FailedTransaction();
            failed.setRequestTransactionId(transactionId);
            failed.setError("DUPLICATE_TRANSACTION_ID");
            failed.setReason("Duplicate transaction ID found");
            failed.setTime(now);
            failedTransactionService.saveFailed(failed);

            logger.warn("Transfer rejected | Transaction ID: {} | Reason: {} | Thread: {}", transactionId,"DUPLICATE_TRANSACTION_ID",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.CONFLICT,"Duplicate Transaction ID found");

        }

        if(senderId.equals(receiverId)){
            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction(transactionId,null,null,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Sender and receiver accounts are same");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"SAME_ACCOUNT_NUMBER","Cannot transfer money to same account",now);
            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Transferrejected | Transaction ID: {} | Sender ID: {} | Receiver ID: {} | Reason: {} | Thread: {}",transactionId,senderId,receiverId,"SAME_ACCOUNT_TRANSFER",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot transfer to same account");
        }
        Account sender;
        Account receiver;

        if(senderId<receiverId){
            sender = accountRepository.findByIdForUpdate(senderId).orElse(null);
            receiver = accountRepository.findByIdForUpdate(receiverId).orElse(null);
        }else{
            receiver = accountRepository.findByIdForUpdate(receiverId).orElse(null);
            sender = accountRepository.findByIdForUpdate(senderId).orElse(null);
        }
        if(sender==null || receiver==null){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction(transactionId,sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setRequestedAccountId(sender==null ? senderId:receiverId);

            transaction.setFailureReason("Sender or receiver account not found");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"ACCOUNT_NOT_FOUND","Sender or receiver account does not exist",now);
            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Transfer rejected | Transaction ID: {} | Sender ID: {} | Receiver ID: {} | Reason:{} | Thread: {}",transactionId,senderId,receiverId,"ACCOUNT_NOT_FOUND",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Sender or receiver account not found");


        }
        if(!"ACTIVE".equalsIgnoreCase(sender.getStatus()) || !"ACTIVE".equalsIgnoreCase(receiver.getStatus())){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction(transactionId,sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Sender or receiver account is inactive");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"ACCOUNT_INACTIVE","Both accounts must be active for a transfer",now);
            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Transfer rejected | Transaction ID: {} | Sender ID: {} | Receiver ID: {} | Reason:{} | Thread: {}",transactionId,senderId,receiverId,"ACCOUNT_INACTIVE",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Both accounts must be active");
        }
        if(amount==null || amount.compareTo(BigDecimal.ZERO)<=0){
            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction(transactionId,sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Invalid transfer amount");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"INVALID_AMOUNT","Transfer amount must be greater than zero",now);
            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Transfer rejected | Transaction ID: {} | Sender ID: {} | Receiver ID: {} | Reason:{} | Thread: {}",transactionId,senderId,receiverId,"INVALID_AMOUNT",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Transfer amount must be greater than zero");
        }
        if(sender.getBalance().compareTo(amount)<0){

            LocalDateTime now = LocalDateTime.now();
            Transaction transaction = new Transaction(transactionId,sender,receiver,"TRANSFER",amount==null ? BigDecimal.ZERO:amount,"FAILED",now);

            transaction.setFailureReason("Insufficient balance");

            FailedTransaction failedTransaction = new FailedTransaction(transaction,"INSUFFICIENT_BALANCE","Insufficient balance in sender account",now);
            failedTransactionService.saveFailed(failedTransaction);

            logger.warn("Transfer rejected | Transaction ID: {} | Sender ID: {} | Receiver ID: {} | Reason:{} | Thread: {}",transactionId,senderId,receiverId,"INSUFFICIENT_BALANCE",Thread.currentThread().getName());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Insufficient balance");
        }
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction(transactionId,sender,receiver,"TRANSFER",amount,"SUCCESS",LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        long duration = System.currentTimeMillis()-start;

        TransactionAudit audit = new TransactionAudit(savedTransaction,LocalDateTime.now(),Thread.currentThread().getName(),duration);
        transactionAuditRepository.save(audit);


        logger.info("Transfer completed | Transaction ID: {} | Amount: {} | Result: SUCCESS | Duration: {} ms | Thread: {}",transactionId,amount,duration,Thread.currentThread().getName());

    }
}
