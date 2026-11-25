package com.banking.system.service;

import com.banking.system.entity.FailedTransaction;
import com.banking.system.entity.Transaction;
import com.banking.system.repository.FailedTransactionRepository;
import com.banking.system.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailedTransactionService {

    private final FailedTransactionRepository failedTransactionRepository;
    private final TransactionRepository transactionRepository;

    public FailedTransactionService(FailedTransactionRepository failedTransactionRepository, TransactionRepository transactionRepository){
        this.failedTransactionRepository=failedTransactionRepository;
        this.transactionRepository=transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailed(FailedTransaction failedTransaction){
        Transaction saved = transactionRepository.save(failedTransaction.getTransaction());
        failedTransaction.setTransaction(saved);
        failedTransactionRepository.save(failedTransaction);
    }
}

