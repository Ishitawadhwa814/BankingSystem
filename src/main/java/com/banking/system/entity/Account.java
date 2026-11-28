package com.banking.system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name="accounts")

public class Account{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String accountNumber;

    @Column(nullable = false,precision = 19,scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name="customer_id")
    private Customer customer;

    public Account(){

    }

    public Account(String accountNumber,BigDecimal balance, String status){
        this.accountNumber=accountNumber;
        this.balance=balance;
        this.status=status;
    }
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomer(){
        return customer;
    }

    public void setCustomer(Customer customer){
        this.customer=customer;
    }
}
