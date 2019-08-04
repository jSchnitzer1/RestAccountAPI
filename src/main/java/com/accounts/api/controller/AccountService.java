package com.accounts.api.controller;

import com.accounts.api.database.DatabaseManager;
import com.accounts.api.http.ResponseBuilder;
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
    @Context private ServletContext context;

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
    @Path("/createAccount/{customerId}/{initialAmount}") // another solution is by using either @@QueryParam or @Consumes(MediaType.APPLICATION_JSON) but then we need to provide a JAX-b enabled class for that
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(@PathParam("customerId") int customerId, @PathParam("initialAmount") double initialAmount) {
        LOGGER.info("createAccount is triggered");
        JSONObject json = null;
        int accountId = DBMANAGER.addAccount(customerId, initialAmount);
        if(accountId > 0) {
            json = new JSONObject();
            json.put("accountId", accountId);
            json.put("balance", initialAmount);
            json.put("customerId", customerId);
            return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
        }
        ErrorMessage errorMessage = new ErrorMessage("Internal database error in creating a new account for customerId" + customerId, 500, "RestAccountsAPI faults resources");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    }

    @POST
    @Path("/checkAccountEndpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAccountEndpoint() {
        LOGGER.info("checkAccountEndpoint is triggered");
        int status = ResponseBuilder.buildReponse("POST", "application/json", "/api/transaction/checkTransactionEndpoint");
        if (status > 299) {
            return Response.serverError().build();
        } else {
            return Response.ok("<response>AccountEndpoint: 200 OK</response>").build();
        }
    }


}
