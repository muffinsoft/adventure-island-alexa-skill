package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.amazon.ask.model.services.monetization.InSkillProductsResponse;
import com.amazon.ask.model.services.monetization.MonetizationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseManager {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseManager.class);

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
        return null != product && String.valueOf(product.getEntitled()).equalsIgnoreCase("ENTITLED");
    }

    public static boolean isAvailable(InSkillProduct product) {
        return product != null && product.getEntitled().toString().equalsIgnoreCase("NOT_ENTITLED")
                && product.getPurchasable().toString().equalsIgnoreCase("PURCHASABLE");
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
