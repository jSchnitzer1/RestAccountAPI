package com.accounts.api.model.dto;

import com.accounts.api.model.entity.Account;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * This DTO is used to prevent exposing the Transaction entity to the world.
 */
@XmlRootElement(name = "transactions")
@XmlAccessorType (XmlAccessType.FIELD)
public class TransactionDTO implements Serializable {
    private long transactionId;
    private double transactionAmount;
    private String transactionUUID;
    private long accountId;

    public TransactionDTO() {
    }

    public TransactionDTO(long transactionId, double transactionAmount, String transactionUUID, long accountId) {
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.transactionUUID = transactionUUID;
        this.accountId = accountId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionUUID() {
        return transactionUUID;
    }

    public void setTransactionUUID(String transactionUUID) {
        this.transactionUUID = transactionUUID;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

}
