package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class RefundIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("RefundIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);

        String speechText = PhraseManager.getPhrase("purchaseNoRefund");
        String repromptText = PhraseManager.getPhrase("unrecognized");
        StateItem stateItem = Utils.getStateItem(input);

        if (PurchaseManager.isEntitled(product)) {
            Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
            JSONObject json = new JSONObject(sessionAttributes);

            SendRequestDirective directive = getRefundDirective(product.getProductId(), json.toString());
            return input.getResponseBuilder()
                    .addDirective(directive)
                    .build();
        } else if (PurchaseManager.isPurchasable(product)) {
            stateItem.setState(State.CONTINUE);
        } else {
            speechText = PhraseManager.getPhrase("purchaseRefundNotPurchasable");
            stateItem.setState(State.CONTINUE);
        }
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromptText)
                .build();
    }

    public SendRequestDirective getRefundDirective(String productId, String token) {
        // Prepare the directive payload
        Map<String,Object> mapObject = new HashMap<>();
        Map<String, Object> product = new HashMap<>();
        product.put("productId", productId);
        mapObject.put("InSkillProduct", product);

        // Prepare the directive request
        return SendRequestDirective.builder()
                .withPayload(mapObject)
                .withName("Cancel")
                .withToken(token)
                .build();
    }
}
