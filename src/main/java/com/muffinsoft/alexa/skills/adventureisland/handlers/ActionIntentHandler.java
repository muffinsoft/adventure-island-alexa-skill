package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazon.ask.model.ui.*;
import com.amazon.ask.response.ResponseBuilder;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ActionIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("ActionIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;

        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        SessionStateManager stateManager = new SessionStateManager(slots, input.getAttributesManager());
        DialogItem dialog = stateManager.nextResponse();

        Response response = assembleResponse(dialog);

        return Optional.of(response);
    }

    private Response assembleResponse(DialogItem dialog) {
        String speechText = dialog.getResponseText();
        OutputSpeech speech = SsmlOutputSpeech.builder()
                .withSsml("<speak>" + speechText + "</speak>")
                .build();

        Card card = SimpleCard.builder()
                .withTitle(PhraseManager.getPhrase("welcomeCard"))
                .withContent(speechText)
                .build();

        Response.Builder response = Response.builder()
                .withOutputSpeech(speech)
                .withCard(card)
                .withShouldEndSession(dialog.isEnd());

        if (dialog.getSlotName() != null) {
            Directive directive = ElicitSlotDirective.builder()
                    .withSlotToElicit(dialog.getSlotName())
                    .build();
            response = response.addDirectivesItem(directive);
        }

        if (dialog.isRepromptRequired()) {
            Reprompt reprompt = Reprompt.builder()
                    .withOutputSpeech(speech)
                    .build();
            response = response.withReprompt(reprompt);
        }

        return response.build();
    }
}
