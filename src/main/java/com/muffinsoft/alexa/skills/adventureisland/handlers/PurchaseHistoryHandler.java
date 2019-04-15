package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class PurchaseHistoryHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("PurchaseHistoryIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);
        if (PurchaseManager.isEntitled(product)) {
            String speechText = PhraseManager.getPhrase("purchaseHistory");
            String repromptText = PhraseManager.getPhrase("purchaseHistoryReprompt");
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        } else {
            String speechText = PhraseManager.getPhrase("purchaseHistoryNothing");
            String repromptText = PhraseManager.getPhrase("purchaseHistoryNothingReprompt");
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        }
    }
}
