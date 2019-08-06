package com.accounts.api.model.dto;

import com.accounts.api.model.entity.Customer;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class CustomerAccountsDTO implements Serializable {
    private CustomerDTO customerDTO;
    private List<AccountDTO> accountDTOs;

    public CustomerAccountsDTO() {
    }

    public CustomerAccountsDTO(CustomerDTO customerDTO, List<AccountDTO> accountDTOs) {
        this.customerDTO = customerDTO;
        this.accountDTOs = accountDTOs;
    }

    public CustomerDTO getCustomerDTO() {
        return customerDTO;
    }

    public void setCustomerDTO(CustomerDTO customerDTO) {
        this.customerDTO = customerDTO;
    }

    public List<AccountDTO> getAccountDTOs() {
        return accountDTOs;
    }

    public void setAccountDTOs(List<AccountDTO> accountDTOs) {
        this.accountDTOs = accountDTOs;
    }

    public static List<CustomerAccountsDTO> createCustomerAccountsDTO(List<Customer> customers) {
        if(customers == null || customers.isEmpty()) return null;
        List<CustomerAccountsDTO> caDTOs = new ArrayList<>();
        customers.forEach(c ->
                caDTOs.add(new CustomerAccountsDTO(
                        CustomerDTO.cloneFromEntity(c),
                        AccountDTO.cloneFromEntity(c.getAccountsRecords()))));
        return caDTOs;
    }
}