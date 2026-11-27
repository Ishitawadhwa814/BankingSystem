package com.banking.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="failed_transactions")
public class FailedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="transaction_id",nullable = true,unique = true)
    private Transaction transaction;

    @Column(name="request_transaction_id")
    private String requestTransactionId;

    @Column(nullable = false)
    private String error;

    @Column(nullable = false,length=200)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime time;

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getRequestTransactionId() {
        return requestTransactionId;
    }

    public void setRequestTransactionId(String requestTransactionId) {
        this.requestTransactionId = requestTransactionId;
    }

    public FailedTransaction(){

    }

    public FailedTransaction(Transaction transaction, String error, String reason, LocalDateTime time) {
        this.transaction = transaction;
        this.error = error;
        this.reason = reason;
        this.time = time;
    }
}
