package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ResetIntentHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(CancelIntentHandler.class);

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.CancelIntent"));
    }

    public Optional<Response> handle(HandlerInput input) {
        String speechText = PhraseManager.getPhrase(Constants.NEW_MISSION + Constants.PROMPT);


        logger.debug("Processing request to cancel with reply {}", speechText);

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard(PhraseManager.getPhrase("welcomeCard"), speechText)
                .withReprompt(speechText)
                .build();
    }
}
