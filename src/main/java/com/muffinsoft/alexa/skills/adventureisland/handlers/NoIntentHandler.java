package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.SpecialReply;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder.getResponse;

public class NoIntentHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(NoIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.NoIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        logger.debug("No intent invoked");
        StateItem stateItem = Utils.getStateItem(input);
        if (stateItem.getState() == State.BUY) {
            logger.debug("User said no to buy");
            stateItem.setState(State.CONTINUE);
            return getResponse(input, null);
        } else if (stateItem.getState() == State.UPSELL) {
            logger.debug("User said no to upsell");
            stateItem.setState(State.MAIN_MENU);
            return getResponse(input, null, SpecialReply.YES);
        } else {
            return getResponse(input, null, SpecialReply.NO);
        }
    }
}
