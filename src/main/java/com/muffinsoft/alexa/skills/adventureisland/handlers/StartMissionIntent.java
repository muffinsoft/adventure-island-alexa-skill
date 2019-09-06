package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;
import static com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder.getResponse;

public class StartMissionIntent implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartMissionIntent.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("StartMissionIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        logger.debug("Start mission intent invoked");
        return getResponse(input, SlotName.MISSION, null);
    }
}
