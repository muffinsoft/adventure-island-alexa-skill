package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.content.ReplyManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CancelIntentHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(CancelIntentHandler.class);

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.CancelIntent"));
    }

    public Optional<Response> handle(HandlerInput input) {
        String speechText = ReplyManager.getReply(Constants.NEW_MISSION + Constants.PROMPT);

        changeState(input);

        logger.debug("Processing request to cancel with reply {}", speechText);

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard(PhraseManager.getPhrase("welcomeCard"), speechText)
                .withReprompt(speechText)
                .build();
    }

    private void changeState(HandlerInput input) {
        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        State state = State.valueOf(String.valueOf(sessionAttributes.getOrDefault(SessionStateManager.STATE, State.INTRO)));
        sessionAttributes.put(SessionStateManager.STATE, State.CANCEL);
        sessionAttributes.put(SessionStateManager.PENDING_STATE, state);
    }
}
