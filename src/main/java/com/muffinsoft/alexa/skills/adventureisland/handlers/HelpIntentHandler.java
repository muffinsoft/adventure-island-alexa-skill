package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder.assembleResponse;

public class HelpIntentHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelpIntentHandler.class);

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    public Optional<Response> handle(HandlerInput input) {
        logger.debug("Help intent invoked");
        SessionStateManager stateManager = new SessionStateManager(null, input.getAttributesManager(), null);
        ResponseBuilder.updatePurchaseState(input, stateManager);
        DialogItem dialog = stateManager.initHelp();
        Response response = assembleResponse(dialog, input);
        return Optional.of(response);
    }
}