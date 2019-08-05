package com.accounts.api.http;

import java.io.Reader;

public class Response {
    private int responseCode;
    private Reader reader;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Response() {
    }

    public Response(int responseCode, Reader reader) {
        this.responseCode = responseCode;
        this.reader = reader;
    }
}
