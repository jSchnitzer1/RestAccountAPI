package com.accounts.api.serialization;

import com.accounts.api.model.*; // only for testing usage (simulate the real customers)
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JsonObjectDeserializerTest {
    private static final Logger LOGGER = Logger.getLogger(JsonObjectDeserializerTest.class.getName());
    public static List<CustomerTest> jsonToCustomers(Reader reader) throws IOException, ParseException {
        List<CustomerTest> customerAccounts = new ArrayList<>();
        JSONParser parser=new JSONParser();
        Object object = parser.parse(reader);

        JSONArray array = (JSONArray) object;
        array.forEach(ca -> {
            List<AccountTest> accounts = new ArrayList<>();
            JSONObject jCustomerAccounts = (JSONObject) ca;

            JSONObject jCustomer = (JSONObject) jCustomerAccounts.get("customerDTO");
            CustomerTest customer = new CustomerTest((long) jCustomer.get("customerId"), (String) jCustomer.get("firstname"), (String)jCustomer.get("lastname"), null);

            JSONArray jAccounts = (JSONArray) jCustomerAccounts.get("accountDTOs");
            jAccounts.forEach(a -> {
                JSONObject jAccount = (JSONObject) a;

                JSONArray jTransactions = (JSONArray) jAccount.get("transactions");
                List<TransactionTest> transactions = new ArrayList<>();
                jTransactions.forEach(jTx -> {
                    JSONObject jTransaction = (JSONObject) jTx;
                    transactions.add(new TransactionTest((long) jTransaction.get("transactionId"), (double) (Math.round(((double) jTransaction.get("transactionAmount")) * 100) / 100), (String) jTransaction.get("transactionUUID"), (long) jTransaction.get("accountId")));
                });

                AccountTest account = new AccountTest((long) jAccount.get("accountId"), (double) jAccount.get("balance"), (boolean) jAccount.get("defaultAccount"), customer, transactions);
                accounts.add(account);
            });
            customer.setAccounts(accounts);
            customerAccounts.add(customer);
        });

        return customerAccounts.size() > 0 ? customerAccounts : null;
    }

    public static boolean jsonToCreatedAccount(Reader reader) throws IOException, ParseException {
        JSONParser parser=new JSONParser();
        Object object = parser.parse(reader);
        JSONObject jsonObject = (JSONObject) object;
        try {
            Long accountId = (Long) jsonObject.get("accountId");
            Long customerId = (Long) jsonObject.get("customerId");
            return (accountId != null);
        } catch (Exception ex) {
            LOGGER.error("Customer id is not in database!");
            return false;
        }
    }
}
