package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CancelIntentHandler implements RequestHandler {

    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.CancelIntent"));
    }

    public Optional<Response> handle(HandlerInput input) {
        String speechText = "Bye Bye";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard(PhraseManager.getPhrase("welcomeCard"), speechText)
                .build();
    }
}
