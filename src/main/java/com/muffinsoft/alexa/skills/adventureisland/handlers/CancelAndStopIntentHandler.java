package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.PENDING_STATE;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.STATE;

public class CancelAndStopIntentHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(CancelAndStopIntentHandler.class);

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.CancelIntent").or(intentName("AMAZON.StopIntent")));
    }

    public Optional<Response> handle(HandlerInput input) {
        String speechText = PhraseManager.getPhrase(Constants.QUIT + Constants.PROMPT);
        String reprompt = PhraseManager.getPhrase(Constants.QUIT + Constants.REPROMPT);

        changeState(input);

        logger.debug("Processing request to cancel with reply {}", speechText);

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(reprompt)
                .build();
    }

    private void changeState(HandlerInput input) {
        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        State state = State.valueOf(String.valueOf(sessionAttributes.getOrDefault(STATE, State.INTRO)));
        sessionAttributes.put(STATE, State.CANCEL);
        sessionAttributes.put(PENDING_STATE, state);
    }
}
