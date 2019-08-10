package com.accounts.api.service;

import com.accounts.api.database.DatabaseManager;
import com.accounts.api.database.DatabaseManagerImpl;
import com.accounts.api.database.TransactionStatus;
import com.accounts.api.model.dto.CustomerAccountsDTO;
import com.accounts.api.model.error.ErrorMessage;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.json.simple.*;

@Path("/account")
public class AccountService {
    private static final Logger LOGGER = Logger.getLogger(AccountService.class.getName());
    private static final Properties PROPERTIES = new Properties();
    private static final DatabaseManager DBMANAGER = DatabaseManagerImpl.getInstance();
    private static String baseURL;
    @Context
    private ServletContext context;

    public static String getBaseURL() {
        return baseURL;
    }

    /**
     * creates database session, initialized logger with its configuration, and baseURL of the transaction service from the properties file
     * Note: modify "transactionApiBaseUrl" in the service.properties file if you need to host transaction api into other server
     */
    @PostConstruct
    public void postConst() {
        DBMANAGER.initDatabase();
        AccountServiceHelper.ConfigureLogger(context);
        AccountServiceHelper.prepareBaseUrl(context);
    }

    /**
     * clean up database resources
     */
    @PreDestroy
    public void preDestroy() {
        DBMANAGER.destroyDatabase();
    }

    /**
     * initialize database with customers (this simulates the real-world database)
     * @return list of initialized customers
     */
    @POST
    @Path("/initCustomers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CustomerAccountsDTO> initCustomers() {
        LOGGER.info("initCustomers is triggered");
        List<CustomerAccountsDTO> caDTOs = DBMANAGER.initExistingCustomers();
        //exception is handled in DBMANAGER.initExistingCustomers() using custom exception handler.
        return caDTOs;
    }

    /**
     * creates a new account for a specific customer
     * @param customerId to create account for this customer
     * @param initialAmount initial amount (if not 0, then this is a default account)
     * @return response ok (json formatted with the new account id, customer id and the current balance), otherwise the error specific
     */
    @POST
    @Path("/createAccount/{customerId}/{initialAmount}")
    // another solution is by using either @@QueryParam or @Consumes(MediaType.APPLICATION_JSON) but then we need to provide a JAX-b enabled class for that
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(@PathParam("customerId") int customerId, @PathParam("initialAmount") double initialAmount) {
        LOGGER.info("createAccount is triggered");
        JSONObject json = null;
        int accountId = DBMANAGER.addAccount(customerId, initialAmount);
        if (accountId > 0) {
            json = new JSONObject();
            json.put("accountId", accountId);
            json.put("balance", initialAmount);
            json.put("customerId", customerId);
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
        }
        return AccountServiceHelper.getErrorResponse("Internal database error in creating a new account for customerId" + customerId, 500);
    }

    /**
     * creates a new transaction based on the default account for such a customer
     * @param customerId the customer id to perform a transaction for
     * @param transactionAmount the amount of the transaction to be deducted
     * @return a response success (json) if the transaction created or the error specific problem
     */
    @POST
    @Path("/createTransaction/{customerId}/{transactionAmount}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTransaction(@PathParam("customerId") int customerId, @PathParam("transactionAmount") double transactionAmount) {
        LOGGER.info("createTransaction is triggered");
        JSONObject json = null;
        TransactionStatus txStatus = DBMANAGER.addTransactionToAccount(customerId, transactionAmount);
        switch (txStatus) {
            case ACCOUNT_NOT_AVAILABLE:
                return AccountServiceHelper.getErrorResponse("Account is not available", 501);
            case BALANCE_IS_NOT_ENOUGH:
                return AccountServiceHelper.getErrorResponse("Balance must be larger than transaction amount", 502);
            case DATABASE_ENDPOINT_ERROR:
                return AccountServiceHelper.getErrorResponse("Database endpoint error", 503);
            case DATABASE_ENDPOINT_RESPONSE_ERROR:
                return AccountServiceHelper.getErrorResponse("Database endpoint server error", 504);
            case DATABASE_ENDPOINT_SERVER_ERROR:
                return AccountServiceHelper.getErrorResponse("Database endpoint server error", 505);
            case EXCEPTION:
                return AccountServiceHelper.getErrorResponse("Unknown Error", 506);
            case SUCCESS:
                json = new JSONObject();
                json.put("result", "success");
                return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
        }
        return AccountServiceHelper.getErrorResponse("Internal database error in creating a new transaction for customerId" + customerId, 500);
    }

    /**
     * fetching all customers
     * @return list of customers
     * @throws com.accounts.api.exception.IndexOutOfBoundsExceptionMapper
     */
    @GET
    @Path("/fetchCustomers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CustomerAccountsDTO> fetchCustomers() {
        LOGGER.info("fetchCustomers is triggered");
        return DBMANAGER.fetchCustomers(-1, -1); // when passing -1, then retrieve all customers
    }

    /**
     * fetching customers with pagination feature (overridden method with start, and size query params)
     * @param start start index
     * @param size size of customer collection
     * @return list of customers
     * @throws com.accounts.api.exception.IndexOutOfBoundsExceptionMapper
     */
    @GET
    @Path("/fetchCustomersWithPagination")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CustomerAccountsDTO> fetchCustomers(@QueryParam("start") int start, @QueryParam("size") int size) {
        LOGGER.info("fetchCustomers is triggered");
        return DBMANAGER.fetchCustomers(start, size);
    }




    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/getIt")
    public Response getIt() {
        return Response.ok().entity("status 200").build();
    }

    private static class AccountServiceHelper {
        private static Response getErrorResponse(String errorMessageStr, int errorCode) {
            ErrorMessage errorMessage = new ErrorMessage(errorMessageStr, errorCode, "RestAccountsAPI faults resources");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }

        private static void ConfigureLogger(ServletContext context) {
            if(context != null) {
                String log4jConfigPath = context.getRealPath("WEB-INF/log4j.properties");
                PropertyConfigurator.configure(log4jConfigPath);
            }
        }

        private static void prepareBaseUrl(ServletContext context) {
            if(context == null) {
                // set baseURL manually when dependency injector fails
                baseURL = "http://localhost:8080/RestTransactionsAPI";
                return;
            }

            InputStream inputStream = context.getResourceAsStream("/WEB-INF/service.properties");
            if (inputStream != null) {
                try {
                    PROPERTIES.load(inputStream);
                } catch (IOException e) {
                    LOGGER.error("postConst - unable to load service properties file. Error: ", e);
                }
            }
            baseURL = !PROPERTIES.isEmpty() ? PROPERTIES.getProperty("transactionApiBaseUrl") : "http://localhost:8080/RestTransactionsAPI";
        }

    }
}
