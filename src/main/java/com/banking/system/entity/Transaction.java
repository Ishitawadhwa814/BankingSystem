package com.banking.system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="transactions")
public class Transaction {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @Column(nullable = false,unique = true)
    private String transactionId;

   @ManyToOne
    @JoinColumn(name="account_id", nullable = false)
    private Account account;

   @Column(nullable = false)
    private String type;

   @Column(nullable = false,precision = 19,scale = 2)
    private BigDecimal amount;

   @Column(nullable = false)
    private String status;

   @Column(nullable = false)
    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Transaction(){
   }

   public Transaction(String transactionId, Account account, String type, BigDecimal amount, String status, LocalDateTime timestamp){
       this.transactionId=transactionId;
       this.account=account;
       this.type=type;
       this.amount=amount;
       this.status=status;
       this.timestamp=timestamp;
   }
}