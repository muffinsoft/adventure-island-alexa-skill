package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class WhatCanIBuyHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("WhatCanIBuyIntent"));

    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);
        if(PurchaseManager.isAvailable(product)) {
            String speechText = "Products available for purchase at this time are . To learn more about a product, say 'Tell me more about' followed by the product name. If you are ready to buy, say, 'Buy' followed by the product name. So what can I help you with?";
            String repromptText = "I didn't catch that. What can I help you with?";
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        } else {
            String speechText = "There are no products to offer to you right now. Sorry about that. Would you like a greeting instead?";
            String repromptText = "I didn't catch that. What can I help you with?";
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        }
    }


}
