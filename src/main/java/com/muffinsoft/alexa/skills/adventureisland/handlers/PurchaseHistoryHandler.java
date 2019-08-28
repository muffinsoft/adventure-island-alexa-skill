package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.PersistentState;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import com.muffinsoft.alexa.skills.adventureisland.util.ApiCommunicator;
import com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder;

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
        boolean arePurchasesEnabled = ApiCommunicator.areInSkillPurchasesEnabled(input);
        StateItem stateItem = Utils.getStateItem(input);
        PersistentState persistentState = Utils.getPersistentState(input);
        stateItem.setPendingState(stateItem.getState());
        stateItem.setState(State.CONTINUE);
        String speechText;
        String repromptText;
        if (PurchaseManager.isEntitled(product)) {
            speechText = PhraseManager.getPhrase("purchaseHistory");
            repromptText = PhraseManager.getPhrase("purchaseHistoryReprompt");
            stateItem.setState(State.MAIN_OR_CONTINUE);
        } else if (!arePurchasesEnabled) {
            return ResponseBuilder.replyAndContinue(input, "unknownRequest");
        } else if (PurchaseManager.isAvailable(product)) {
            speechText = PhraseManager.getPhrase("purchaseHistoryNothing");
            repromptText = PhraseManager.getPhrase("purchaseHistoryNothingReprompt");
        } else if (PurchaseManager.isPending(product, persistentState.getPurchaseState())) {
            speechText = PhraseManager.getPhrase("purchaseHistoryPending");
            repromptText = PhraseManager.getPhrase("purchaseAlreadyOwnRePrompt");
            stateItem.setState(State.MAIN_OR_CONTINUE);
        } else {
            speechText = PhraseManager.getPhrase("purchaseNothing");
            repromptText = PhraseManager.getPhrase("unrecognized");
            stateItem.setState(State.MAIN_OR_CONTINUE);
        }
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromptText)
                .build();
    }
}
