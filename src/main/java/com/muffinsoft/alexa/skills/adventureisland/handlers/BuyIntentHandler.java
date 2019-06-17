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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class BuyIntentHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(BuyIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("BuyIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
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

}
