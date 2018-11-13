package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;

import java.util.Collections;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler {

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    public Optional<Response> handle(HandlerInput input) {
        SessionStateManager stateManager = new SessionStateManager(Collections.emptyMap(), input.getAttributesManager());
        DialogItem dialog = stateManager.initHelp();
        Response response = ActionIntentHandler.assembleResponse(dialog);
        return Optional.of(response);
    }
}