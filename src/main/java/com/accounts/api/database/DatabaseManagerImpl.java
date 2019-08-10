package com.accounts.api.database;

import com.accounts.api.serialization.JsonObjectDeserializer;
import com.accounts.api.exception.DataNotFoundException;
import com.accounts.api.exception.DatabaseFailureException;
import com.accounts.api.http.Response;
import com.accounts.api.http.ResponseBuilder;
import com.accounts.api.model.dto.CustomerAccountsDTO;
import com.accounts.api.model.dto.TransactionDTO;
import com.accounts.api.model.entity.Account;
import com.accounts.api.model.entity.Customer;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.simple.parser.ParseException;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement
public class DatabaseManagerImpl implements DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManagerImpl.class.getName());
    private static SessionFactory sessionFactory;
    private static Session session;
    private static DatabaseManager databaseManager;

    private DatabaseManagerImpl() {
    }

    /**
     * Ensuring the singleton design pattern...
     *
     * @return a singleton instance
     */
    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            LOGGER.info("Initialize DatabaseManager (Singleton)");
            databaseManager = new DatabaseManagerImpl();
        }
        return databaseManager;
    }

    /**
     * This method is creates a SessionFactory only and only if the connection to database is disrupted
     * or SessionFactory is closed or null
     */
    public void initDatabase() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            LOGGER.info("initDatabase is triggered.");
            sessionFactory = new Configuration().configure().buildSessionFactory();
            session = sessionFactory.openSession();
            LOGGER.info("initDatabase has initialized the database.");
        }
    }

    /**
     * This method is used to release database resources and session when the service is stopped
     */
    public void destroyDatabase() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            LOGGER.info("destroyDatabase is triggered.");
            session.close();
            sessionFactory.close();
            LOGGER.info("destroyDatabase has destroyed in-memory database.");
        }
    }

    /**
     * This function is used to simulate a "real world database" that has data on it
     *
     * @return List<CustomerAccountsDTO> of customers and their associated accounts
     */
    public List<CustomerAccountsDTO> initExistingCustomers() {
        LOGGER.info("initExistingCustomers is triggered");
        List<CustomerAccountsDTO> caDTOs = null;
        List<Customer> inCustomers = Stream
                .of(new Customer("Khaled", "Jendi"), new Customer("Tina", "John"), new Customer("Mat", "Olof"))
                .collect(Collectors.toCollection(ArrayList::new));

        org.hibernate.Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<Customer> customers = fetchCustomersAndAccounts(false, -1, -1);

            //saving all new inCustomers with a default account
            inCustomers.forEach(c -> {
                if (customers == null || customers.isEmpty()) {
                    session.save(c);
                    Account account = new Account(7000, true, c, null);
                    session.save(account);
                } else {
                    Optional<Customer> opInCustomer = Customer.containsInCustomers(customers, c.getFirstname(), c.getLastname());
                    if (!opInCustomer.isPresent()) {
                        session.save(c);
                        Account account = new Account(7000, true, c, null);
                        session.save(account);
                    }
                }
            });

            //fetch real customers with accounts from database
            List<Customer> resultCustomers = fetchCustomersAndAccounts(true, -1, -1);
            tx.commit();
            caDTOs = CustomerAccountsDTO.createCustomerAccountsDTO(resultCustomers);
            if (caDTOs == null) {
                throw new DatabaseFailureException("Unable to retrieve inCustomers into the database.");
            }
            LOGGER.info("createTestDatabase - new inCustomers and accounts are added successfully.");
            return caDTOs;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.error("createTestDatabase - error in initializing database: ", e);
            throw new DatabaseFailureException("Unable to initialize inCustomers into the database. Error is: " + e.getMessage());
        }
    }

    /**
     * fetching customers from database with all assigned accounts and transactions of these accounts from the transaction web service
     *
     * @return List<CustomerAccountsDTO> of customers and their associated accounts
     */
    public List<CustomerAccountsDTO> fetchCustomers(int start, int size) {
        LOGGER.info("fetchCustomers is triggered");
        List<Customer> customers = fetchCustomersAndAccounts(true, start, size);
        customers.forEach(c -> {
            c.getAccountsRecords().forEach(a -> {
                Response response = ResponseBuilder.buildReponse("GET", "application/json", "/api/transaction/getTransactions/" + a.getAccountId());
                if (response != null && response.getResponseCode() < 299) {
                    try {
                        List<TransactionDTO> transactionDTOs = JsonObjectDeserializer.jsonToTransactionsDTO(response.getReader());
                        a.setTransactionDTOs(transactionDTOs);
                    } catch (IOException e) {
                        LOGGER.error("fetchCustomers - JsonObjectDeserializer.jsonToTransactionsDTO failed because of IOException", e);
                    } catch (ParseException e) {
                        LOGGER.error("fetchCustomers - JsonObjectDeserializer.jsonToTransactionsDTO failed because of ParseException", e);
                    }
                }
            });
        });
        return CustomerAccountsDTO.createCustomerAccountsDTO(customers);
    }

    /**
     * adds account to a current user.
     * @param customerId the customer id that the account will be added to
     * @param initialAmount amount to be added to this account
     * @return
     */
    public int addAccount(int customerId, double initialAmount) {
        List<Customer> customers = session.createQuery("from Customer c where c.customerId = :customerId").setParameter("customerId", customerId).list();
        if (customers == null || customers.size() == 0) {
            throw new DataNotFoundException("Customer Id: " + customerId + " is not found!");
        }
        org.hibernate.Transaction tx = null;
        try {
            Account account = null;
            tx = session.beginTransaction();
            if (initialAmount > 0) {
                updateDefaultAccount(customers.get(0).getCustomerId(), false);
                account = new Account(initialAmount, true, customers.get(0), null);
            } else {
                account = new Account(initialAmount, false, customers.get(0), null);
            }

            session.save(account);
            tx.commit();
            return account.getAccountId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.error("addAccount - error in saving account", e);
            return -1;
        }
    }

    /**
     * perform the transaction against a specific account. Enables a SQL transaction-wise in case transaction does not pass through by deleting the transaction
     * @param customerId the customer id that the transaction will be added to
     * @param transactionAmount transaction to be deducted from the accout
     * @return
     */
    public TransactionStatus addTransactionToAccount(int customerId, double transactionAmount) {
        LOGGER.info("addTransactionToAccount - new transaction is added successfully for customer: " + customerId);
        StringBuilder methodURL = new StringBuilder();
        UUID uuid = UUID.randomUUID();
        String transactionUUID = uuid.toString();

        List<Account> accounts = session.createQuery("from Account where customer.customerId=:customerId and defaultAccount=:defaultAccount")
                .setParameter("customerId", customerId)
                .setParameter("defaultAccount", true)
                .list();
        if (accounts == null || accounts.isEmpty()) return TransactionStatus.ACCOUNT_NOT_AVAILABLE;
        if (accounts.get(0).getBalance() < transactionAmount) return TransactionStatus.BALANCE_IS_NOT_ENOUGH;

        try {
            methodURL.append("/api/transaction/createTransaction/").append(accounts.get(0).getAccountId()).append("/").append(transactionAmount).append("/").append(transactionUUID);
            Response response = ResponseBuilder.buildReponse("POST", "application/json", methodURL.toString());
            if (response == null) return TransactionStatus.DATABASE_ENDPOINT_RESPONSE_ERROR;
            if (response.getResponseCode() > 299) return TransactionStatus.DATABASE_ENDPOINT_SERVER_ERROR;
            if (JsonObjectDeserializer.jsonToTransaction(response.getReader())) {
                accounts.get(0).setBalance(accounts.get(0).getBalance() - transactionAmount);
                session.update(accounts.get(0));
                return TransactionStatus.SUCCESS;
            }
            return TransactionStatus.DATABASE_ENDPOINT_ERROR;
        } catch (Exception ex) {
            methodURL.append("/api/transaction/deleteTransaction/").append(accounts.get(0).getAccountId()).append("/").append(transactionUUID);
            ResponseBuilder.buildReponse("DELETE", "application/json", methodURL.toString());
            return TransactionStatus.EXCEPTION;
        }
    }

    private void updateDefaultAccount(int customerId, boolean isDefault) {
        //int rows = session.createQuery("update Account set defaultAccount = false where customer.customerId = :customerId").setParameter("customerId", customerId).executeUpdate();
        List<Account> accounts = session.createQuery("from Account where customer.customerId=:customerId").setParameter("customerId", customerId).list();
        accounts.forEach(a -> {
            a.setDefaultAccount(isDefault);
            session.update(a);
        });
    }

    /**
     * fetching customers with their accounts
     * @param fetchAccounts set to true to fetch account otherwise accounts are not fetched.
     * @param start pagination support. Start index.
     * @param size size of elements
     * @return List of customers along with their accounts
     */
    private List<Customer> fetchCustomersAndAccounts(boolean fetchAccounts, int start, int size) {
        LOGGER.info("fetchCustomersAndAccounts - fetchAccounts: " + fetchAccounts + ", start: " + start + ", size: " + size);
        List<Customer> resultCustomers = null;

        try {
            if (start == -1 || size == -1)
                resultCustomers = session.createQuery("from Customer").list();
            else
                resultCustomers = session.createQuery("from Customer").list().subList(start, start + size);
        } catch (IndexOutOfBoundsException ex) {
            String errorMsg = "fetchCustomersAndAccounts - IndexOutOfBoundsException start: " + start + " and size: " + size +  " for the customer list";
            LOGGER.error(errorMsg, ex);
            throw new IndexOutOfBoundsException(errorMsg + " " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("fetchCustomersAndAccounts - Unable to parse start: " + start + " and size: " + size +  " for the customer list", ex);
            return new ArrayList<>();
        }

        if (fetchAccounts) { // lazy fetching accounts
            resultCustomers.forEach(c -> {
                c.setAccountsRecords(session.createQuery("from Account a where a.customer.customerId = :customerId").setParameter("customerId", c.getCustomerId()).list());
            });
        }
        return resultCustomers;
    }
}
