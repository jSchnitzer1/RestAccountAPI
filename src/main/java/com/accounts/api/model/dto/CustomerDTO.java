package com.accounts.api.model.dto;

import com.accounts.api.model.entity.Customer;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This DTO is used to prevent exposing the Customer entity to the world.
 */
@XmlRootElement
public class CustomerDTO implements Serializable {
    private long customerId;
    private String firstname;
    private String lastname;

    public CustomerDTO() {
    }

    public CustomerDTO(long customerId, String firstname, String lastname) {
        this.customerId = customerId;
        this.firstname = firstname;
        this.lastname = lastname;
    }

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

    public static CustomerDTO cloneFromEntity(Customer customer) {
        return new CustomerDTO(customer.getCustomerId(), customer.getFirstname(), customer.getLastname());
    }
}