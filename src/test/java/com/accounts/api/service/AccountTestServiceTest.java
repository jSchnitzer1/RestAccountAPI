package com.accounts.api.service;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import com.accounts.api.model.CustomerTest;
import com.accounts.api.serialization.JsonObjectDeserializerTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * For this unit test to pass all methods, the RestTransactions web service must be online
 * Otherwise, some methods will fail, specifically createTransaction, the method that creates a transaction for a customer
 */

public class AccountTestServiceTest extends JerseyTest {

    @Override
    public Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(AccountService.class);
    }

    @Test
    public void getIt() {
        Response response = target("/account/getIt").request().get();
        assertEquals("status 200", 200, response.getStatus());
        assertNotNull("Should return status 200", response.getEntity().toString());
    }

    @Test
    public void initCustomers() {
        Response response = target("/account/initCustomers").request().post(Entity.json(null));

        testResponseStatus(response, "initCustomers");
        InputStream data = testResponse(response, "initCustomers");

        List<CustomerTest> customers = null;
        try {
            customers = JsonObjectDeserializerTest.jsonToCustomers(new InputStreamReader(data, "UTF-8"));
        } catch (IOException e) {
            fail("IOException in initCustomers: " + e.getMessage());
        } catch (ParseException e) {
            fail("ParseException in initCustomers: " + e.getMessage());
        }

        assertNotNull("initCustomers - list of customers are not null or empty", customers);
        assertEquals("initCustomers - list of customers are three customers", customers.size(), 3);
    }

    @Test
    public void createAccount() {
        Response response = target("/account/createAccount/1/770").request().post(Entity.json(null));

        testResponseStatus(response, "createAccount");
        InputStream data = testResponse(response, "createAccount");

        boolean isCreated = false;
        try {
            isCreated = JsonObjectDeserializerTest.jsonToCreatedAccount(new InputStreamReader(data, "UTF-8"));
        } catch (IOException e) {
            fail("IOException in createAccount: " + e.getMessage());
        } catch (ParseException e) {
            fail("ParseException in createAccount: " + e.getMessage());
        }

        assertTrue("createAccount - a new account is created successfully", isCreated);
    }

    /**
     * This method requires RestTransactionsAPI to be up and running
     * othewise, it fails
     */
    @Test
    public void createTransaction() {
        Response response = target("/account/createTransaction/1/50.7").request().post(Entity.json(null));
        testResponseStatus(response, "createTransaction");
    }

    @Test
    public void fetchCustomers() {
        Response response = target("/account/fetchCustomers").request().get();

        testResponseStatus(response, "initCustomers");
        InputStream data = testResponse(response, "initCustomers");

        List<CustomerTest> customers = null;
        try {
            customers = JsonObjectDeserializerTest.jsonToCustomers(new InputStreamReader(data, "UTF-8"));
        } catch (IOException e) {
            fail("IOException in initCustomers: " + e.getMessage());
        } catch (ParseException e) {
            fail("ParseException in initCustomers: " + e.getMessage());
        }

        assertNotNull("initCustomers - list of customers are not null or empty", customers);
        assertEquals("initCustomers - list of customers are three customers", customers.size(), 3);
    }

    @Test
    public void fetchCustomersWithPagination() {
        Response response = target("/account/fetchCustomersWithPagination").queryParam("start", "1").queryParam("size", 2).request().get();

        testResponseStatus(response, "fetchCustomersWithPagination");
        InputStream data = testResponse(response, "fetchCustomersWithPagination");
        List<CustomerTest> customers = null;
        try {
            customers = JsonObjectDeserializerTest.jsonToCustomers(new InputStreamReader(data, "UTF-8"));
        } catch (IOException e) {
            fail("IOException in initCustomers: " + e.getMessage());
        } catch (ParseException e) {
            fail("ParseException in initCustomers: " + e.getMessage());
        }

        assertNotNull("initCustomers - list of customers are not null or empty", customers);
        assertEquals("initCustomers - list of customers are three customers", customers.size(), 2);
    }

    private void testResponseStatus(Response response, String methodName) {
        int status = response.getStatus();
        assertEquals(methodName + " response status is: 200", status, response.getStatus());
    }
    private InputStream testResponse(Response response, String methodName) {
        InputStream data = (InputStream) response.getEntity();
        assertNotNull(methodName + " response data should not be null", data);
        return data;
    }
}
