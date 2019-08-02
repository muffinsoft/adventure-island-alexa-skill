package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.services.monetization.InSkillProduct;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.ImageManager;
import com.muffinsoft.alexa.skills.adventureisland.content.NicknameManager;
import com.muffinsoft.alexa.skills.adventureisland.game.MissionSelector;
import com.muffinsoft.alexa.skills.adventureisland.game.PurchaseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.State;

import java.math.BigDecimal;
import java.util.*;

import static com.amazon.ask.request.Predicates.requestType;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.MISSION_NAME_PLACEHOLDER;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.game;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.util.ResponseBuilder.assembleResponse;

public class LaunchRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        DialogItem dialog = getSpeechText(input);

        return Optional.of(assembleResponse(dialog, input));
    }

    @SuppressWarnings("unchecked")
    private DialogItem getSpeechText(HandlerInput input) {

        Map<String, Object> persistentAttributes = input.getAttributesManager().getPersistentAttributes();
        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        String missionName = "";
        String speechText;
        String reprompt;
        String cardText = null;
        String backgroundImage = ImageManager.getGeneralImageByKey(Constants.WELCOME);

        InSkillProduct product = PurchaseManager.getInSkillProduct(input);
        boolean purchasable = PurchaseManager.isPurchasable(product);
        boolean entitled = PurchaseManager.isEntitled(product);

        List<List<BigDecimal>> completedMissions;
        List<String> oldObstacles;
        if (persistentAttributes != null && !persistentAttributes.isEmpty()) {
            completedMissions = (List<List<BigDecimal>>) persistentAttributes.getOrDefault(COMPLETED_MISSIONS, Collections.emptyList());
            oldObstacles = (List<String>) persistentAttributes.get(OLD_OBSTACLES);
            List<BigDecimal> checkpoint = (List<BigDecimal>) persistentAttributes.get(CHECKPOINT);
            Map<String, List<String>> achievements = (Map<String, List<String>>) persistentAttributes.get(ACHIEVEMENTS);
            Map<String, List<String>> nicknames = (Map<String, List<String>>) persistentAttributes.get(NICKNAMES);
            if (exists(achievements) || exists(nicknames)) {
                speechText = getPhrase(Constants.WELCOME_BACK_ROYAL);
                if (exists(achievements)) {
                    speechText += " " + getPhrase(Constants.WELCOME_BACK_ROYAL + Constants.ACHIEVEMENTS);
                }
                if (exists(nicknames)) {
                    speechText += " " + NicknameManager.getNicknamesGreeting(nicknames);
                }
            } else if (checkpoint != null || completedMissions != null || oldObstacles != null) {
                speechText = getPhrase(Constants.WELCOME_BACK);
            } else {
                speechText = getPhrase(Constants.WELCOME);
                sessionAttributes.put(STATE, State.WELCOME);
            }
            if (checkpoint != null) {
                int missionIndex = checkpoint.get(1).intValue();
                int tierIndex = checkpoint.get(0).intValue();
                missionName = game.getMissions().get(missionIndex).getTierNames().get(tierIndex);
                speechText += Utils.wrap(getPhrase(Constants.WELCOME_CHECKPOINT));
                reprompt = getPhrase(Constants.WELCOME_CHECKPOINT);
                sessionAttributes.put(STATE, State.CHECKPOINT);
            } else if (completedMissions != null || oldObstacles != null) {
                completedMissions = completedMissions != null ? completedMissions : new ArrayList<>();
                DialogItem missions =  MissionSelector.promptForMission(null, completedMissions, purchasable || entitled);
                String missionPrompt = missions.getResponseText();
                backgroundImage = missions.getBackgroundImage();
                cardText = missions.getCardText();
                speechText += Utils.wrap(missionPrompt);
                sessionAttributes.put(STATE, State.INTRO);
                reprompt = missionPrompt;
            } else {
                reprompt = getPhrase(Constants.WELCOME + Constants.REPROMPT);
            }
        } else {
            speechText = getPhrase(Constants.WELCOME);
            sessionAttributes.put(STATE, State.WELCOME);
            reprompt = getPhrase(Constants.WELCOME + Constants.REPROMPT);
        }
        if (!missionName.isEmpty()) {
            speechText = speechText.replace(MISSION_NAME_PLACEHOLDER, missionName);
            reprompt = reprompt.replace(MISSION_NAME_PLACEHOLDER, missionName);
        }
        return DialogItem.builder()
                .responseText(speechText)
                .backgroundImage(backgroundImage)
                .cardText(cardText)
                .reprompt(reprompt)
                .build();
    }

    private boolean exists(Map<String, List<String>> map) {
        return map != null && !map.isEmpty();
    }
}
