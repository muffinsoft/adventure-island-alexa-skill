package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.SpecialReply;
import com.muffinsoft.alexa.skills.adventureisland.model.State;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder.getResponse;

public class YesIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.YesIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        if (Utils.getStateItem(input).getState() == State.BUY) {
            return PurchaseManager.buy(input);
        } else {
            return getResponse(input, null, SpecialReply.YES);
        }
    }
}
