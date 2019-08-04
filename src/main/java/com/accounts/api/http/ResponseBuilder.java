package com.accounts.api.http;

import com.accounts.api.controller.AccountService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ResponseBuilder {
    private static final Logger LOGGER = Logger.getLogger(ResponseBuilder.class.getName());

    public static int buildReponse(String method, String contentType, String methodURL) {
        LOGGER.info("checkTransactionService is triggered");

        try {
            URL url = new URL(AccountService.getBaseURL() + methodURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", contentType);

            int status = con.getResponseCode();

            Reader streamReader = null;

            if (status > 299) {
                streamReader = new InputStreamReader(con.getErrorStream());
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }

            return status;

        } catch (MalformedURLException e) {
            LOGGER.error("checkTransactionService - MalformedURLException: " + e);
        } catch (ProtocolException e) {
            LOGGER.error("checkTransactionService - ProtocolException: " + e);
        } catch (IOException e) {
            LOGGER.error("checkTransactionService - IOException: " + e);
        }
        return 500; //internal server error
    }
}
