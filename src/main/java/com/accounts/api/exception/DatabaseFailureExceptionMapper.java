package com.accounts.api.exception;

import com.accounts.api.model.error.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DatabaseFailureExceptionMapper implements ExceptionMapper<DatabaseFailureException> {
    @Override
    public Response toResponse(DatabaseFailureException ex) {
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 500, "RestAccountsAPI faults resources");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorMessage)
                .build();
    }
}
