package com.accounts.api.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "Customer")
public class Customer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerId")
    private int customerId;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @OneToMany(mappedBy = "customer",
            fetch = FetchType.LAZY)
    private List<Account> accountsRecords;

    public Customer() {
    }

    public Customer(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public Customer(String firstname, String lastname, List<Account> accountsRecords) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.accountsRecords = accountsRecords;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstName) {
        this.firstname = firstName;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastName) {
        this.lastname = lastName;
    }

    public List<Account> getAccountsRecords() {
        return accountsRecords;
    }

    public void setAccountsRecords(List<Account> accountsRecords) {
        this.accountsRecords = accountsRecords;
    }

    public static Optional<Customer> containsInCustomers(final List<Customer> cList, final String firstname, final String lastname) {
        return cList.stream().filter(c -> c.getFirstname().equals(firstname) && c.getLastname().equals(lastname)).findFirst();
    }
}