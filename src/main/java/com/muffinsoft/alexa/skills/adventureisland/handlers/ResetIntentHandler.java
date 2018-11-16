package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.PENDING_STATE;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.STATE;

public class ResetIntentHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(CancelIntentHandler.class);

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("ResetIntent"));
    }

    public Optional<Response> handle(HandlerInput input) {
        String speechText = PhraseManager.getPhrase(State.RESET.getKey().toLowerCase() + Constants.PROMPT);

        logger.debug("Processing request to reset with reply {}", speechText);

        changeState(input);

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard(PhraseManager.getPhrase("welcomeCard"), speechText)
                .withReprompt(speechText)
                .build();
    }

    private void changeState(HandlerInput input) {
        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        State state = State.valueOf(String.valueOf(sessionAttributes.getOrDefault(STATE, State.INTRO)));
        sessionAttributes.put(STATE, State.RESET);
        sessionAttributes.put(PENDING_STATE, state);
    }
}
