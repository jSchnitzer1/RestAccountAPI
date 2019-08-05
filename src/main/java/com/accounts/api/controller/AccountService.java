package com.accounts.api.controller;

import com.accounts.api.database.DatabaseManager;
import com.accounts.api.database.TransactionStatus;
import com.accounts.api.model.ErrorMessage;
import com.accounts.api.model.dto.CustomerAccountsDTO;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.json.simple.*;

@Path("/account")
public class AccountService {
    private static final Logger LOGGER = Logger.getLogger(AccountService.class.getName());
    private static final Properties PROPERTIES = new Properties();
    private static final DatabaseManager DBMANAGER = DatabaseManager.getInstance();
    private static String baseURL;
    @Context
    private ServletContext context;

    public static String getBaseURL() {
        return baseURL;
    }

    @PostConstruct
    public void postConst() {
        DBMANAGER.initDatabase();
        String log4jConfigPath = context.getRealPath("WEB-INF/log4j.properties");
        PropertyConfigurator.configure(log4jConfigPath);
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

    @PreDestroy
    public void preDestroy() {
        DBMANAGER.destroyDatabase();
    }

    @POST
    @Path("/initCustomers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CustomerAccountsDTO> initCustomers() {
        LOGGER.info("initCustomers is triggered");
        List<CustomerAccountsDTO> caDTOs = DBMANAGER.initExistingCustomers();
        //exception is handled in DBMANAGER.initExistingCustomers() using custom exception handler.
        return caDTOs;
    }

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

        return getErrorResponse("Internal database error in creating a new account for customerId" + customerId, 500);
    }

    private Response getErrorResponse(String errorMessageStr, int errorCode) {
        ErrorMessage errorMessage = new ErrorMessage(errorMessageStr, errorCode, "RestAccountsAPI faults resources");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    }

    @POST
    @Path("/createTransaction/{customerId}/{transactionAmount}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTransaction(@PathParam("customerId") int customerId, @PathParam("transactionAmount") double transactionAmount) {
        LOGGER.info("createTransaction is triggered");
        JSONObject json = null;
        TransactionStatus txStatus = DBMANAGER.addTransactionToAccount(customerId, transactionAmount);
        switch (txStatus) {
            case ACCOUNT_NOT_AVAILABLE:
                return getErrorResponse("Account is not available", 501);
            case BALANCE_IS_NOT_ENOUGH:
                return getErrorResponse("Balance must be larger than transaction amount", 502);
            case DATABASE_ENDPOINT_ERROR:
                return getErrorResponse("Database endpoint error", 503);
            case DATABASE_ENDPOINT_RESPONSE_ERROR:
                return getErrorResponse("Database endpoint server error", 504);
            case DATABASE_ENDPOINT_SERVER_ERROR:
                return getErrorResponse("Database endpoint server error", 505);
            case EXCEPTION:
                return getErrorResponse("Unknown Error", 506);
            case SUCCESS:
                json = new JSONObject();
                json.put("result", "success");
                return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
        }
        return getErrorResponse("Internal database error in creating a new transaction for customerId" + customerId, 500);
    }


}
