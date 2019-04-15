package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.amazon.ask.model.services.monetization.InSkillProductsResponse;
import com.amazon.ask.model.services.monetization.MonetizationServiceClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseManager {

    public static InSkillProduct getInSkillProduct(HandlerInput input) {
        String locale = input.getRequestEnvelope().getRequest().getLocale();
        MonetizationServiceClient client = input.getServiceClientFactory().getMonetizationService();
        InSkillProductsResponse response = client.getInSkillProducts(locale, null, null, null, null, null);
        List<InSkillProduct> inSkillProducts = response.getInSkillProducts();
        if (null != inSkillProducts && inSkillProducts.size() > 0) {
            for (InSkillProduct product : inSkillProducts) {
                if (product.getReferenceName().equalsIgnoreCase("Premium")) {
                    return product;
                }
            }
        }
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
}
