package com.accounts.api.model;

import java.util.List;

public class CustomerTest {
    private long customerId;
    private String firstname;
    private String lastname;
    private List<AccountTest> accounts;

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<AccountTest> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountTest> accounts) {
        this.accounts = accounts;
    }

    public CustomerTest() {
    }

    public CustomerTest(long customerId, String firstname, String lastname, List<AccountTest> accounts) {
        this.customerId = customerId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.accounts = accounts;
    }
}
