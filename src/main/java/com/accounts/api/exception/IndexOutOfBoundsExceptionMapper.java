package com.accounts.api.exception;

import com.accounts.api.model.error.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IndexOutOfBoundsExceptionMapper implements ExceptionMapper<IndexOutOfBoundsException> {
    @Override
    public Response toResponse(IndexOutOfBoundsException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage(), 400, "RestAccountsAPI faults resources");
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorMessage)
                .build();
    }
}