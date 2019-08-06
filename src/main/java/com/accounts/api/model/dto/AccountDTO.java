package com.accounts.api.model.dto;

import com.accounts.api.model.entity.Account;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This DTO is used to prevent exposing the Account entity to the world.
 */
@XmlRootElement
public class AccountDTO implements Serializable {
    private long accountId;
    private double balance;
    private boolean defaultAccount;
    @XmlElement(name = "transactions") private List<TransactionDTO> transactionDTOs;

    public AccountDTO() {
    }

    public AccountDTO(long accountId, double balance, boolean defaultAccount, List<TransactionDTO> transactionDTOs) {
        this.accountId = accountId;
        this.balance = balance;
        this.defaultAccount = defaultAccount;
        this.transactionDTOs = transactionDTOs != null ? transactionDTOs : new ArrayList<>();
    }

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

    public List<TransactionDTO> getTransactionDTOs() {
        return transactionDTOs == null ? new ArrayList<>() : transactionDTOs;
    }

    public void setTransactionDTOs(List<TransactionDTO> transactionDTOs) {
        if(transactionDTOs == null)
            this.transactionDTOs = new ArrayList<>();
        else
            this.transactionDTOs = transactionDTOs;
    }

    public static AccountDTO cloneFromEntity(Account account) {
        return new AccountDTO(account.getAccountId(), account.getBalance(), account.isDefaultAccount(), account.getTransactionDTOs());
    }

    public static List<AccountDTO> cloneFromEntity(List<Account> accounts) {
        if(accounts == null || accounts.size() == 0) return null;
        List<AccountDTO> accountDTOs = new ArrayList<>();
        accounts.forEach(a -> accountDTOs.add(cloneFromEntity(a)));
        return accountDTOs;
    }
}