package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;

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
        StateItem stateItem = Utils.getStateItem(input);
        stateItem.setPendingState(stateItem.getState());
        String speechText;
        String repromptText;
        if(PurchaseManager.isAvailable(product)) {
            stateItem.setState(State.BUY);
            speechText = PhraseManager.getPhrase("purchaseWhat");
            repromptText = PhraseManager.getPhrase("unrecognized");
        } else if (PurchaseManager.isPending(product)) {
            speechText = PhraseManager.getPhrase("purchaseWhatPending");
            repromptText = PhraseManager.getPhrase("unrecognized");
            stateItem.setState(State.MAIN_OR_CONTINUE);
        } else {
            speechText = PhraseManager.getPhrase("purchaseNothing");
            repromptText = PhraseManager.getPhrase("unrecognized");
            stateItem.setState(State.MAIN_MENU);
        }
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromptText)
                .build();
    }


}
