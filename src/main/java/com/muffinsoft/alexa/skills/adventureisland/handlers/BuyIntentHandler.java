package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class BuyIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("BuyIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);

        if (PurchaseManager.isAvailable(product)) {
            Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
            JSONObject json = new JSONObject(sessionAttributes);

            SendRequestDirective directive = getBuyDirective(product.getProductId(), json.toString());
            return input.getResponseBuilder()
                    .addDirective(directive)
                    .build();
        } else {
            String speechText = PhraseManager.getPhrase("purchaseNothing");
            String repromptText = PhraseManager.getPhrase("unrecognized");
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        }
    }

    private SendRequestDirective getBuyDirective(String productId, String token) {
        // Prepare the directive payload
        Map<String,Object> mapObject = new HashMap<>();
        Map<String, Object> inskillProduct = new HashMap<>();
        inskillProduct.put("productId", productId);
        mapObject.put("InSkillProduct", inskillProduct);

        // Prepare the directive request

        return SendRequestDirective.builder()
                .withPayload(mapObject)
                .withName("Buy")
                .withToken(token)
                .build();
    }

}
