package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.amazon.ask.model.services.monetization.InSkillProductsResponse;
import com.amazon.ask.model.services.monetization.MonetizationServiceClient;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.PurchaseState;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.model.services.monetization.EntitledState.ENTITLED;
import static com.amazon.ask.model.services.monetization.PurchasableState.PURCHASABLE;
import static com.muffinsoft.alexa.skills.adventureisland.model.PurchaseState.DECLINED;
import static com.muffinsoft.alexa.skills.adventureisland.model.PurchaseState.PENDING;

public class PurchaseManager {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseManager.class);

    public static Optional<Response> buy(HandlerInput input) {
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);
        logger.debug("Got in skill product: " + product);

        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        if (PurchaseManager.isAvailable(product)) {
            JSONObject json = new JSONObject(sessionAttributes);

            SendRequestDirective directive = PurchaseManager.getBuyDirective(product.getProductId(), json.toString());

            logger.info("Sending a directive to purchase expansion pack");

            return input.getResponseBuilder()
                    .addDirective(directive)
                    .build();
        } else {
            logger.info("Expansion pack is not available");
            String speechText = PhraseManager.getPhrase("purchaseNothing");
            String repromptText = PhraseManager.getPhrase("unrecognized");
            StateItem stateItem = Utils.getStateItem(input);
            stateItem.setState(State.PLAY_AGAIN);
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptText)
                    .build();
        }
    }

    public static InSkillProduct getInSkillProduct(HandlerInput input) {
        String locale = input.getRequestEnvelope().getRequest().getLocale();
        MonetizationServiceClient client = input.getServiceClientFactory().getMonetizationService();
        InSkillProductsResponse response = client.getInSkillProducts(locale, null, null, null, null, null);
        List<InSkillProduct> inSkillProducts = response.getInSkillProducts();
        if (null != inSkillProducts && inSkillProducts.size() > 0) {
            logger.debug("Got in skill product: " + inSkillProducts.get(0));
            return inSkillProducts.get(0);
        }
        logger.debug("No in skill products found");
        return null;
    }

    public static boolean isEntitled(InSkillProduct product) {
        return null != product && product.getEntitled() == ENTITLED;
    }

    public static boolean isAvailable(InSkillProduct product) {
        return product != null && product.getEntitled() != ENTITLED
                && product.getPurchasable() == PURCHASABLE;
    }

    public static boolean isPending(InSkillProduct product, PurchaseState previousState) {
        return !isEntitled(product) && !isPurchasable(product) && previousState == PENDING;
    }

    public static boolean isDeclined(InSkillProduct product, PurchaseState previousState) {
        return !isEntitled(product) && !isPurchasable(product) && previousState == DECLINED;
    }

    public static boolean isPurchasable(InSkillProduct product) {
        return product != null &&
                product.getPurchasable() == PURCHASABLE;
    }

    public static SendRequestDirective getUpsellDirective(String productId, String upsellMessage, String token) {

        // Prepare the directive payload
        Map<String,Object> mapObject = new HashMap<>();
        Map<String, Object> inskillProduct = new HashMap<>();
        inskillProduct.put("productId", productId);
        mapObject.put("upsellMessage", upsellMessage);
        mapObject.put("InSkillProduct", inskillProduct);

        return SendRequestDirective.builder()
                .withPayload(mapObject)
                .withName("Upsell")
                .withToken(token)
                .build();
    }

    public static SendRequestDirective getBuyDirective(String productId, String token) {
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
