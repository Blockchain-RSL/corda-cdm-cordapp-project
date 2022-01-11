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

    public static Map<String, Object> mapValuesFromBondJSON(String jsonString) throws ParseException {
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

    public static Map<String, Object> mapValuesFromIrsJSON(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(jsonString);

        JSONObject business = (JSONObject) json.get("businessEvent");
        JSONObject primitives =  (JSONObject) ((JSONArray) business.get("primitives")).get(0);
        JSONObject contractFormation = (JSONObject) primitives.get("contractFormation");
        JSONObject after = (JSONObject) contractFormation.get("after");
        JSONObject trade = (JSONObject) after.get("trade");
        JSONArray party = (JSONArray) trade.get("party");

        String proposee = (String) (((JSONObject) party.get(1)).get("name"));

        JSONObject tradableProduct = (JSONObject) trade.get("tradableProduct");
        JSONObject product = (JSONObject) tradableProduct.get("product");
        JSONObject contractualProduct = (JSONObject) product.get("contractualProduct");
        JSONObject productIdentification = (JSONObject) contractualProduct.get("productIdentification");
        String instrumentType = (String) productIdentification.get("productQualifier");


        JSONObject tradeIdentifier1 = (JSONObject) ((JSONArray) trade.get("tradeIdentifier")).get(1);
        JSONObject assignedIdentifier0 = (JSONObject) ((JSONArray) tradeIdentifier1.get("assignedIdentifier")).get(0);
        JSONObject identifier = (JSONObject) assignedIdentifier0.get("identifier");
        String instrument = (String) identifier.get("value");


        JSONObject tradeLot = (JSONObject) ((JSONArray) tradableProduct.get("tradeLot")).get(0);
        JSONObject priceQuantity1 = (JSONObject) ((JSONArray) tradeLot.get("priceQuantity")).get(1);

        JSONObject quantity0 = (JSONObject) ((JSONArray) priceQuantity1.get("quantity")).get(0);
        JSONObject valueQ = (JSONObject) quantity0.get("value");
        String quantity = String.valueOf((Long)valueQ.get("amount"));


        JSONObject price0 = (JSONObject) ((JSONArray) priceQuantity1.get("price")).get(0);
        JSONObject valueP = (JSONObject) price0.get("value");
        Double price = (Double) valueP.get("amount");

        JSONObject unitOfAmount = (JSONObject) valueP.get("unitOfAmount");
        JSONObject currencyO = (JSONObject) unitOfAmount.get("currency");
        String currency = (String) currencyO.get("value");

        String market = "USNY";


        JSONObject contractDetails = (JSONObject) trade.get("contractDetails");
        JSONObject documentation = (JSONObject) ((JSONArray) contractDetails.get("documentation")).get(0);
        JSONObject documentationIdentification = (JSONObject) documentation.get("documentationIdentification");
        JSONObject masterAgreementO = (JSONObject) documentationIdentification.get("masterAgreement");
        JSONObject masterAgreementType = (JSONObject) masterAgreementO.get("masterAgreementType");
        String masterAgreement = (String) masterAgreementType.get("value");


        JSONObject contractualDefinitions = (JSONObject) ((JSONArray)documentationIdentification.get("contractualDefinitions")).get(0);
        String contractualDefinition = (String) contractualDefinitions.get("value");


        HashMap<String, Object> mapValues = new HashMap<>();
        mapValues.put("proposee", proposee);
        mapValues.put("instrumentType", instrumentType);
        mapValues.put("instrument", instrument);
        mapValues.put("quantity", quantity);
        mapValues.put("price", price);
        mapValues.put("currency", currency);
        mapValues.put("market", market);
        mapValues.put("masterAgreement", masterAgreement);
        mapValues.put("contractualDefinition", contractualDefinition);

        return mapValues;
    }

}
