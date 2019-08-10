package com.accounts.api.model;

import java.util.List;

public class CustomerAccountsTest {
    private CustomerTest customer;
    private List<AccountTest> accounts;

    public CustomerAccountsTest() {
    }

    public CustomerAccountsTest(CustomerTest customer, List<AccountTest> accounts) {
        this.customer = customer;
        this.accounts = accounts;
    }

    public CustomerTest getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerTest customer) {
        this.customer = customer;
    }

    public List<AccountTest> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountTest> accounts) {
        this.accounts = accounts;
    }
}