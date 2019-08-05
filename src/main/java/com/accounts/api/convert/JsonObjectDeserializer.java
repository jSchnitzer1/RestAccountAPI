package com.accounts.api.convert;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JsonObjectDeserializer {
    private static final Logger LOGGER = Logger.getLogger(JsonObjectDeserializer.class.getName());

    public static boolean jsonToTransaction(Reader reader) throws IOException, ParseException {
        JSONParser parser=new JSONParser();
        Object object = parser.parse(reader); // throws IOException, ParseException
        JSONObject jsonObject = (JSONObject) object;
        try {
            String result = (String) jsonObject.get("result");
            return result.equals("success");
        } catch (Exception ex) {
            return false;
        }
    }
}
