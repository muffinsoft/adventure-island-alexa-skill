package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.ConnectionsResponse;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConnectionsResponseHandler implements com.amazon.ask.dispatcher.request.handler.impl.ConnectionsResponseHandler{
    @Override
    public boolean canHandle(HandlerInput input, ConnectionsResponse connectionsResponse) {
        String name = input.getRequestEnvelopeJson().get("request").get("name").asText();
        return (name.equalsIgnoreCase("Buy") || name.equalsIgnoreCase("Upsell"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, ConnectionsResponse connectionsResponse) {
        JsonNode token = input.getRequestEnvelopeJson().get("request").get("token");
        input.getAttributesManager().setSessionAttributes(getSessionAttributes(token));

        String code = input.getRequestEnvelopeJson().get("request").get("status").get("code").asText();
        if(code.equalsIgnoreCase("200")) {
            String speechText;
            String repromptText;
            InSkillProduct premiumProduct = PurchaseManager.getInSkillProduct(input);
            String purchaseResult = input.getRequestEnvelopeJson().get("request").get("payload").get("purchaseResult").asText();

            switch (purchaseResult) {
                case "PENDING_PURCHASE" :
                    //TODO
                    break;
                case "ACCEPTED" : {
                    speechText = PhraseManager.getPhrase("purchaseComplete");
                    break;
                }
                case "DECLINED" : {
                    String name = input.getRequestEnvelopeJson().get("request").get("name").asText();
                    if(name.equalsIgnoreCase("Buy")) {
                        // response when declined buy request
                        speechText = "Thanks for your interest in the "+premiumProduct.getName()+". Would you like to listen to the standard greeting?";
                        repromptText = "Would you like to listen to the standard greeting?";
                        break;
                    }
                    // response when declined upsell request
                    break;
                }
                case "ALREADY_PURCHASED": {
                    speechText = "You already own the "+premiumProduct.getName()+". "+IspUtil.getPremiumGreeting()+IspUtil.getRandomYesNoQuestion();
                    repromptText = IspUtil.getRandomYesNoQuestion();
                    break;
                }
                default:
                    speechText = "Something unexpected happened, but thanks for your interest in the "+premiumProduct.getName()+".  Would you like another random greeting?";
                    repromptText = "Would you like another random greeting?";
            }
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        } else {
            //Something failed
            System.out.println("Connections.Response indicated failure. error: "+input.getRequestEnvelopeJson().get("request").get("status").get("message").toString());
            return input.getResponseBuilder()
                    .withSpeech("There was an error handling your purchase request. Please try again or contact us for help.")
                    .build();
        }

    }

    private Map<String, Object> getSessionAttributes(JsonNode jsonNode) {
        String json = jsonNode.toString().replaceAll("^\"|\"$|\\\\", "");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            TypeReference<HashMap<String, Object>> mapType = new TypeReference<HashMap<String, Object>>() {};
            Map<String, Object> result = mapper.readValue(json, mapType);
            return result;
        } catch (IOException e) {
            throw new AskSdkException("Unable to read or deserialize data"+e.getMessage());
        }
    }
}
