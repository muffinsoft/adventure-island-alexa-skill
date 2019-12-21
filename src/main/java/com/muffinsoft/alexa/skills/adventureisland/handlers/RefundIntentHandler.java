package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.PersistentState;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import com.muffinsoft.alexa.skills.adventureisland.util.ApiCommunicator;
import com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.model.services.monetization.EntitlementReason.AUTO_ENTITLED;
import static com.amazon.ask.request.Predicates.intentName;

public class RefundIntentHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RefundIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("RefundIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        logger.debug("Refund intent invoked");
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);
        boolean arePurchasesDisabled = ApiCommunicator.areInSkillPurchasesEnabled(input);
        String speechText = PhraseManager.getPhrase("purchaseNoRefund");
        String repromptText = PhraseManager.getPhrase("unrecognized");
        StateItem stateItem = Utils.getStateItem(input);
        PersistentState persistentState = Utils.getPersistentState(input);

        if (PurchaseManager.isEntitled(product) && product.getEntitlementReason() != AUTO_ENTITLED) {
            Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
            JSONObject json = new JSONObject(sessionAttributes);

            SendRequestDirective directive = getRefundDirective(product.getProductId(), json.toString());
            return input.getResponseBuilder()
                    .addDirective(directive)
                    .build();
        } else if (PurchaseManager.isEntitled(product) && product.getEntitlementReason() == AUTO_ENTITLED || !arePurchasesDisabled) {
            return ResponseBuilder.replyAndContinue(input, "unknownRequest");
        } else if (PurchaseManager.isPurchasable(product)) {
            stateItem.setState(State.CONTINUE);
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        } else if (PurchaseManager.isPending(product, persistentState.getPurchaseState())) {
            return ResponseBuilder.replyAndContinue(input, "purchaseRefundNotPurchasable");
        } else {
            //NON_PURCHASABLE
            return ResponseBuilder.replyAndContinue(input, "unknownRequest");
        }
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
