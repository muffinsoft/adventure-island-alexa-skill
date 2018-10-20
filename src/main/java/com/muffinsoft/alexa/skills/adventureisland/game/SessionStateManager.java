package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.content.ReplyManager;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getTurnsToNextExclamation;
import static com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager.getObstacleExplanation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getExclamation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.model.StateItem.*;

public class SessionStateManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);

    /**
     * Below are keys for session attributes. They aren't used anywhere
     * outside of the program, thus the values can be chosen freely.
     */
    static final String OBSTACLE = "obstacle";
    static final String MISSION = "mission";
    static final String LOCATION = "location";
    static final String SCENE = "scene";
    static final String STATE = "state";
    static final String STATE_INDEX = "stateIndex";
    static final String GAME_STATE = "gameState";
    static final String USERNAME = "userName";
    static final String HEALTH = "health";
    static final String COINS = "coins";
    static final String TOTAL_COINS = "totalCoins";
    static final String VISITED_LOCATIONS = "visitedLocations";
    static final String OLD_OBSTACLES = "oldObstacles";
    static final String TURNS_TO_NEXT_EXCLAMATION = "turnsToNextExclamation";

    private AttributesManager attributesManager;
    private Map<String, Object> sessionAttributes;
    private String slotName = SlotName.ACTION.text;
    private String userReply;

    private StateItem stateItem = new StateItem();
    private String userName;
    private int health;
    private int coins;
    private int totalCoins;
    private String currentObstacle;
    private int toNextExclamation;
    private boolean skipReadyPrompt;

    private List<String> visitedLocations;
    private List<String> oldObstacles;

    public SessionStateManager(Map<String, Slot> slots, AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
        logger.debug("Slot received: " + slots.get(slotName).getValue());
        this.sessionAttributes = attributesManager.getSessionAttributes();
        if (sessionAttributes == null || sessionAttributes.isEmpty()) {
            sessionAttributes = new HashMap<>();
        }
        populateFields();
        userReply = slots.get(slotName).getValue();
    }

    @SuppressWarnings("unchecked")
    private void populateFields() {
        stateItem.setMission(String.valueOf(sessionAttributes.getOrDefault(MISSION, ROOT)));
        stateItem.setLocation(String.valueOf(sessionAttributes.getOrDefault(LOCATION, ROOT)));
        stateItem.setScene(String.valueOf(sessionAttributes.getOrDefault(SCENE, ROOT)));
        stateItem.setState((State) sessionAttributes.getOrDefault(STATE, State.INTRO));
        stateItem.setIndex((int) sessionAttributes.getOrDefault(STATE_INDEX, 0));
        stateItem.setGameState((byte[]) sessionAttributes.getOrDefault(GAME_STATE, new byte[4]));

        userName = String.valueOf(sessionAttributes.get(USERNAME));
        health = (int) sessionAttributes.getOrDefault(HEALTH, getNumber(HEALTH));
        coins = (int) sessionAttributes.getOrDefault(COINS, 0);
        totalCoins = (int) sessionAttributes.getOrDefault(TOTAL_COINS, 0);
        Object obstacle = sessionAttributes.get(OBSTACLE);
        currentObstacle = obstacle != null ? String.valueOf(obstacle) : null;

        toNextExclamation = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_EXCLAMATION, getTurnsToNextExclamation());

        visitedLocations = (List<String>) sessionAttributes.getOrDefault(VISITED_LOCATIONS, new ArrayList<String>());
        oldObstacles = (List<String>) sessionAttributes.getOrDefault(OLD_OBSTACLES, new ArrayList<String>());
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public DialogItem nextResponse() {

        logger.debug("Starting to process user reply {}", userReply);
        DialogItem dialog;

        if (stateItem.getState() == State.FAILED) {
            dialog = getFailedChoice();
        } else if (stateItem.getState() == State.INTRO) {
            dialog = getIntroOutroDialog();
        } else {
            dialog = getActionDialog();
        }

        String responseText = dialog.getResponseText().replace(USERNAME_PLACEHOLDER, userName);
        dialog.setResponseText(responseText);

        updateSession();
        return dialog;
    }

    private DialogItem getFailedChoice() {
        String basicKey = State.FAILED.getKey().toLowerCase();
        if (userReply.contains(ReplyManager.getReply(basicKey + 1))) {
            stateItem.setState(State.ACTION);
            stateItem.setIndex(0);
            skipReadyPrompt = true;
            return getActionDialog();
        }
        if (userReply.contains(ReplyManager.getReply(basicKey + 2))) {
            stateItem.setMission(ROOT);
            stateItem.setLocation(ROOT);
            stateItem.setScene(ROOT);
            stateItem.setState(State.INTRO);
            stateItem.setIndex(0);
            stateItem.getGameState()[StateItem.MISSION_INDEX] = 0;
            stateItem.getGameState()[LOCATION_INDEX] = 0;
            stateItem.getGameState()[SCENE_INDEX] = 0;
            return promptForMission();
        }
        String response = getPhrase(SCENE_FAIL + REPROMPT);
        return new DialogItem(response, false, slotName, true);
    }

    private DialogItem getCoinsDialog() {
        List<String> expectedReplies = ObstacleManager.getTreasureResponses(currentObstacle);
        String speechText;
        if (expectedReplies != null && expectedReplies.contains(userReply)) {
            coins++;
            if (coins >= getNumber(COINS_TO_COLLECT)) {
                return finishScene();
            }
            String coinText = coins == 1 ? COIN_SINGLE : COIN_PLURAL;
            speechText = getPhrase(ACTION_APPROVE) + " " + getPhrase(YOU_HAVE) + " " +
                    coins + " " + getPhrase(coinText) + ".";
        } else {
            speechText = getPhrase(COIN_NOT_PICKED);
        }

        stateItem.setIndex(stateItem.getIndex() + 1);
        speechText = nextObstacle(speechText);
        return new DialogItem(speechText, false, slotName);
    }

    private DialogItem finishScene() {
        totalCoins += coins;
        coins = 0;
        stateItem.setIndex(0);
        String sceneOutro = getSceneOutro();
        getNextScene();
        DialogItem response = getIntroOutroDialog();
        response.setResponseText(combineWithBreak(sceneOutro, response.getResponseText()));
        return response;
    }

    private String getSceneOutro() {
        return getPhrase(getNameKey(State.OUTRO));
    }

    private DialogItem getActionDialog() {

        String speechText = "";

        if (currentObstacle != null) {

            toNextExclamation--;
            if (ObstacleManager.isTreasure(currentObstacle)) {
                return getCoinsDialog();
            }
            List<String> expectedReplies = ObstacleManager.getObstacleResponses(stateItem, currentObstacle);
            currentObstacle = null;
            if (expectedReplies != null && expectedReplies.contains(userReply)) {
                if (toNextExclamation <= 0) {
                    speechText = getExclamation();
                    toNextExclamation = getTurnsToNextExclamation();
                }
            } else {
                health--;
                if (health <= 0) {
                    return processSceneFail();
                }
                speechText = getPhrase(ACTION_FAIL);
            }
        } else if (skipReadyPrompt) {
            skipReadyPrompt = false;
        } else {
            return getStartConfirmation();
        }

        speechText = nextObstacle(speechText);
        stateItem.setIndex(stateItem.getIndex() + 1);

        return new DialogItem(speechText, false, slotName);
    }

    private DialogItem getStartConfirmation() {
        byte[] gameState = stateItem.getGameState();
        String responseText = "";

        // user is ready
        if (Objects.equals(userReply, ReplyManager.getReply(DEMO + 1))) {
            responseText = nextObstacle(responseText);
            stateItem.setIndex(stateItem.getIndex() + 1);
            return new DialogItem(responseText, false, slotName);
        }

        if (gameState[TIER_INDEX] == 0 && gameState[LOCATION_INDEX] == 0 && gameState[SCENE_INDEX] == 0) {
            // Lily first
            if (Objects.equals(userReply, ReplyManager.getReply(DEMO + 2))) {
                responseText += getPhrase(stateItem.getScene() + capitalizeFirstLetter(DEMO));
            } else {
                // prompt for demo round
                responseText += getPhrase(stateItem.getScene() + capitalizeFirstLetter(DEMO) + PROMPT);
                return new DialogItem(responseText, false, slotName, true);
            }
        }

        // ask if the user ready or needs help
        responseText += getPhrase(stateItem.getScene() + READY + PROMPT);

        return new DialogItem(responseText, false, slotName, true);
    }

    private DialogItem processSceneFail() {
        stateItem.setState(State.FAILED);
        stateItem.setIndex(0);
        return new DialogItem(getPhrase(SCENE_FAIL), false, slotName, true);
    }

    private DialogItem getIntroOutroDialog() {
//        if (Objects.equals(stateItem.getMission(), ROOT) && stateItem.getIndex() == 1) {
//            userName = userReply;
//            sessionAttributes.put(USERNAME, userName);
//        }

        DialogItem dialog = getResponse();

        while (isLastStep()) {
            getNextScene();
            if (stateItem.getState() != State.ACTION) {
                if (stateItem.getState() == State.OUTRO) {
                    visitedLocations.add(stateItem.getLocation());
                }
                String responseText = dialog.getResponseText();
                dialog = getResponse();
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
            } else {
                String responseText = dialog.getResponseText();
                dialog = getActionDialog();
                if (!visitedLocations.contains(stateItem.getLocation())) {
                    responseText = combineWithBreak(responseText, getObstacleExplanation(stateItem));
                }
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
                break;
            }
        }

        return dialog;
    }

    private String combineWithBreak(String responseText, String newText) {
        return responseText + " <break time=\"3s\"/> " + newText;
    }

    private boolean isLastStep() {
        String nextPhrase = getPhrase(stateItem.getScene() + stateItem.getState().getKey() + stateItem.getIndex());
        return nextPhrase == null;
    }

    private DialogItem getResponse() {
        // ROOT menu is for mission selection
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            if (userReply == null || !detectMission()) {
                return promptForMission();
            }
        }
        String nameKey = getNameKey(stateItem.getState());
        String expectedReply = ReplyManager.getReply(nameKey);
        String responseText;
        if (expectedReply != null) {
            if (Objects.equals(expectedReply, userReply)) {
                responseText = getPhrase(nameKey + YES);
            } else {
                responseText = getPhrase(nameKey + NO);
            }

        } else {
             responseText = getPhrase(nameKey);
        }
        stateItem.setIndex(stateItem.getIndex() + 1);

        DialogItem dialog = new DialogItem();
        dialog.setResponseText(responseText);
        dialog.setEnd(false);
        dialog.setSlotName(slotName);
        dialog.setRepromptRequired(stateItem.getState() == State.INTRO);

        return dialog;
    }

    private boolean detectMission() {
        List<Mission> missions = game.getMissions();
        for (int i = 0; i < missions.size(); i++) {
            Mission mission = missions.get(i);
            logger.debug("Comparing reply {} with mission name {}", userReply, mission.getName());
            String missionName = mission.getName();
            if (missionName.contains(userReply) || missionName.toLowerCase().contains(userReply)) {
                String key = PhraseManager.nameToKey(mission.getName());

                stateItem.setMission(key);
                stateItem.setLocation(key);
                stateItem.setScene(key);
                stateItem.setState(State.INTRO);
                stateItem.setIndex(0);
                stateItem.getGameState()[StateItem.MISSION_INDEX] = (byte) i;

                health = getNumber(HEALTH);

                return true;
            }
        }
        return false;
    }

    private DialogItem promptForMission() {
        StringBuilder responseText = new StringBuilder(getPhrase(SELECT_MISSION));
        List<Mission> missions = game.getMissions();
        for (Mission mission : missions) {
            responseText.append(mission.getName());
            responseText.append(". ");
        }
        return new DialogItem(responseText.toString(), false, slotName, true);
    }

    private String getNameKey(State state) {
        return stateItem.getScene() + stateItem.getIntroOutroId(state) + state.getKey() + stateItem.getIndex();
    }

    private void getNextScene() {
        stateItem = game.nextActivity(stateItem);
    }

    private void updateSession() {
        sessionAttributes.put(MISSION, stateItem.getMission());
        sessionAttributes.put(LOCATION, stateItem.getLocation());
        sessionAttributes.put(SCENE, stateItem.getScene());
        sessionAttributes.put(STATE, stateItem.getState());
        sessionAttributes.put(STATE_INDEX, stateItem.getIndex());
        sessionAttributes.put(GAME_STATE, stateItem.getGameState());

        sessionAttributes.put(OBSTACLE, currentObstacle);
        sessionAttributes.put(COINS, coins);
        sessionAttributes.put(TOTAL_COINS, totalCoins);
        sessionAttributes.put(HEALTH, health);
        sessionAttributes.put(VISITED_LOCATIONS, visitedLocations);
        sessionAttributes.put(OLD_OBSTACLES, oldObstacles);
        sessionAttributes.put(TURNS_TO_NEXT_EXCLAMATION, toNextExclamation);

        attributesManager.setSessionAttributes(sessionAttributes);
    }

    private String nextObstacle(String speechText) {
        String obstacle = game.nextObstacle(stateItem);
        if (!oldObstacles.contains(obstacle)) {
            oldObstacles.add(obstacle);
            String preObstacle = ObstacleManager.getPreObstacle(stateItem, obstacle);
            speechText += " " + preObstacle;
        }
        currentObstacle = obstacle;
        speechText += " " + capitalizeFirstLetter(obstacle) + "!";

        // handle silent scenes
        if (Objects.equals(SILENT_SCENE, stateItem.getScene())) {
            speechText += " You did not hear this.";
        }
        return speechText;
    }
}
