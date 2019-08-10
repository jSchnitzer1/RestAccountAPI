package com.accounts.api.database;

import com.accounts.api.model.dto.CustomerAccountsDTO;

import java.util.List;

public interface DatabaseManager {
    void initDatabase();
    void destroyDatabase();
    List<CustomerAccountsDTO> initExistingCustomers();
    List<CustomerAccountsDTO> fetchCustomers(int start, int size);
    int addAccount(int customerId, double initialAmount);
    TransactionStatus addTransactionToAccount(int customerId, double transactionAmount);

}
