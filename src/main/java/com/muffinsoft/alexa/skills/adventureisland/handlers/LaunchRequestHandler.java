package com.muffinsoft.alexa.skills.adventureisland.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.MissionSelector;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.State;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.NICKNAMES;

public class LaunchRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        String speechText = getSpeechText(input);
        speechText = TagProcessor.insertTags(speechText);

        return Optional.of(ActionIntentHandler.assembleResponse(new DialogItem(speechText, false, null, true)));
    }

    @SuppressWarnings("unchecked")
    private String getSpeechText(HandlerInput input) {

        Map<String, Object> persistentAttributes = input.getAttributesManager().getPersistentAttributes();
        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        String missionName = "";
        String speechText;

        List<List<BigDecimal>> completedMissions;
        if (persistentAttributes != null && !persistentAttributes.isEmpty()) {
            completedMissions = (List<List<BigDecimal>>) persistentAttributes.get(COMPLETED_MISSIONS);
            List<BigDecimal> checkpoint = (List<BigDecimal>) persistentAttributes.get(CHECKPOINT);
            Map<String, List<String>> achievements = (Map<String, List<String>>) persistentAttributes.get(AttributeKeys.ACHIEVEMENTS);
            Map<String, List<String>> nicknames = (Map<String, List<String>>) persistentAttributes.get(NICKNAMES);
            if (achievements != null && !achievements.isEmpty() && nicknames != null && !nicknames.isEmpty()) {
                speechText = getRoyalGreeting(achievements, nicknames);
            } else if (checkpoint != null || completedMissions != null) {
                speechText = PhraseManager.getPhrase(WELCOME_BACK);
            } else {
                speechText = PhraseManager.getPhrase(WELCOME);
                sessionAttributes.put(STATE, State.WELCOME);
            }
            if (checkpoint != null) {
                int missionIndex = checkpoint.get(1).intValue();
                int tierIndex = checkpoint.get(0).intValue();
                missionName = game.getMissions().get(missionIndex).getTierNames().get(tierIndex);
                speechText += Utils.wrap(PhraseManager.getPhrase(WELCOME_CHECKPOINT));
                sessionAttributes.put(STATE, State.CHECKPOINT);
            } else if (completedMissions != null) {
                String missionPrompt = MissionSelector.promptForMission(null, completedMissions).getResponseText();
                speechText += Utils.wrap(missionPrompt);
                sessionAttributes.put(STATE, State.INTRO);
            }
        } else {
            speechText = PhraseManager.getPhrase(WELCOME);
            sessionAttributes.put(STATE, State.WELCOME);

        }
        if (!missionName.isEmpty()) {
            speechText = speechText.replace(MISSION_NAME_PLACEHOLDER, missionName);
        }
        return speechText;
    }

    private String getRoyalGreeting(Map<String, List<String>> achievements, Map<String, List<String>> nicknames) {
        String result = "";

        if (nicknames != null && !nicknames.isEmpty()) {
            String nicknamePhrase = PhraseManager.getPhrase(WELCOME_BACK_ROYAL + NICKNAMES);
            StringBuilder latestNicknames = new StringBuilder();
            for (Iterator<String> iterator = nicknames.keySet().iterator(); iterator.hasNext();) {
                List<String> nicknameList = nicknames.get(iterator.next());
                String separator = iterator.hasNext() ? ", " : ", and ";
                String latest = separator + nicknameList.get(nicknameList.size() - 1);
                latestNicknames.append(latest);
            }
            nicknamePhrase = nicknamePhrase.replace(NICKNAME_PLACEHOLDER, latestNicknames);
            result = Utils.combine(result, nicknamePhrase);
        }

        if (achievements != null && !achievements.isEmpty()) {

        }

        result = Utils.combine(result, PhraseManager.getPhrase(WELCOME_BACK_ROYAL + LAST));

        return result;
    }
}
