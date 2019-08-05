package com.accounts.api.model.dto;

import com.accounts.api.model.entity.Account;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This DTO is used to prevent exposing the Account entity to the world.
 */
@XmlRootElement
public class AccountDTO {
    private long accountId;
    private double balance;
    private boolean defaultAccount;

    public AccountDTO() {
    }

    public AccountDTO(long accountId, double balance, boolean defaultAccount) {
        this.accountId = accountId;
        this.balance = balance;
        this.defaultAccount = defaultAccount;
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

    public static AccountDTO cloneFromEntity(Account account) {
        return new AccountDTO(account.getAccountId(), account.getBalance(), account.isDefaultAccount());
    }

    public static List<AccountDTO> cloneFromEntity(List<Account> accounts) {
        if(accounts == null || accounts.size() == 0) return null;
        List<AccountDTO> accountDTOs = new ArrayList<>();
        accounts.forEach(a -> accountDTOs.add(cloneFromEntity(a)));
        return accountDTOs;
    }
}