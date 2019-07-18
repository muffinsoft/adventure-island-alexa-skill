package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.ConnectionsResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.PersistentState;
import com.muffinsoft.alexa.skills.adventureisland.model.PurchaseState;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.verifyMap;

public class ConnectionsResponseHandler implements com.amazon.ask.dispatcher.request.handler.impl.ConnectionsResponseHandler {
    @Override
    public boolean canHandle(HandlerInput input, ConnectionsResponse connectionsResponse) {
        String name = input.getRequestEnvelopeJson().get("request").get("name").asText();
        return (name.equalsIgnoreCase("Buy") || name.equalsIgnoreCase("Upsell"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, ConnectionsResponse connectionsResponse) {
        JsonNode token = input.getRequestEnvelopeJson().get("request").get("token");
        AttributesManager attributesManager = input.getAttributesManager();
        Map<String, Object> sessionAttributes = token != null ? getSessionAttributes(token) :
                verifyMap(attributesManager.getSessionAttributes());

        attributesManager.setSessionAttributes(sessionAttributes);
        attributesManager.setPersistentAttributes(verifyMap(attributesManager.getPersistentAttributes()));
        PersistentState persistentState = new PersistentState(attributesManager);
        StateItem stateItem = new StateItem(attributesManager);

        String code = input.getRequestEnvelopeJson().get("request").get("status").get("code").asText();
        if (code.equalsIgnoreCase("200")) {
            String speechText;
            String repromptText;
            String purchaseResult = input.getRequestEnvelopeJson().get("request").get("payload").get("purchaseResult").asText();

            switch (purchaseResult) {
                case "PENDING_PURCHASE":
                    persistentState.setLastPurchaseAttempt(ZonedDateTime.now());
                    persistentState.setPurchaseState(PurchaseState.PENDING);
                    speechText = PhraseManager.getPhrase("purchaseWait");
                    stateItem.setState(State.PLAY_AGAIN);
                    repromptText = speechText;
                    break;
                case "ACCEPTED": {
                    persistentState.setPurchaseState(PurchaseState.ENTITLED);
                    speechText = PhraseManager.getPhrase("purchaseComplete");
                    repromptText = PhraseManager.getPhrase("purchaseCompleteRePrompt");
                    stateItem.setState(State.PLAY_NEW);
                    break;
                }
                case "DECLINED": {
                    stateItem.setState(State.MAIN_MENU);
                    return ResponseBuilder.getResponse(input, null, null);
                }
                case "ALREADY_PURCHASED": {
                    speechText = PhraseManager.getPhrase("purchaseAlreadyOwn");
                    repromptText = PhraseManager.getPhrase("purchaseAlreadyOwnRePrompt");
                    persistentState.setPurchaseState(PurchaseState.ENTITLED);
                    stateItem.setState(State.PLAY_AGAIN);
                    break;
                }
                default:
                    speechText = PhraseManager.getPhrase("purchaseUnsuccessful");
                    repromptText = PhraseManager.getPhrase("playAgainRePrompt");
                    stateItem.setState(State.PLAY_AGAIN);
                    persistentState.setPurchaseState(PurchaseState.FAILED);
                    persistentState.setLastPurchaseAttempt(ZonedDateTime.now());
                    break;
            }

            attributesManager.savePersistentAttributes();

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        } else {
            //Something failed
            System.out.println("Connections.Response indicated failure. error: " + input.getRequestEnvelopeJson().get("request").get("status").get("message").toString());
            stateItem.setState(State.PLAY_AGAIN);
            persistentState.setPurchaseState(PurchaseState.FAILED);
            persistentState.setLastPurchaseAttempt(ZonedDateTime.now());
            return input.getResponseBuilder()
                    .withSpeech(PhraseManager.getPhrase("purchaseError"))
                    .withReprompt(PhraseManager.getPhrase("playAgainRePrompt"))
                    .build();
        }

    }

    private Map<String, Object> getSessionAttributes(JsonNode jsonNode) {
        String json = jsonNode.toString().replaceAll("^\"|\"$|\\\\", "");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            TypeReference<HashMap<String, Object>> mapType = new TypeReference<HashMap<String, Object>>() {
            };
            return mapper.readValue(json, mapType);
        } catch (IOException e) {
            throw new AskSdkException("Unable to read or deserialize data" + e.getMessage());
        }
    }
}
