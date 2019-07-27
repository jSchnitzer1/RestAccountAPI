package com.accounts.api.controller;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.servlet.ServletContext;

@Path("/account")
public class Account {
    private static final Logger LOGGER = Logger.getLogger(Account.class.getName());
    @Context private ServletContext context;

    @PostConstruct
    public void postConst() {
        String log4jConfigPath = context.getRealPath("WEB-INF/log4j.properties");
        PropertyConfigurator.configure(log4jConfigPath);
    }

    @POST
    @Path("/checkAccountEndpoint")
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkAccountEndpoint() {
        LOGGER.info("checkAccountEndpoint is triggered");
        return Response.ok("<response>AccountEndpoint: 200 OK</response>").build();
    }
}
