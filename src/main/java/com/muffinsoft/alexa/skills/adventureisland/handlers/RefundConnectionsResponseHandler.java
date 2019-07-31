package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.ConnectionsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder;

import java.util.Map;
import java.util.Optional;

import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.verifyMap;
import static com.muffinsoft.alexa.skills.adventureisland.handlers.ConnectionsResponseHandler.getSessionAttributes;

public class RefundConnectionsResponseHandler implements com.amazon.ask.dispatcher.request.handler.impl.ConnectionsResponseHandler {

    @Override
    public boolean canHandle(HandlerInput input, ConnectionsResponse connectionsResponse) {
        String name = input.getRequestEnvelopeJson().get("request").get("name").asText();
        return name.equalsIgnoreCase("Cancel");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, ConnectionsResponse connectionsResponse) {
        JsonNode token = input.getRequestEnvelopeJson().get("request").get("token");
        AttributesManager attributesManager = input.getAttributesManager();
        Map<String, Object> sessionAttributes = token != null ? getSessionAttributes(token) :
                verifyMap(attributesManager.getSessionAttributes());

        attributesManager.setSessionAttributes(sessionAttributes);
        StateItem stateItem = new StateItem(attributesManager);

        stateItem.setState(State.CONTINUE);
        return ResponseBuilder.getResponse(input, null, null);
    }
}
