package com.accounts.api.model.entity;

import com.accounts.api.model.dto.TransactionDTO;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Account")
public class Account implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountId")
    private int accountId;

    @Column
    private double balance;

    @Column (name = "default")
    private boolean defaultAccount;

    @ManyToOne(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL)
    @JoinColumn(name = "Customer", referencedColumnName="customerId")
    private Customer customer;

    //non entity fields
    @Transient
    private List<TransactionDTO> transactionDTOs;

    public Account() {
    }

    public Account(double balance, boolean defaultAccount, Customer customer, List<TransactionDTO> transactionDTOs) {
        this.balance = balance;
        this.defaultAccount = defaultAccount;
        this.customer = customer;
        this.transactionDTOs = transactionDTOs;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public List<TransactionDTO> getTransactionDTOs() {
        return transactionDTOs;
    }

    public void setTransactionDTOs(List<TransactionDTO> transactionDTOs) {
        if(transactionDTOs == null)
            this.transactionDTOs= new ArrayList<>();
        else
            this.transactionDTOs = transactionDTOs;
    }
}
