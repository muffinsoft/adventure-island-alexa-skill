package com.muffinsoft.alexa.skills.adventureisland.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.*;
import com.amazon.ask.model.interfaces.connections.SendRequestDirective;
import com.amazon.ask.model.interfaces.viewport.ViewportState;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.amazon.ask.model.ui.OutputSpeech;
import com.amazon.ask.model.ui.Reprompt;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.PurchaseState;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;
import com.muffinsoft.alexa.skills.adventureisland.model.SpecialReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.props;

public class ResponseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ResponseBuilder.class);

    private ResponseBuilder(){}

    private static final String DOCUMENT_JSON = "apl/document.json";
    private static final String DOCUMENT2_JSON = "apl/document2.json";

    private static final String IMAGES_DIR = props.getProperty("images-dir");
    private static final String SMALL_IMAGE_WIDTH = props.getProperty("small-image-width");
    private static final String SMALL_IMAGE_HEIGHT = props.getProperty("small-image-height");

    public static Optional<Response> getResponse(HandlerInput input, SlotName slotName) {
        return getResponse(input, slotName, null);
    }

    public static Optional<Response> getResponse(HandlerInput input, SlotName slotName, SpecialReply specialReply) {
        String userReply = specialReply != null ? specialReply.text : null;

        if (slotName != null) {
            Request request = input.getRequestEnvelope().getRequest();
            IntentRequest intentRequest = (IntentRequest) request;

            Map<String, Slot> slots = intentRequest.getIntent().getSlots();

            if (slots != null && !slots.isEmpty()) {
                Slot slot = slots.get(slotName.text);
                userReply = slot.getValue();
            }
        }

        SessionStateManager stateManager = new SessionStateManager(userReply, input.getAttributesManager(), specialReply);
        InSkillProduct product = updatePurchaseState(input, stateManager);
        DialogItem dialog = stateManager.nextResponse();

        if (dialog.getDirective() != null) {
            return getDirectiveResponse(input, product, dialog);
        } else {
            Response response = assembleResponse(dialog, input);
            return Optional.of(response);
        }
    }

    private static InSkillProduct updatePurchaseState(HandlerInput input, SessionStateManager stateManager) {
        InSkillProduct product = PurchaseManager.getInSkillProduct(input);
        stateManager.setEntitled(PurchaseManager.isEntitled(product));
        stateManager.setPurchasable(PurchaseManager.isPurchasable(product));

        PurchaseState storedState = stateManager.getPersistentPurchaseState();

        if (PurchaseManager.isEntitled(product)) {
            stateManager.updatePersistentPurchaseState(PurchaseState.ENTITLED);
        } else if (PurchaseManager.isPending(product, storedState)) {
            stateManager.updatePersistentPurchaseState(PurchaseState.PENDING);
        } else if (PurchaseManager.isDeclined(product, storedState)) {
            stateManager.updatePersistentPurchaseState(PurchaseState.DECLINED);
        } else if (PurchaseManager.isPurchasable(product)) {
            stateManager.updatePersistentPurchaseState(PurchaseState.NOT_ENTITLED);
        }

        return product;
    }

    private static Optional<Response> getDirectiveResponse(HandlerInput input, InSkillProduct product, DialogItem dialog) {
        if (product == null) {
            throw new RuntimeException("Buy requested, but no product found!");
        }
        SendRequestDirective directive;
        if (Objects.equals(dialog.getDirective(), Constants.UPSELL)) {
             directive = PurchaseManager.getUpsellDirective(product.getProductId(), PhraseManager.getPhrase("purchaseUpSell"), "");
        } else {
            directive = PurchaseManager.getBuyDirective(product.getProductId(), "");
        }
        return input.getResponseBuilder()
                .addDirective(directive)
                .build();
    }

    public static Response assembleResponse(DialogItem dialog, HandlerInput input) {
        String speechText = dialog.getResponseText();
        speechText = speechText.replace("> <", "><");
        OutputSpeech speech = SsmlOutputSpeech.builder()
                .withSsml("<speak>" + speechText + "</speak>")
                .build();


        Response.Builder response = Response.builder()
                .withOutputSpeech(speech)
                .withShouldEndSession(dialog.isEnd());

        if (isAplReady(input) && dialog.getBackgroundImage() != null) {
            adjustImageSize(dialog, input);
            createCard(dialog, response);
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

    private static void createCard(DialogItem dialog, Response.Builder response) {

        if (dialog.getBackgroundImage() == null && dialog.getBackgroundImage1() != null) {
            dialog.setBackgroundImage(dialog.getBackgroundImage1());
            dialog.setBackgroundImage1(null);
        }

        boolean multipleImages = dialog.getBackgroundImage1() != null;

        String documentPath = multipleImages ? DOCUMENT2_JSON : DOCUMENT_JSON;

        Map<String, Object> document = contentLoader.loadContent(new HashMap<>(), documentPath, new TypeReference<HashMap<String, Object>>() {});

        Map<String, Object> content = new HashMap<>();
        content.put("title", dialog.getCardText());
        if (multipleImages) {
            content.put("backgroundImage1", dialog.getBackgroundImage1());
        }
        content.put("backgroundImage", dialog.getBackgroundImage());
        Map<String, Object> dataSources = new HashMap<>();
        dataSources.put("templateData", content);

        Directive directive = RenderDocumentDirective.builder()
                .withDocument(document)
                .withDatasources(dataSources)
                .withToken("whatever")
                .build();

        response.addDirectivesItem(directive);

        if (multipleImages) {
            Command command = SetPageCommand.builder()
                    .withComponentId("picCarousel")
                    .withPosition(Position.RELATIVE)
                    .withValue(1)
                    .build();

            Directive directive1 = ExecuteCommandsDirective.builder()
                    .addCommandsItem(command)
                    .withToken("whatever")
                    .build();

            response.addDirectivesItem(directive1);
        }
    }

    private static void adjustImageSize(DialogItem dialog, HandlerInput input) {
        BigDecimal width = new BigDecimal(SMALL_IMAGE_WIDTH);
        BigDecimal height = new BigDecimal(SMALL_IMAGE_HEIGHT);
        ViewportState viewport = input.getRequestEnvelope().getContext().getViewport();
        if (viewport.getPixelWidth().compareTo(width) < 0 || viewport.getPixelHeight().compareTo(height) < 0) {
            if (dialog.getBackgroundImage() != null) {
                dialog.setBackgroundImage(dialog.getBackgroundImage().replace(IMAGES_DIR, IMAGES_DIR + SMALL_IMAGE_WIDTH + "/"));
            }
            if (dialog.getBackgroundImage1() != null) {
                dialog.setBackgroundImage1(dialog.getBackgroundImage1().replace(IMAGES_DIR, IMAGES_DIR + SMALL_IMAGE_WIDTH + "/"));
            }
        }
    }

    private static boolean isAplReady(HandlerInput input) {
        boolean result = false;
        try {
            SupportedInterfaces interfaces = input.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces();
            boolean hasDisplay = interfaces.getDisplay() != null;
            boolean supportsApl = interfaces.getAlexaPresentationAPL() != null;
            logger.debug("Has display: {}, supports APL: {}", hasDisplay, supportsApl);
            result = supportsApl;

        } catch (Exception e) {
            logger.debug("Caught exception", e);
        }
        return result;
    }

}
