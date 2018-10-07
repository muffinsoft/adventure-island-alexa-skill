package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazon.ask.model.ui.*;
import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ActionIntentHandler implements RequestHandler {

    private static final String ITEM = "item";

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("ActionIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String slotName = SlotName.ACTION.text;

        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;

        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        String reply = slots.get(slotName).getValue();

        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();

        String saved = null;
        if (attributes != null) {
            Object savedItem = attributes.get(ITEM);
            saved = String.valueOf(savedItem);
        } else {
            attributes = new HashMap<>();
        }

        boolean shouldEnd = false;
        String speechText;
        if (reply == null || reply.isEmpty()) {
            speechText = PhraseManager.getPhrase("actionDescription");
            speechText = nextObstacle(input, attributes, speechText);
        } else if (ObstacleManager.getObstacleResponse(saved).equals(reply)) {
            speechText = PhraseManager.getPhrase("actionApprove");
            speechText = nextObstacle(input, attributes, speechText);
        } else {
            speechText = PhraseManager.getPhrase("actionFail");
            shouldEnd = true;
        }

        OutputSpeech speech = SsmlOutputSpeech.builder()
                .withSsml("<speak>" + speechText + "</speak>")
                .build();

        Card card = SimpleCard.builder()
                .withTitle(PhraseManager.getPhrase("welcomeCard"))
                .withContent(speechText)
                .build();

        Intent intent = Intent.builder()
                .withName("ActionIntent")
                .putSlotsItem(slotName, Slot.builder()
                        .withName(slotName)
                        .withValue("woah")
                        .build())
                .build();
        Directive directive = ElicitSlotDirective.builder()
                .withSlotToElicit(slotName)
                .withUpdatedIntent(intent)
                .build();

        Response response = Response.builder()
                .withOutputSpeech(speech)
                .withCard(card)
                .addDirectivesItem(directive)
                .withShouldEndSession(shouldEnd)
                .build();

        return Optional.of(response);
    }

    private String nextObstacle(HandlerInput input, Map<String, Object> attributes, String speechText) {
        String obstacle = ObstacleManager.getObstacle();
        attributes.put(ITEM, obstacle);
        input.getAttributesManager().setSessionAttributes(attributes);
        speechText += " " + obstacle;
        return speechText;
    }
}
