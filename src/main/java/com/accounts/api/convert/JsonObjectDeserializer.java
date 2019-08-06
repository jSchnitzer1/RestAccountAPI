package com.accounts.api.convert;

import com.accounts.api.model.dto.TransactionDTO;
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

    public static List<TransactionDTO> jsonToTransactionsDTO(Reader reader) throws IOException, ParseException {
        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        JSONParser parser=new JSONParser();
        Object object = parser.parse(reader);

        JSONArray array = (JSONArray) object;
        array.forEach(tDTO -> {
            JSONObject jtDTO = (JSONObject) tDTO;
            TransactionDTO transactionDTO = new TransactionDTO((long) jtDTO.get("transactionId"), (double) jtDTO.get("transactionAmount"), (String) jtDTO.get("transactionUUID"), (long) jtDTO.get("accountId"));
            transactionDTOs.add(transactionDTO);
        });

        return transactionDTOs;
    }


}
