package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager;
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
    static final String TIER_INDEX = "tierIndex";
    static final String MISSION_INDEX = "missionIndex";
    static final String LOCATION_INDEX = "locationIndex";
    static final String SCENE_INDEX = "sceneIndex";
    static final String USERNAME = "userName";
    static final String HEALTH = "health";
    static final String COINS = "coins";
    static final String TOTAL_COINS = "totalCoins";
    static final String VISITED_LOCATIONS = "visitedLocations";
    static final String OLD_OBSTACLES = "oldObstacles";
    static final String TURNS_TO_NEXT_EXCLAMATION = "turnsToNextExclamation";
    static final String TURNS_TO_NEXT_HEADS_UP = "turnsToNextHeadsUp";
    static final String COMPLETED_MISSIONS = "completedMissions";
    static final String CHECKPOINT = "checkpoint";

    private AttributesManager attributesManager;
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> persistentAttributes;
    private String slotName = SlotName.ACTION.text;
    private String userReply;

    // SESSION attributes
    private StateItem stateItem = new StateItem();
    private int health;
    private int coins;
    private String currentObstacle;
    private int toNextExclamation;
    private int toNextHeadsUp;
    private boolean skipReadyPrompt;

    // PERSISTENT attributes
    private String userName;
    private int totalCoins;
    private List<String> visitedLocations;
    private List<String> oldObstacles;
    /**
     * Index of the external list is tier, it contains a list of indices of completed missions
     */
    private List<List<Integer>> completedMissions;
    /**
     * Checkpoint contains 4 integers: tier, mission, location, and scene
     */
    private List<Integer> checkpoint;


    public SessionStateManager(Map<String, Slot> slots, AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
        this.sessionAttributes = verifyMap(attributesManager.getSessionAttributes());
        this.persistentAttributes = verifyMap(attributesManager.getPersistentAttributes());
        populateFields();
        userReply = slots.get(slotName).getValue();
    }

    private Map<String, Object> verifyMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private void populateFields() {
        stateItem.setMission(String.valueOf(sessionAttributes.getOrDefault(MISSION, ROOT)));
        stateItem.setLocation(String.valueOf(sessionAttributes.getOrDefault(LOCATION, ROOT)));
        stateItem.setScene(String.valueOf(sessionAttributes.getOrDefault(SCENE, ROOT)));
        String stateStr = String.valueOf(sessionAttributes.getOrDefault(STATE, State.INTRO));
        stateItem.setState(State.valueOf(stateStr));
        stateItem.setIndex((int) sessionAttributes.getOrDefault(STATE_INDEX, 0));
        stateItem.setTierIndex((int) sessionAttributes.getOrDefault(TIER_INDEX, 0));
        stateItem.setMissionIndex((int) sessionAttributes.getOrDefault(MISSION_INDEX, 0));
        stateItem.setLocationIndex((int) sessionAttributes.getOrDefault(LOCATION_INDEX, 0));
        stateItem.setSceneIndex((int) sessionAttributes.getOrDefault(SCENE_INDEX, 0));
        health = (int) sessionAttributes.getOrDefault(HEALTH, getNumber(HEALTH));
        coins = (int) sessionAttributes.getOrDefault(COINS, 0);
        Object obstacle = sessionAttributes.get(OBSTACLE);
        currentObstacle = obstacle != null ? String.valueOf(obstacle) : null;
        toNextExclamation = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_EXCLAMATION, getTurnsToNextExclamation());
        toNextHeadsUp = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_HEADS_UP, getNumber(HEADS_UP));

        userName = String.valueOf(persistentAttributes.get(USERNAME));
        totalCoins = (int) persistentAttributes.getOrDefault(TOTAL_COINS, 0);
        visitedLocations = (List<String>) persistentAttributes.getOrDefault(VISITED_LOCATIONS, new ArrayList<String>());
        oldObstacles = (List<String>) persistentAttributes.getOrDefault(OLD_OBSTACLES, new ArrayList<String>());
        completedMissions = (List<List<Integer>>) persistentAttributes.getOrDefault(COMPLETED_MISSIONS, new ArrayList<>());
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public DialogItem nextResponse() {

        logger.debug("Starting to process user reply {}", userReply);
        DialogItem dialog;

        if (stateItem.getState() == State.FAILED) {
            dialog = getFailedChoice();
        } else if (stateItem.getState() != State.ACTION) {
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
            stateItem.setMissionIndex(0);
            stateItem.setLocationIndex(0);
            stateItem.setSceneIndex(0);
            checkpoint = null;
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
                currentObstacle = null;
                return finishScene();
            }
            String coinText = coins == 1 ? COIN_SINGLE : COIN_PLURAL;
            speechText = getPhrase(ACTION_APPROVE) + " " + getPhrase(YOU_HAVE) + " " +
                    coins + " " + getPhrase(coinText) + ".";
        } else {
            speechText = getPhrase(COIN_NOT_PICKED);
        }

        currentObstacle = null;
        speechText = nextObstacle(speechText);
        stateItem.setIndex(stateItem.getIndex() + 1);
        return new DialogItem(speechText, false, slotName);
    }

    private DialogItem finishScene() {
        totalCoins += coins;
        coins = 0;
        stateItem.setIndex(0);
        health = getNumber(HEALTH);
        setCheckpoint();
        String sceneOutro = getSceneOutro();
        getNextScene();
        DialogItem response = getIntroOutroDialog();
        response.setResponseText(combineWithBreak(sceneOutro, response.getResponseText()));
        return response;
    }

    private void setCheckpoint() {
        checkpoint = new ArrayList<>();
        checkpoint.add(stateItem.getTierIndex());
        checkpoint.add(stateItem.getMissionIndex());
        checkpoint.add(stateItem.getLocationIndex());
        checkpoint.add(stateItem.getSceneIndex());
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
        String responseText = "";

        // user is ready
        if (userReply.contains(ReplyManager.getReply(DEMO + 1))) {
            responseText = nextObstacle(responseText);
            stateItem.setIndex(stateItem.getIndex() + 1);
            return new DialogItem(responseText, false, slotName);
        }

        if (stateItem.getTierIndex() == 0 && stateItem.getLocationIndex() == 0 && stateItem.getSceneIndex() == 0) {
            // Lily first
            if (userReply.contains(ReplyManager.getReply(DEMO + 2))) {
                responseText += getPhrase(stateItem.getScene() + capitalizeFirstLetter(DEMO));
            } else {
                // prompt for demo round
                responseText += getPhrase(DEMO + PROMPT);
                return new DialogItem(responseText, false, slotName, true);
            }
        }

        // ask if the user ready or needs help
        responseText += getPhrase(READY + PROMPT);

        return new DialogItem(responseText, false, slotName, true);
    }

    private DialogItem processSceneFail() {
        stateItem.setState(State.FAILED);
        stateItem.setIndex(0);
        return new DialogItem(getPhrase(SCENE_FAIL), false, slotName, true);
    }

    private DialogItem getIntroOutroDialog() {
        DialogItem dialog = getResponse();

        while (isLastStep()) {
            // ROOT menu is for mission selection
            if (Objects.equals(stateItem.getMission(), ROOT)) {
                if (userReply == null || !detectMission()) {
                    String responseText = dialog.getResponseText();
                    dialog = promptForMission();
                    dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
                    return dialog;
                }
            } else {
                getNextScene();
            }
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
        if (responseText != null) {
            return responseText + " <break time=\"3s\"/> " + newText;
        }
        return newText;
    }

    private boolean isLastStep() {
        String key = getNameKey(stateItem.getState());
        String nextPhrase = getPhrase(key);
        boolean result = nextPhrase == null;
        if (result) {
            key = key + YES;
            nextPhrase = getPhrase(key);
        }
        result = nextPhrase == null;
        logger.debug("Checking if {} is the last phrase: {}", key, result);
        return result;
    }

    private DialogItem getResponse() {

        String nameKey = getNameKey(stateItem.getState());
        String expectedReply = ReplyManager.getReply(nameKey);
        String responseText;
        if (expectedReply != null) {
            if (Objects.equals(expectedReply, NAME)) {
                logger.debug("Saving {} as user name", userReply);
                userName = userReply;
                responseText = getPhrase(nameKey);
            } else if (Objects.equals(expectedReply, userReply)) {
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

            int tier = getTier(i);
            String missionName = missions.get(i).getTierNames().get(tier);
            logger.debug("Comparing reply {} with mission name {}", userReply, missionName);
            if (Objects.equals(missionName.toLowerCase(), userReply)) {
                String key = PhraseManager.nameToKey(missionName);

                stateItem.setTierIndex(tier);
                stateItem.setMission(key);
                stateItem.setLocation(key);
                stateItem.setScene(key);
                stateItem.setState(State.INTRO);
                stateItem.setIndex(0);
                stateItem.setMissionIndex(i);

                health = getNumber(HEALTH);

                return true;
            }
        }
        return false;
    }

    private DialogItem promptForMission() {
        StringBuilder responseText = new StringBuilder(getPhrase(SELECT_MISSION));
        List<Mission> missions = game.getMissions();
        for (int i = 0; i < missions.size(); i++) {
            int tier = getTier(i);
            responseText.append(missions.get(i).getTierNames().get(tier));
            responseText.append(". ");
        }
        return new DialogItem(responseText.toString(), false, slotName, true);
    }

    private int getTier(int missionIndex) {
        int result = 0;
        for (int i = (completedMissions.size() - 1); i >= 0; i--) {
            if (completedMissions.get(i).contains(missionIndex)) {
                result = i + 1;
                break;
            }
        }
        return result;
    }

    private String getNameKey(State state) {
        String prefix = "";
        if (Objects.equals(stateItem.getScene(), SILENT_SCENE)) {
            prefix = stateItem.getLocation();
        }

        String scene = stateItem.getScene();
        scene = "".equals(prefix) ? scene : capitalizeFirstLetter(scene);

        return prefix + scene + stateItem.getIntroOutroId(state) + state.getKey() + stateItem.getIndex();
    }

    private void getNextScene() {
        stateItem = game.nextActivity(stateItem);
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            updateCompletedMissions();
        }
    }

    private void updateCompletedMissions() {
        while (completedMissions.size() < stateItem.getMissionIndex() + 1) {
            completedMissions.add(new ArrayList<>());
        }
        List<Integer> tier = completedMissions.get(stateItem.getMissionIndex());
        tier.add(stateItem.getMissionIndex());

    }

    private void updateSession() {
        sessionAttributes.put(MISSION, stateItem.getMission());
        sessionAttributes.put(LOCATION, stateItem.getLocation());
        sessionAttributes.put(SCENE, stateItem.getScene());
        sessionAttributes.put(STATE, stateItem.getState());
        sessionAttributes.put(STATE_INDEX, stateItem.getIndex());
        sessionAttributes.put(TIER_INDEX, stateItem.getTierIndex());
        sessionAttributes.put(MISSION_INDEX, stateItem.getMissionIndex());
        sessionAttributes.put(LOCATION_INDEX, stateItem.getLocationIndex());
        sessionAttributes.put(SCENE_INDEX, stateItem.getSceneIndex());

        sessionAttributes.put(OBSTACLE, currentObstacle);
        sessionAttributes.put(COINS, coins);
        sessionAttributes.put(HEALTH, health);
        sessionAttributes.put(TURNS_TO_NEXT_HEADS_UP, toNextHeadsUp);
        sessionAttributes.put(TURNS_TO_NEXT_EXCLAMATION, toNextExclamation);

        attributesManager.setSessionAttributes(sessionAttributes);

        persistentAttributes.put(TOTAL_COINS, totalCoins);
        persistentAttributes.put(VISITED_LOCATIONS, visitedLocations);
        persistentAttributes.put(OLD_OBSTACLES, oldObstacles);
        persistentAttributes.put(USERNAME, userName);
        persistentAttributes.put(CHECKPOINT, checkpoint);
        persistentAttributes.put(COMPLETED_MISSIONS, completedMissions);

        attributesManager.setPersistentAttributes(persistentAttributes);
        attributesManager.savePersistentAttributes();
    }

    private String nextObstacle(String speechText) {
        String obstacle = game.nextObstacle(stateItem);
        logger.debug("Got obstacle {} for {} {} {}", obstacle, stateItem.getMission(), stateItem.getLocation(), stateItem.getScene());
        if (oldObstacles.contains(obstacle)) {
            if (toNextHeadsUp-- <= 0) {
                speechText += " " + ObstacleManager.getHeadsUp(stateItem, obstacle);
                toNextHeadsUp = getNumber(HEADS_UP);
            }
        } else {
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
