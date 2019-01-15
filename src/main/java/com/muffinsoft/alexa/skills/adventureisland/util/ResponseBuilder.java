package com.muffinsoft.alexa.skills.adventureisland.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.amazon.ask.model.ui.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;
import com.muffinsoft.alexa.skills.adventureisland.model.SpecialReply;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class ResponseBuilder {

    private ResponseBuilder(){}

    private static final String DOCUMENT_JSON = "apl/document.json";
    private static final String SMALL_IMAGE = "_s";

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

        Response response = assembleResponse(dialog, input);

        return Optional.of(response);
    }

    public static Response assembleResponse(DialogItem dialog, HandlerInput input) {
        String speechText = dialog.getResponseText();
        OutputSpeech speech = SsmlOutputSpeech.builder()
                .withSsml("<speak>" + speechText + "</speak>")
                .build();


        Response.Builder response = Response.builder()
                .withOutputSpeech(speech)
                .withShouldEndSession(dialog.isEnd());

        if (dialog.getBackgroundImage() != null) {
            createCard(dialog, input, response);
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

    private static void createCard(DialogItem dialog, HandlerInput input, Response.Builder response) {
        Map<String, Object> document = contentLoader.loadContent(new HashMap<>(), DOCUMENT_JSON, new TypeReference<HashMap<String, Object>>() {});

        Map<String, Object> content = new HashMap<>();
        content.put("title", dialog.getCardText());
        content.put("backgroundImage", dialog.getBackgroundImage());
        Map<String, Object> dataSources = new HashMap<>();
        dataSources.put("templateData", content);

        Directive directive = RenderDocumentDirective.builder()
                .withDocument(document)
                .withDatasources(dataSources)
                .build();

        response.addDirectivesItem(directive);
    }

}
