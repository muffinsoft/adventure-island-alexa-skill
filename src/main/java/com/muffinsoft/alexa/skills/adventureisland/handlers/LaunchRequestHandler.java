package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.State;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.MISSION_NAME_PLACEHOLDER;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.game;

public class LaunchRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Response> handle(HandlerInput input) {

        Map<String, Object> persistentAttributes = input.getAttributesManager().getPersistentAttributes();
        String missionName = "";
        String speechText;

        if (persistentAttributes != null && !persistentAttributes.isEmpty()) {
            List<BigDecimal> checkpoint = (List<BigDecimal>) persistentAttributes.get(SessionStateManager.CHECKPOINT);
            Map<String, List<String>> achievements = (Map<String, List<String>>) persistentAttributes.get(SessionStateManager.ACHIEVEMENTS);
            Map<String, List<String>> nicknames = (Map<String, List<String>>) persistentAttributes.get(SessionStateManager.NICKNAMES);
            if (achievements != null && !achievements.isEmpty() && nicknames != null && !nicknames.isEmpty()) {
                speechText = PhraseManager.getPhrase(Constants.WELCOME_BACK_ROYAL);
            } else if (checkpoint != null) {
                speechText = PhraseManager.getPhrase(Constants.WELCOME_BACK);
            } else {
                speechText = PhraseManager.getPhrase(Constants.WELCOME);
                input.getAttributesManager().getSessionAttributes().put(SessionStateManager.STATE, State.WELCOME);
            }
            if (checkpoint != null) {
                int missionIndex = checkpoint.get(1).intValue();
                int tierIndex = checkpoint.get(0).intValue();
                missionName = game.getMissions().get(missionIndex).getTierNames().get(tierIndex);
                speechText += Utils.wrap(PhraseManager.getPhrase(Constants.WELCOME_CHECKPOINT));
            }
        } else {
            speechText = PhraseManager.getPhrase(Constants.WELCOME);
            input.getAttributesManager().getSessionAttributes().put(SessionStateManager.STATE, State.WELCOME);

        }
        if (!missionName.isEmpty()) {
            speechText = speechText.replace(MISSION_NAME_PLACEHOLDER, missionName);
        }

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard(PhraseManager.getPhrase("welcomeCard"), speechText)
                .withReprompt(speechText)
                .build();
    }
}
