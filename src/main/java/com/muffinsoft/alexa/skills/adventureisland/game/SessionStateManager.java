package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager;
import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;

public class SessionStateManager {

    /**
     * Below are keys for session attributes. They aren't used anywhere
     * outside of the program, thus the values can be chosen freely.
     */
    static final String OBSTACLE = "obstacle";
    static final String MISSION = "mission";
    static final String LOCATION = "location";
    static final String SCENE = "scene";
    static final String SCENE_STATE = "sceneState";
    static final String USERNAME = "userName";
    static final String HEALTH = "health";
    static final String COINS = "coins";
    static final String VISITED_LOCATIONS = "visitedLocations";
    static final String TURNS_TO_NEXT_COIN = "turnsToNextCoin";

    private static final String COINS_OBSTACLE = "COINS!";

    private AttributesManager attributesManager;
    private Map<String, Object> sessionAttributes;
    private String slotName = SlotName.ACTION.text;
    private String userReply;

    private String mission;
    private String location;
    private String scene;
    private String userName;
    private int sceneState;
    private int health;
    private int coins;
    private int turnsToNextCoin;
    private String currentObstacle;

    private ThreadLocalRandom random = ThreadLocalRandom.current();

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
        sessionAttributes.put(SCENE_STATE, 0);
        sessionAttributes.put(TURNS_TO_NEXT_COIN, 0);
    }

    private void populateFields() {
        mission = String.valueOf(sessionAttributes.get(MISSION));
        location = String.valueOf(sessionAttributes.get(LOCATION));
        scene = String.valueOf(sessionAttributes.get(SCENE));
        userName = String.valueOf(sessionAttributes.get(USERNAME));
        sceneState = (int) sessionAttributes.get(SCENE_STATE);
        health = (int) sessionAttributes.get(HEALTH);
        coins = (int) sessionAttributes.get(COINS);
        turnsToNextCoin = (int) sessionAttributes.get(TURNS_TO_NEXT_COIN);
        Object obstacle = sessionAttributes.get(OBSTACLE);
        currentObstacle = obstacle != null ? String.valueOf(obstacle) : null;
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public DialogItem nextResponse() {
        DialogItem dialog;
        if (currentObstacle == null) {
            dialog = getIntroDialog();
        } else if (Objects.equals(currentObstacle, COINS_OBSTACLE)) {
            dialog = getCoinsDialog();
        } else {
            dialog = getActionDialog();
        }

        String responseText = dialog.getResponseText().replace(USERNAME_PLACEHOLDER, userName);
        dialog.setResponseText(responseText);

        updateSession();
        return dialog;
    }

    private DialogItem getCoinsDialog() {
        String expectedReply = ObstacleManager.getTreasureResponse();
        String speechText;
        if (Objects.equals(expectedReply, userReply)) {
            coins++;
            if (coins >= getNumber(COINS_TO_COLLECT)) {
                return new DialogItem(getPhrase(SCENE_CONFIRM), true);
            }
            speechText = getPhrase(ACTION_APPROVE);
        } else {
            speechText = getPhrase(COIN_NOT_PICKED);
        }
        setNextCoinOpportunity();
        speechText = nextObstacle(speechText);
        return new DialogItem(speechText, false, slotName);
    }

    private void setNextCoinOpportunity() {
        int minObstacles = NumbersManager.getNumber(MIN_OBSTACLES_TO_COIN);
        int maxObstacles = NumbersManager.getNumber(MAX_OBSTACLES_TO_COIN);
        turnsToNextCoin = random.nextInt(minObstacles, maxObstacles + 1);
    }

    private DialogItem getActionDialog() {
        String expectedReply = ObstacleManager.getObstacleResponse(location, currentObstacle);
        String speechText;
        if (Objects.equals(expectedReply, userReply)) {
            speechText = "";
        } else {
            health--;
            if (health <= 0) {
                return new DialogItem(getPhrase(SCENE_FAIL), true);
            }
            speechText = getPhrase(ACTION_FAIL);
        }

        turnsToNextCoin--;
        if (turnsToNextCoin <= 0) {
            speechText = nextCoin(speechText);
        } else {
            speechText = nextObstacle(speechText);
        }

        return new DialogItem(speechText, false, slotName);
    }

    private DialogItem getIntroDialog() {
        if (Objects.equals(mission, ROOT) && sceneState == 1) {
            userName = userReply;
            sessionAttributes.put(USERNAME, userName);
        }

        int maxStates = Integer.parseInt(getPhrase(scene + capitalizeFirstLetter(INTRO) + COUNT));
        if (sceneState >= maxStates) {
            if (Objects.equals(location, scene)) {
                getNextScene();
            } else {
                return getActionDialog();
            }
        }

        String responseKey = scene + capitalizeFirstLetter(INTRO) + sceneState;
        String responseText = getPhrase(responseKey);
        sceneState++;

        DialogItem dialog = new DialogItem();
        dialog.setResponseText(responseText);
        dialog.setEnd(false);
        dialog.setSlotName(slotName);
        dialog.setRepromptRequired(true);
        return dialog;
    }

    private void getNextScene() {
        String firstMission = "royalRansom";
        if (Objects.equals(mission, ROOT)) {
            mission = firstMission;
            location = firstMission;
            scene = firstMission;
        } else if (Objects.equals(location, firstMission)) {
            location = "ancientTemple";
            scene = "ancientTemple";
        } else {
            scene = "templeHalls";
        }
        sceneState = 0;
        sessionAttributes.put(MISSION, mission);
        sessionAttributes.put(LOCATION, location);
        sessionAttributes.put(SCENE, scene);
    }

    private void updateSession() {
        sessionAttributes.put(COINS, coins);
        sessionAttributes.put(HEALTH, health);
        sessionAttributes.put(TURNS_TO_NEXT_COIN, turnsToNextCoin);
        sessionAttributes.put(SCENE_STATE, sceneState);
        attributesManager.setSessionAttributes(sessionAttributes);
    }

    private String nextCoin(String speechText) {
        sessionAttributes.put(OBSTACLE, COINS_OBSTACLE);
        return speechText + " " + capitalizeFirstLetter(ObstacleManager.getTreasureName()) + "!";
    }

    private String nextObstacle(String speechText) {
        String obstacle = ObstacleManager.getObstacle(location, scene, 1);
        sessionAttributes.put(OBSTACLE, obstacle);
        speechText += " " + capitalizeFirstLetter(obstacle) + "!";
        return speechText;
    }
}
