package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.MissionSelector;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.State;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.MISSION_NAME_PLACEHOLDER;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.game;

public class LaunchRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        DialogItem dialog = getSpeechText(input);

        return Optional.of(ActionIntentHandler.assembleResponse(dialog));
    }

    @SuppressWarnings("unchecked")
    private DialogItem getSpeechText(HandlerInput input) {

        Map<String, Object> persistentAttributes = input.getAttributesManager().getPersistentAttributes();
        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        String missionName = "";
        String speechText;
        String reprompt;
        String cardText;

        List<List<BigDecimal>> completedMissions;
        if (persistentAttributes != null && !persistentAttributes.isEmpty()) {
            completedMissions = (List<List<BigDecimal>>) persistentAttributes.get(COMPLETED_MISSIONS);
            List<BigDecimal> checkpoint = (List<BigDecimal>) persistentAttributes.get(CHECKPOINT);
            Map<String, List<String>> achievements = (Map<String, List<String>>) persistentAttributes.get(ACHIEVEMENTS);
            Map<String, List<String>> nicknames = (Map<String, List<String>>) persistentAttributes.get(NICKNAMES);
            if (achievements != null && !achievements.isEmpty() && nicknames != null && !nicknames.isEmpty()) {
                speechText = PhraseManager.getPhrase(Constants.WELCOME_BACK_ROYAL);
            } else if (checkpoint != null || completedMissions != null) {
                speechText = PhraseManager.getPhrase(Constants.WELCOME_BACK);
            } else {
                speechText = PhraseManager.getPhrase(Constants.WELCOME);
                sessionAttributes.put(STATE, State.WELCOME);
            }
            if (checkpoint != null) {
                int missionIndex = checkpoint.get(1).intValue();
                int tierIndex = checkpoint.get(0).intValue();
                missionName = game.getMissions().get(missionIndex).getTierNames().get(tierIndex);
                speechText += Utils.wrap(PhraseManager.getPhrase(Constants.WELCOME_CHECKPOINT));
                reprompt = PhraseManager.getPhrase(Constants.WELCOME_CHECKPOINT);
                sessionAttributes.put(STATE, State.CHECKPOINT);
                cardText = PhraseManager.getTextOnly(Constants.CONTINUE + Constants.CARD);
            } else if (completedMissions != null) {
                String missionPrompt = MissionSelector.promptForMission(null, completedMissions).getResponseText();
                speechText += Utils.wrap(missionPrompt);
                sessionAttributes.put(STATE, State.INTRO);
                reprompt = missionPrompt;
                cardText = PhraseManager.getTextOnly(Constants.SELECT_MISSION + Constants.CARD);
            } else {
                reprompt = PhraseManager.getPhrase(Constants.WELCOME + Constants.REPROMPT);
                cardText = PhraseManager.getTextOnly(Constants.CONTINUE + Constants.CARD);
            }
        } else {
            speechText = PhraseManager.getPhrase(Constants.WELCOME);
            sessionAttributes.put(STATE, State.WELCOME);
            reprompt = PhraseManager.getPhrase(Constants.WELCOME + Constants.REPROMPT);
            cardText = PhraseManager.getTextOnly(Constants.CONTINUE + Constants.CARD);
        }
        if (!missionName.isEmpty()) {
            speechText = speechText.replace(MISSION_NAME_PLACEHOLDER, missionName);
        }
        return DialogItem.builder()
                .responseText(speechText)
                .reprompt(reprompt)
                .cardText(cardText)
                .build();
    }
}
