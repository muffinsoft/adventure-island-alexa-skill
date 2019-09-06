package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.SpecialReply;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder.getResponse;

public class YesIntentHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(YesIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.YesIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        logger.debug("Yes intent invoked");
        if (Utils.getStateItem(input).getState() == State.BUY) {
            logger.debug("User said yes to buy");
            return PurchaseManager.buy(input);
        } else {
            return getResponse(input, null, SpecialReply.YES);
        }
    }
}
