package com.accounts.api.database;

import com.accounts.api.exception.DataNotFoundException;
import com.accounts.api.exception.DatabaseFailureException;
import com.accounts.api.model.dto.CustomerAccountsDTO;
import com.accounts.api.model.entity.Account;
import com.accounts.api.model.entity.Customer;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static SessionFactory sessionFactory;
    private static Session session;
    private static DatabaseManager databaseManager;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            LOGGER.info("Initialize DatabaseManager (Singleton)");
            databaseManager = new DatabaseManager();
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

    public void destroyDatabase() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            LOGGER.info("destroyDatabase is triggered.");
            session.close();
            sessionFactory.close();
            LOGGER.info("destroyDatabase has destroyed in-memory database.");
        }
    }

    public List<CustomerAccountsDTO> initExistingCustomers() {
        LOGGER.info("initExistingCustomers is triggered");
        List<CustomerAccountsDTO> caDTOs = null;
        List<Customer> inCustomers = Stream
                .of(new Customer("Khaled", "Jendi"), new Customer("Tina", "John"), new Customer("Mat", "Olof"))
                .collect(Collectors.toCollection(ArrayList::new));

        org.hibernate.Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<Customer> customers = fetchCustomersAndAccounts(false);

            //saving all new inCustomers with a default account
            inCustomers.forEach(c -> {
                if (customers == null || customers.isEmpty()) {
                    session.save(c);
                    Account account = new Account(7000, c);
                    session.save(account);
                } else {
                    Optional<Customer> opInCustomer = Customer.containsInCustomers(customers, c.getFirstname(), c.getLastname());
                    if (!opInCustomer.isPresent()) {
                        session.save(c);
                        Account account = new Account(7000, c);
                        session.save(account);
                    }
                }
            });

            //fetch real customers with accounts from database
            List<Customer> resultCustomers = fetchCustomersAndAccounts(true);
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

    public int addAccount(int customerId, double initialAmount) {
        List<Customer> customers = session.createQuery("from Customer c where c.customerId = :customerId").setParameter("customerId", customerId).list();
        if(customers == null || customers.size() == 0) {
            throw new DataNotFoundException("Customer Id: " + customerId + " is not found!");
        }
        try {
            Account account = new Account(initialAmount, customers.get(0));
            session.save(account);
            return account.getAccountId();
        } catch (Exception e) {
            LOGGER.error("addAccount - error in saving account", e);
            return -1;
        }
    }

    private List<Customer> fetchCustomersAndAccounts(boolean fetchAccounts) {
        List<Customer> resultCustomers = session.createQuery("from Customer").list();
        if(fetchAccounts) { // lazy fetching accounts
            resultCustomers.forEach(c -> {
                c.setAccountsRecords(session.createQuery("from Account a where a.customer.customerId = :customerId").setParameter("customerId", c.getCustomerId()).list());
            });
        }
        return resultCustomers;
    }
}
