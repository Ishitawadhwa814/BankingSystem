package com.banking.system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    private String transactionId;
    private String accountNumber;
    private String type;
    private BigDecimal amount;
    private String status;
    private LocalDateTime time;

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTime() {
        return time;
    }

    private String debitOrCredit;

    public String getDebitOrCredit() {
        return debitOrCredit;
    }
    public TransactionDTO(String transactionId, String accountNumber, String type, BigDecimal amount, String status, LocalDateTime time, String debitOrCredit) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.time = time;
        this.debitOrCredit=debitOrCredit;
    }
}
