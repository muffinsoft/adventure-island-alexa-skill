package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return PurchaseManager.buy(input);
    }

}
