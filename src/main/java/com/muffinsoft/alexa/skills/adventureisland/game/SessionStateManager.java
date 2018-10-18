package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.*;
import com.muffinsoft.alexa.skills.adventureisland.model.*;

import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getExclamation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.nameToKey;

public class SessionStateManager {

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
    static final String USERNAME = "userName";
    static final String HEALTH = "health";
    static final String COINS = "coins";
    static final String VISITED_LOCATIONS = "visitedLocations";
    static final String OLD_OBSTACLES = "oldObstacles";

    private AttributesManager attributesManager;
    private Map<String, Object> sessionAttributes;
    private String slotName = SlotName.ACTION.text;
    private String userReply;

    private StateItem stateItem = new StateItem();
    private String userName;
    private int health;
    private int coins;
    private String currentObstacle;

    private List<String> visitedLocations;
    private List<String> oldObstacles;

    public SessionStateManager(Map<String, Slot> slots, AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
        this.sessionAttributes = attributesManager.getSessionAttributes();
        if (sessionAttributes == null || sessionAttributes.isEmpty()) {
            initializeGame();
        }
        populateFields();
        userReply = slots.get(slotName).getValue();
    }

    private void initializeGame() {
        sessionAttributes = new HashMap<>();
        sessionAttributes.put(MISSION, ROOT);
        sessionAttributes.put(LOCATION, ROOT);
        sessionAttributes.put(SCENE, ROOT);
        sessionAttributes.put(HEALTH, getNumber(HEALTH));
        sessionAttributes.put(COINS, 0);
        sessionAttributes.put(STATE, State.INTRO);
        sessionAttributes.put(STATE_INDEX, 0);
        sessionAttributes.put(VISITED_LOCATIONS, new ArrayList<String>());
        sessionAttributes.put(OLD_OBSTACLES, new ArrayList<String>());
    }

    private void populateFields() {
        stateItem.setMission(String.valueOf(sessionAttributes.get(MISSION)));
        stateItem.setLocation(String.valueOf(sessionAttributes.get(LOCATION)));
        stateItem.setScene(String.valueOf(sessionAttributes.get(SCENE)));
        stateItem.setState((State) sessionAttributes.get(STATE));
        stateItem.setIndex((int) sessionAttributes.get(STATE_INDEX));

        userName = String.valueOf(sessionAttributes.get(USERNAME));
        health = (int) sessionAttributes.get(HEALTH);
        coins = (int) sessionAttributes.get(COINS);
        Object obstacle = sessionAttributes.get(OBSTACLE);
        currentObstacle = obstacle != null ? String.valueOf(obstacle) : null;

        visitedLocations = (List<String>) sessionAttributes.get(VISITED_LOCATIONS);
        oldObstacles = (List<String>) sessionAttributes.get(OLD_OBSTACLES);
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public DialogItem nextResponse() {

        DialogItem dialog;

        if (stateItem.getState() == State.INTRO) {
            dialog = getIntroOutroDialog();
        } else {
            dialog = getActionDialog();
        }

        String responseText = dialog.getResponseText().replace(USERNAME_PLACEHOLDER, userName);
        dialog.setResponseText(responseText);

        updateSession();
        return dialog;
    }

    private DialogItem getCoinsDialog() {
        List<String> expectedReplies = ObstacleManager.getTreasureResponses(currentObstacle);
        String speechText;
        if (expectedReplies != null && expectedReplies.contains(userReply)) {
            coins++;
            if (coins >= getNumber(COINS_TO_COLLECT)) {
                String sceneOutro = getSceneOutro();
                getNextScene();
                DialogItem response = getIntroOutroDialog();
                response.setResponseText(combineWithBreak(sceneOutro, response.getResponseText()));
                return response;
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

    private String getSceneOutro() {
        return getPhrase(getNameKey(State.OUTRO.getKey()));
    }

    private DialogItem getActionDialog() {

        String speechText = "";

        if (currentObstacle != null) {
            if (ObstacleManager.isTreasure(currentObstacle)) {
                return getCoinsDialog();
            }
            List<String> expectedReplies = ObstacleManager.getObstacleResponses(stateItem, currentObstacle);
            if (expectedReplies != null && expectedReplies.contains(userReply)) {
                // TODO: random exclamation
                speechText = getExclamation();
            } else {
                health--;
                if (health <= 0) {
                    // TODO: restart scene / mission or return to main menu
                    return new DialogItem(getPhrase(SCENE_FAIL), true);
                }
                speechText = getPhrase(ACTION_FAIL);
            }
        }

        speechText = nextObstacle(speechText);
        stateItem.setIndex(stateItem.getIndex() + 1);

        return new DialogItem(speechText, false, slotName);
    }

    private DialogItem getIntroOutroDialog() {
        if (Objects.equals(stateItem.getMission(), ROOT) && stateItem.getIndex() == 1) {
            userName = userReply;
            sessionAttributes.put(USERNAME, userName);
        }

        DialogItem dialog = getResponse();

        while (isLastStep()) {
            if (Objects.equals(stateItem.getLocation(), stateItem.getScene())) {
                if (stateItem.getState() == State.OUTRO) {
                    visitedLocations.add(stateItem.getLocation());
                }
                getNextScene();
                String responseText = dialog.getResponseText();
                dialog = getResponse();
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
            } else {
                String responseText = dialog.getResponseText();
                dialog = getActionDialog();
                if (!visitedLocations.contains(stateItem.getLocation())) {
                    responseText = combineWithBreak(responseText, ObstacleManager.getObstacleExplanation(stateItem));
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
        int maxStates = Integer.parseInt(getPhrase(stateItem.getScene() + stateItem.getState().getKey() + COUNT));
        return stateItem.getIndex() >= maxStates;
    }

    private DialogItem getResponse() {
        // ROOT menu is for mission selection
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            if (userReply == null || !detectMission()) {
                return promptForMission();
            }
        }
        String nameKey = getNameKey(stateItem.getState().getKey());
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
        for (Mission mission : missions) {
            String missionName = mission.getName();
            if (missionName.contains(userReply) || missionName.toLowerCase().contains(userReply)) {
                String key = PhraseManager.nameToKey(mission.getName());

                stateItem.setMission(key);
                stateItem.setLocation(key);
                stateItem.setScene(key);
                stateItem.setState(State.INTRO);
                stateItem.setIndex(0);

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

    private String getNameKey(String key) {
        return stateItem.getScene() + key + stateItem.getIndex();
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

        sessionAttributes.put(COINS, coins);
        sessionAttributes.put(HEALTH, health);
        sessionAttributes.put(VISITED_LOCATIONS, visitedLocations);
        sessionAttributes.put(OLD_OBSTACLES, oldObstacles);
        attributesManager.setSessionAttributes(sessionAttributes);
    }

    private String nextObstacle(String speechText) {
        String obstacle = game.nextObstacle(stateItem);
        if (!oldObstacles.contains(obstacle)) {
            oldObstacles.add(obstacle);
            String preObstacle = ObstacleManager.getPreObstacle(stateItem, obstacle);
            speechText += " " + preObstacle;
        }
        sessionAttributes.put(OBSTACLE, obstacle);
        speechText += " " + capitalizeFirstLetter(obstacle) + "!";

        // handle silent scenes
        if (Objects.equals(SILENT_SCENE, stateItem.getScene())) {
            speechText += " You did not hear this.";
        }
        return speechText;
    }
}
