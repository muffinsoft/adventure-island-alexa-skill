package com.muffinsoft.alexa.skills.adventureisland.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import com.amazon.ask.model.dialog.ElicitSlotDirective;
import com.amazon.ask.model.ui.*;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;
import com.muffinsoft.alexa.skills.adventureisland.model.SpecialReply;

import java.util.Map;
import java.util.Optional;

public class ResponseBuilder {

    private ResponseBuilder(){}

    public static Optional<Response> getResponse(HandlerInput input, SlotName slotName) {
        return getResponse(input, slotName, null);
    }

    public static Optional<Response> getResponse(HandlerInput input, SlotName slotName, SpecialReply specialReply) {
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;

        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        String userReply = null;

        if (slots != null && !slots.isEmpty()) {
            Slot slot = slots.get(slotName.text);
            userReply = slot.getValue();
        } else if (specialReply != null) {
            userReply = specialReply.text;
        }
        SessionStateManager stateManager = new SessionStateManager(userReply, input.getAttributesManager(), specialReply);
        DialogItem dialog = stateManager.nextResponse();

        Response response = assembleResponse(dialog);

        return Optional.of(response);
    }

    public static Response assembleResponse(DialogItem dialog) {
        String speechText = dialog.getResponseText();
        OutputSpeech speech = SsmlOutputSpeech.builder()
                .withSsml("<speak>" + speechText + "</speak>")
                .build();


        Response.Builder response = Response.builder()
                .withOutputSpeech(speech)
                .withShouldEndSession(dialog.isEnd());

        if (dialog.getCardText() != null) {
            Card card = SimpleCard.builder()
                    .withTitle(PhraseManager.getPhrase("welcomeCardTitle"))
                    .withContent(dialog.getCardText())
                    .build();
            response = response.withCard(card);
        }


        if (dialog.getReprompt() != null) {
            OutputSpeech text = SsmlOutputSpeech.builder()
                    .withSsml("<speak>" + dialog.getReprompt() + "</speak>")
                    .build();
            Reprompt reprompt = Reprompt.builder()
                    .withOutputSpeech(text)
                    .build();
            response = response.withReprompt(reprompt);
        }

        return response.build();
    }

}
