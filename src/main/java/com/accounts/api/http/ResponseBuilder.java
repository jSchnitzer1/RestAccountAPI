package com.accounts.api.http;

import com.accounts.api.controller.AccountService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ResponseBuilder {
    private static final Logger LOGGER = Logger.getLogger(ResponseBuilder.class.getName());

    public static Response buildReponse(String method, String contentType, String methodURL) {
        LOGGER.info("buildReponse is triggered");
        Response response = new Response();
        try {
            URL url = new URL(AccountService.getBaseURL() + methodURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", contentType);

            response.setResponseCode(con.getResponseCode());

            if (response.getResponseCode() > 299) {
                response.setReader(new InputStreamReader(con.getErrorStream()));
            } else {
                response.setReader(new InputStreamReader(con.getInputStream()));
            }
            return response;
        } catch (MalformedURLException e) {
            LOGGER.error("checkTransactionService - MalformedURLException: " + e);
        } catch (ProtocolException e) {
            LOGGER.error("checkTransactionService - ProtocolException: " + e);
        } catch (IOException e) {
            LOGGER.error("checkTransactionService - IOException: " + e);
        }

        return null;
    }
}
