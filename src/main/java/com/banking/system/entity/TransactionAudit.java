package com.banking.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="transaction_audit")
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="transaction_id",nullable=false,unique=true)
    private Transaction transaction;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String thread;

    @Column(nullable = false)
    private Long duration;

    public TransactionAudit(){

    }

    public TransactionAudit(Transaction transaction, LocalDateTime timestamp, String thread, Long duration) {
        this.transaction = transaction;
        this.timestamp = timestamp;
        this.thread = thread;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
