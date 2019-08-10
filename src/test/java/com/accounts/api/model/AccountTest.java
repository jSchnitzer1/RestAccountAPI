package com.accounts.api.model;

import java.util.List;

public class AccountTest {
    private long accountId;
    private double balance;
    private boolean defaultAccount;
    private CustomerTest customer;
    private List<TransactionTest> transactions;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public CustomerTest getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerTest customer) {
        this.customer = customer;
    }

    public List<TransactionTest> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionTest> transactions) {
        this.transactions = transactions;
    }

    public AccountTest() {
    }

    public AccountTest(long accountId, double balance, boolean defaultAccount, CustomerTest customer, List<TransactionTest> transactions) {
        this.accountId = accountId;
        this.balance = balance;
        this.defaultAccount = defaultAccount;
        this.customer = customer;
        this.transactions = transactions;
    }
}
