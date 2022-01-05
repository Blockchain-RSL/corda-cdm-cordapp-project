package com.template.webserver.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class ParsingUtils {

    private ParsingUtils() {
        //Hide constructor for utils
    }

    public static Map<String, Object> mapValuesFromJSON(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);

        JSONArray arrParty = (JSONArray) json.get("party");
        String proposer = (String) ((JSONObject)((JSONObject) arrParty.get(0)).get("name")).get("value");
        String proposee = (String) ((JSONObject)((JSONObject) arrParty.get(1)).get("name")).get("value");

        JSONObject primitiveJson = (JSONObject) json.get("primitive");
        JSONObject executionJson = (JSONObject)
                ((JSONObject)
                        ((JSONObject)
                                ((JSONArray)
                                        primitiveJson.get("execution")).get(0))
                                .get("after"))
                        .get("execution");

        JSONObject intrumentTypeBond = (JSONObject)
                ((JSONObject)
                        ((JSONObject)
                                executionJson.get("product"))
                                .get("security"))
                        .get("bond");

        String instrumentType = null;
        if(intrumentTypeBond != null) {
            instrumentType = "bond";
        }

        String instrument = (String)
                ((JSONObject)
                        ((JSONArray)
                            ((JSONObject)
                                intrumentTypeBond.get("productIdentifier"))
                                .get("identifier"))
                        .get(0)).get("value");

        String quantity = String.valueOf((Double)
                ((JSONObject)
                        executionJson.get("quantity"))
                        .get("amount"));

        Double price = (Double)
                ((JSONObject)
                        ((JSONObject)
                                executionJson.get("price"))
                                .get("netPrice"))
                        .get("amount");

        String currency = (String)
                ((JSONObject)((JSONObject)
                        ((JSONObject)
                                executionJson.get("price"))
                                .get("netPrice"))
                        .get("currency"))
                        .get("value");

        String market = currency;

        HashMap<String, Object> mapValues = new HashMap<>();
        mapValues.put("proposer", proposer);
        mapValues.put("proposee", proposee);
        mapValues.put("instrumentType", instrumentType);
        mapValues.put("instrument", instrument);
        mapValues.put("quantity", quantity);
        mapValues.put("price", price);
        mapValues.put("currency", currency);
        mapValues.put("market", market);

        return mapValues;
    }
}
