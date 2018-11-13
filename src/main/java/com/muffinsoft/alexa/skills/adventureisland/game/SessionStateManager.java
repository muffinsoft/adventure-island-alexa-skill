package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.*;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getCoinsToCollect;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getTurnsToNextExclamation;
import static com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager.getObstacleExplanation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getExclamation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.nameToKey;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.combineWithBreak;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.wrap;

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
    static final String PENDING_STATE = "pendingState";
    static final String STATE_INDEX = "stateIndex";
    static final String PENDING_INDEX = "pendingIndex";
    static final String TIER_INDEX = "tierIndex";
    static final String MISSION_INDEX = "missionIndex";
    static final String LOCATION_INDEX = "locationIndex";
    static final String SCENE_INDEX = "sceneIndex";
    public static final String USERNAME = "userName";
    static final String HEALTH = "health";
    static final String COINS = "coins";
    static final String TOTAL_COINS = "totalCoins";
    static final String VISITED_LOCATIONS = "visitedLocations";
    static final String OLD_OBSTACLES = "oldObstacles";
    static final String TURNS_TO_NEXT_EXCLAMATION = "turnsToNextExclamation";
    static final String TURNS_TO_NEXT_HEADS_UP = "turnsToNextHeadsUp";
    static final String COMPLETED_MISSIONS = "completedMissions";
    public static final String CHECKPOINT = "checkpoint";
    static final String JUST_FAILED = "justFailed";
    static final String POWERUPS = "powerups";
    static final String NICKNAMES = "nicknames";
    static final String ACHIEVEMENTS = "achievements";
    static final String HITS_HISTORY = "hitsHistory";
    static final String HELP_STATE = "helpState";

    private AttributesManager attributesManager;
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> persistentAttributes;
    private String slotName = SlotName.ACTION.text;
    private String userReply;
    private String replyResolution;
    private String additionalResponse;

    // SESSION attributes
    private StateItem stateItem = new StateItem();
    private int health;
    private int coins;
    private String currentObstacle;
    private int toNextExclamation;
    private int toNextHeadsUp;
    private boolean skipReadyPrompt;
    private boolean justFailed;
    private List<String> powerups;

    // PERSISTENT attributes
    private String userName;
    private int totalCoins;
    private List<String> visitedLocations;
    private List<String> oldObstacles;
    /**
     * Index of the external list is tier, it contains a list of indices of completed missions
     */
    private List<List<BigDecimal>> completedMissions;
    /**
     * Checkpoint contains 4 integers: tier, mission, location, and last successful scene
     */
    private List<BigDecimal> checkpoint;

    /**
     * Earned nicknames are mapped to mission name keys (missionName).
     */
    private Map<String, List<String>> nicknames;
    private Map<String, List<String>> achievements;
    /**
     * How many times the user was hit in the mission, contains 36 digits as String (4*3 per mission * 3 tiers)
     */
    private Map<String, List<String>> hitsHistory;


    public SessionStateManager(Map<String, Slot> slots, AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
        this.sessionAttributes = verifyMap(attributesManager.getSessionAttributes());
        this.persistentAttributes = verifyMap(attributesManager.getPersistentAttributes());
        populateFields();
        if (slots != null && !slots.isEmpty()) {
            Slot slot = slots.get(slotName);
            userReply = slot.getValue();
            replyResolution = Utils.extractSlotResolution(slot, userReply);
        }
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
        stateStr = String.valueOf(sessionAttributes.get(PENDING_STATE));
        if (stateStr != null && !Objects.equals("null", stateStr)) {
            stateItem.setPendingState(State.valueOf(stateStr));
        }
        stateItem.setIndex((int) sessionAttributes.getOrDefault(STATE_INDEX, 0));
        stateItem.setTierIndex((int) sessionAttributes.getOrDefault(TIER_INDEX, 0));
        stateItem.setMissionIndex((int) sessionAttributes.getOrDefault(MISSION_INDEX, 0));
        stateItem.setLocationIndex((int) sessionAttributes.getOrDefault(LOCATION_INDEX, 0));
        stateItem.setSceneIndex((int) sessionAttributes.getOrDefault(SCENE_INDEX, 0));
        stateItem.setPendingIndex((int) sessionAttributes.getOrDefault(PENDING_INDEX, 0));
        health = (int) sessionAttributes.getOrDefault(HEALTH, getNumber(HEALTH));
        coins = (int) sessionAttributes.getOrDefault(COINS, 0);
        Object obstacle = sessionAttributes.get(OBSTACLE);
        currentObstacle = obstacle != null ? String.valueOf(obstacle) : null;
        toNextExclamation = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_EXCLAMATION, getTurnsToNextExclamation());
        toNextHeadsUp = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_HEADS_UP, getNumber(HEADS_UP));
        justFailed = sessionAttributes.get(JUST_FAILED) != null;
        powerups = (List<String>) sessionAttributes.getOrDefault(POWERUPS, new ArrayList<>());

        stateStr = String.valueOf(sessionAttributes.get(HELP_STATE));
        if (stateStr != null && !Objects.equals("null", stateStr)) {
            stateItem.setHelpState(HelpState.valueOf(stateStr));
        }

        userName = String.valueOf(persistentAttributes.getOrDefault(USERNAME, "my friend"));
        BigDecimal totalCoinsBD = (BigDecimal) persistentAttributes.getOrDefault(TOTAL_COINS, BigDecimal.ZERO);
        totalCoins = totalCoinsBD.intValue();
        visitedLocations = (List<String>) persistentAttributes.getOrDefault(VISITED_LOCATIONS, new ArrayList<String>());
        oldObstacles = (List<String>) persistentAttributes.getOrDefault(OLD_OBSTACLES, new ArrayList<String>());
        completedMissions = (List<List<BigDecimal>>) persistentAttributes.getOrDefault(COMPLETED_MISSIONS, new ArrayList<>());
        checkpoint = (List<BigDecimal>) persistentAttributes.get(CHECKPOINT);
        nicknames = (Map<String, List<String>>) persistentAttributes.get(NICKNAMES);
        achievements = (Map<String, List<String>>) persistentAttributes.get(ACHIEVEMENTS);
        hitsHistory = (Map<String, List<String>>) persistentAttributes.get(HITS_HISTORY);
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public DialogItem nextResponse() {

        logger.debug("Starting to process user reply {}, resolved to {}, state: {}", userReply, replyResolution, stateItem.getState());
        DialogItem dialog;

        if (stateItem.getState() == State.HELP) {
            dialog = processHelp();
        } else {
            if (checkpoint != null && Objects.equals(CONTINUE, userReply)) {
                restoreFromCheckpoint();
            }

            if (stateItem.getState() == State.FAILED) {
                dialog = getFailedChoice();
            } else if (stateItem.getState() != State.ACTION) {
                dialog = getIntroOutroDialog();
            } else {
                dialog = getActionDialog();
            }

            String responseText = dialog.getResponseText().replace(USERNAME_PLACEHOLDER, userName);
            dialog.setResponseText(responseText);
        }

        updateSession();
        return dialog;
    }

    private void restoreFromCheckpoint() {
        stateItem.setTierIndex(checkpoint.get(0).intValue());
        stateItem.setMissionIndex(checkpoint.get(1).intValue());
        stateItem.setLocationIndex(checkpoint.get(2).intValue());
        stateItem.setSceneIndex(checkpoint.get(3).intValue() + 1); // proceed to the next scene
        Mission currentMission = game.getMissions().get(stateItem.getMissionIndex());
        stateItem.setMission(nameToKey(currentMission.getName()));
        Location currentLocation = currentMission.getLocations().get(stateItem.getLocationIndex());
        stateItem.setLocation(nameToKey(currentLocation.getName()));
        stateItem.setScene(nameToKey(currentLocation.getActivities().get(stateItem.getSceneIndex()).getName()));
    }

    private DialogItem getFailedChoice() {
        String basicKey = State.FAILED.getKey().toLowerCase();
        if (userReply.contains(ReplyManager.getReply(basicKey + 1))) {
            stateItem.setState(State.ACTION);
            stateItem.setIndex(0);
            coins = 0;
            powerups = null;
            justFailed = false;
            skipReadyPrompt = true;
            return getActionDialog();
        }
        if (userReply.contains(ReplyManager.getReply(basicKey + 2))) {
            return quitToRoot();
        }
        String response = getPhrase(SCENE_FAIL + REPROMPT);
        return new DialogItem(response, false, slotName, true);
    }

    private DialogItem quitToRoot() {
        stateItem.setMission(ROOT);
        stateItem.setLocation(ROOT);
        stateItem.setScene(ROOT);
        stateItem.setState(State.INTRO);
        stateItem.setIndex(0);
        stateItem.setMissionIndex(0);
        stateItem.setLocationIndex(0);
        stateItem.setSceneIndex(0);
        checkpoint = null;
        return MissionSelector.promptForMission(slotName, completedMissions);
    }

    private DialogItem getCoinsDialog() {
        List<String> expectedReplies = ObstacleManager.getTreasureResponses(currentObstacle);
        String speechText = "";
        if (expectedReplies != null && expectedReplies.contains(userReply)) {
            coins++;
            Powerup powerup = PowerupManager.useFirstRelevant(powerups, currentObstacle, MULTIPLY);
            if (powerup != null) {
                coins++;
                speechText = wrap(getPhrase(POWERUP_USED).replace(POWERUP_PLACEHOLDER, powerup.getName()));
            }
            if (coins >= getCoinsToCollect(stateItem.getTierIndex())) {
                currentObstacle = null;
                return finishScene(speechText);
            }
            String coinText = coins == 1 ? COIN_SINGLE : COIN_PLURAL;
            speechText += wrap(getPhrase(ACTION_APPROVE) + " " + getPhrase(YOU_HAVE) + " " +
                    coins + " " + getPhrase(coinText) + ".");
        } else {
            speechText = wrap(getPhrase(COIN_NOT_PICKED));
        }

        currentObstacle = null;
        speechText = nextObstacle(speechText);
        stateItem.setIndex(stateItem.getIndex() + 1);
        return new DialogItem(speechText, false, slotName);
    }

    private DialogItem finishScene(String speechText) {
        totalCoins += coins;
        coins = 0;
        stateItem.setIndex(0);

        int hits = getNumber(HEALTH) - health;
        saveHits(hits);

        health = getNumber(HEALTH);
        powerups.clear();
        justFailed = false;
        setCheckpoint();
        String sceneOutro = getSceneOutro();
        if (sceneOutro != null) {
            sceneOutro = speechText + sceneOutro;
        }
        getNextScene();
        DialogItem response = getIntroOutroDialog();
        response.setResponseText(combineWithBreak(sceneOutro, response.getResponseText()));
        return response;
    }

    private void saveHits(int hits) {
        List<String> allHits = hitsHistory.getOrDefault(stateItem.getMission(), new ArrayList<>());
        allHits.add("" + hits);
        hitsHistory.put(stateItem.getMission(), allHits);
    }

    private void setCheckpoint() {
        checkpoint = new ArrayList<>();
        checkpoint.add(BigDecimal.valueOf(stateItem.getTierIndex()));
        checkpoint.add(BigDecimal.valueOf(stateItem.getMissionIndex()));
        checkpoint.add(BigDecimal.valueOf(stateItem.getLocationIndex()));
        checkpoint.add(BigDecimal.valueOf(stateItem.getSceneIndex()));
    }

    private String getSceneOutro() {
        String phrase = getPhrase(getNameKey(State.OUTRO));
        if (phrase != null) {
            phrase = wrap(phrase);
        }
        return phrase;
    }

    private DialogItem getActionDialog() {

        if (ObstacleManager.isTreasure(currentObstacle)) {
            return getCoinsDialog();
        }

        String speechText = "";

        if (currentObstacle != null) {

            List<String> expectedReplies = ObstacleManager.getObstacleResponses(stateItem, currentObstacle);
            // correct reply
            if (expectedReplies != null && expectedReplies.contains(userReply)) {
                speechText = getPowerup();
                if (speechText.isEmpty() && --toNextExclamation <= 0) {
                    speechText += wrap(getExclamation());
                    toNextExclamation = getTurnsToNextExclamation();
                }
            // wrong reply
            } else {
                // check if a powerup is available
                Powerup powerup = PowerupManager.useFirstRelevant(powerups, currentObstacle, SKIP, RETRY);
                if (powerup != null) {
                    speechText = wrap(getPhrase(POWERUP_USED).replace(POWERUP_PLACEHOLDER, powerup.getName()));
                    if (powerup.getAction().toLowerCase().contains(RETRY)) {
                        return new DialogItem(speechText, false, slotName);
                    }
                // lose a heart if no powerup
                } else {
                    health--;
                    if (health <= 0) {
                        return processSceneFail();
                    }
                    justFailed = true;
                    speechText = wrap(getPhrase(ACTION_FAIL + health));
                }
                currentObstacle = null;
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

    private String getPowerup() {
        String previous = "";
        if (!powerups.isEmpty()) {
            previous = powerups.get(powerups.size() - 1);
        }
        if (justFailed) {
            Powerup powerup = PowerupManager.getPowerup(previous);
            powerups.add(powerup.getName());
            justFailed = false;
            return wrap(getPhrase(POWERUP_GOT).replace(POWERUP_PLACEHOLDER, powerup.getName()) +
                    " " + powerup.getExplanation());
        }
        return "";
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
                responseText += wrap(getPhrase(stateItem.getScene() + capitalizeFirstLetter(DEMO)));
            } else {
                // prompt for demo round
                responseText += wrap(getPhrase(DEMO + PROMPT));
                return new DialogItem(responseText, false, slotName, true);
            }
        }

        // ask if the user ready or needs help
        responseText += wrap(getPhrase(READY + PROMPT));

        return new DialogItem(responseText, false, slotName, true);
    }

    private DialogItem processSceneFail() {
        currentObstacle = null;
        stateItem.setState(State.FAILED);
        stateItem.setIndex(0);
        return new DialogItem(getPhrase(SCENE_FAIL), false, slotName, true);
    }

    // TODO: skip welcome intro for returning users
    private DialogItem getIntroOutroDialog() {
        DialogItem dialog = getResponse();

        while (isLastStep()) {
            // ROOT menu is for mission selection
            if (Objects.equals(stateItem.getMission(), ROOT)) {
                if (userReply == null || !detectMission()) {
                    String responseText = dialog.getResponseText();
                    // after all outros, before mission prompt, insert additional response (like nicknames earned)
                    if (additionalResponse != null && !additionalResponse.isEmpty()) {
                        if (responseText != null) {
                            responseText += " " + additionalResponse;
                        } else {
                            responseText = additionalResponse;
                        }
                        additionalResponse = null;
                    }
                    dialog = MissionSelector.promptForMission(slotName, completedMissions);
                    if (responseText != null) {
                        dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
                    }
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
        logger.debug("Will look up the following phrase: {}", nameKey);
        String expectedReply = ReplyManager.getReply(nameKey);
        String responseText;
        if (expectedReply != null) {
            if (Objects.equals(expectedReply, userReply)) {
                responseText = wrap(getPhrase(nameKey + YES));
            } else {
                responseText = wrap(getPhrase(nameKey + NO));
            }

        } else {
            responseText = getPhrase(nameKey);
            if (responseText != null) {
                responseText = wrap(responseText);
            }
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

            int tier = MissionSelector.getTier(i, completedMissions);
            String missionName = missions.get(i).getTierNames().get(tier);
            logger.debug("Comparing reply {} with mission name {}", userReply, missionName);
            if (Objects.equals(missionName.toLowerCase(), userReply)) {
                String key = PhraseManager.nameToKey(missions.get(i).getName());

                stateItem.setTierIndex(tier);
                stateItem.setMission(key);
                stateItem.setLocation(key);
                stateItem.setScene(key);
                stateItem.setState(State.INTRO);
                stateItem.setIndex(0);
                stateItem.setMissionIndex(i);

                health = getNumber(HEALTH);
                checkpoint = null;

                return true;
            }
        }
        return false;
    }

    private String getNameKey(State state) {
        String prefix = "";
        if (Objects.equals(stateItem.getScene(), SILENT_SCENE)) {
            prefix = stateItem.getLocation();
        } else if (stateItem.getState() == State.OUTRO && Objects.equals(stateItem.getScene(), stateItem.getLocation())) {
            prefix = stateItem.getMission();
        }

        String scene = stateItem.getScene();
        scene = "".equals(prefix) ? scene : capitalizeFirstLetter(scene);

        return prefix + scene + stateItem.getIntroOutroId(state) + state.getKey() + stateItem.getIndex();
    }

    private void getNextScene() {
        String oldMission = stateItem.getMission();
        int oldTier = stateItem.getTierIndex();
        stateItem = game.nextActivity(stateItem);
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            updateCompletedMissions();
            checkpoint = null;
            updateNicknames(oldMission, oldTier);
        }
    }

    private void updateNicknames(String oldMission, int oldTier) {
        List<String> nicknamesForMission = nicknames.getOrDefault(oldMission, new ArrayList<>());
        String newNickname = NicknameManager.getNickname(oldMission, oldTier);
        nicknamesForMission.add(newNickname);
        nicknames.put(oldMission, nicknamesForMission);

        additionalResponse = wrap(getPhrase(NICKNAME_GOT).replace(NICKNAME_PLACEHOLDER, newNickname));
    }

    private void updateCompletedMissions() {
        while (completedMissions.size() < stateItem.getTierIndex() + 1) {
            completedMissions.add(new ArrayList<>());
        }
        List<BigDecimal> tier = completedMissions.get(stateItem.getTierIndex());
        tier.add(BigDecimal.valueOf(stateItem.getMissionIndex()));

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
        sessionAttributes.put(PENDING_STATE, stateItem.getPendingState());
        sessionAttributes.put(PENDING_INDEX, stateItem.getPendingIndex());
        sessionAttributes.put(HELP_STATE, stateItem.getHelpState());

        sessionAttributes.put(OBSTACLE, currentObstacle);
        sessionAttributes.put(COINS, coins);
        sessionAttributes.put(HEALTH, health);
        sessionAttributes.put(TURNS_TO_NEXT_HEADS_UP, toNextHeadsUp);
        sessionAttributes.put(TURNS_TO_NEXT_EXCLAMATION, toNextExclamation);
        sessionAttributes.put(JUST_FAILED, justFailed ? "yes" : null);
        sessionAttributes.put(POWERUPS, powerups);

        attributesManager.setSessionAttributes(sessionAttributes);

        persistentAttributes.put(TOTAL_COINS, totalCoins);
        persistentAttributes.put(VISITED_LOCATIONS, visitedLocations);
        persistentAttributes.put(OLD_OBSTACLES, oldObstacles);
        persistentAttributes.put(USERNAME, userName);
        persistentAttributes.put(CHECKPOINT, checkpoint);
        persistentAttributes.put(COMPLETED_MISSIONS, completedMissions);
        persistentAttributes.put(NICKNAMES, nicknames);
        persistentAttributes.put(ACHIEVEMENTS, achievements);
        persistentAttributes.put(HITS_HISTORY, hitsHistory);

        attributesManager.setPersistentAttributes(persistentAttributes);
        attributesManager.savePersistentAttributes();
    }

    private String nextObstacle(String speechText) {
        String obstacle = game.nextObstacle(stateItem);

        Powerup powerup = PowerupManager.useFirstRelevant(powerups, obstacle, REPLACE);
        if (powerup != null) {
            String action = powerup.getAction().toLowerCase();
            obstacle = action.substring(action.indexOf(REPLACEMENT_PREFIX) + REPLACEMENT_PREFIX.length());
            speechText += wrap(getPhrase(POWERUP_USED).replace(POWERUP_PLACEHOLDER, powerup.getName()));
        } else {
            logger.debug("Got obstacle {} for {} {} {}", obstacle, stateItem.getMission(), stateItem.getLocation(), stateItem.getScene());
            speechText = getPreObstacle(speechText, obstacle);
        }

        currentObstacle = obstacle;
        speechText += wrap(capitalizeFirstLetter(obstacle) + "!");

        // handle silent scenes
        if (Objects.equals(SILENT_SCENE, stateItem.getScene())) {
            speechText += wrap("<amazon:effect name=\"whispered\">You did not hear this.</amazon:effect>");
        }
        return speechText;
    }

    private String getPreObstacle(String speechText, String obstacle) {
        if (oldObstacles.contains(obstacle)) {
            if (--toNextHeadsUp <= 0) {
                speechText += wrap(ObstacleManager.getHeadsUp(stateItem, obstacle));
                toNextHeadsUp = getNumber(HEADS_UP);
            }
        } else {
            oldObstacles.add(obstacle);
            String preObstacle = ObstacleManager.getPreObstacle(stateItem, obstacle);
            speechText += wrap(preObstacle);
        }
        return speechText;
    }

    public DialogItem initHelp() {
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            return getRootHelp();
        }

        stateItem.setPendingState(stateItem.getState());
        stateItem.setState(State.HELP);
        stateItem.setPendingIndex(stateItem.getIndex());
        stateItem.setIndex(0);

        if (stateItem.getPendingState() == State.ACTION) {
            String reply = wrap(getPhrase(State.ACTION.getKey().toLowerCase() + HELP));
            stateItem.setHelpState(HelpState.ACTION_SHORT);
            updateSession();
            return new DialogItem(reply, false, null, true);
        }

        return getInMissionHelp(null);
    }

    private DialogItem getInMissionHelp(String inSlotName) {
        String reply = wrap(getPhrase(stateItem.getMission() + HELP).replace(TOTAL_COINS_PLACEHOLDER, "" + totalCoins));
        reply += wrap(getPhrase(QUIT + HELP + capitalizeFirstLetter(CONTINUE)));
        stateItem.setHelpState(HelpState.ROOT);
        updateSession();
        return new DialogItem(reply, false, inSlotName, true);
    }

    private DialogItem getRootHelp() {
        String reply = wrap(getPhrase(ROOT + HELP));
        return new DialogItem(reply, false, null, true);
    }

    private DialogItem processHelp() {
        switch (stateItem.getHelpState()) {
            case QUIT:
                return startNewMission();
            case ROOT:
                return getRootHelpOrContinue();
            case MISSION:
                return getInMissionHelp(slotName);
            case ACTION_SHORT:
                return getActionHelpShort();
            case ACTION_LONG:
                return getActionHelpLong();
            default:
                throw new RuntimeException("Unexpected help state: " + stateItem.getHelpState());
        }
    }

    private DialogItem startNewMission() {
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            return quitToRoot();
        } else {
            return continueMission();
        }
    }

    private DialogItem getRootHelpOrContinue() {
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            return continueMission();
        } else {
            stateItem.setHelpState(HelpState.QUIT);
            updateSession();
            return getRootHelp();
        }
    }

    private DialogItem getActionHelpShort() {
        String prefix = stateItem.getTierIndex() > 0 ? "" + stateItem.getTierIndex() : "";
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            String reply = getPhrase(stateItem.getScene() + prefix + HELP);
            if (stateItem.getSceneIndex() != 0) {
                stateItem.setHelpState(HelpState.ACTION_LONG);
                reply += wrap(getPhrase(FULL_HELP));
            } else {
                stateItem.setHelpState(HelpState.MISSION);
                reply += wrap(getPhrase(LEARN_MORE));
            }
            updateSession();
            return new DialogItem(reply, false, slotName, true);
        } else {
            return getInMissionHelp(slotName);
        }
    }

    private DialogItem getActionHelpLong() {
        String prefix = stateItem.getTierIndex() > 0 ? "" + stateItem.getTierIndex() : "";
        String reply = "";
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            reply = getPhrase(stateItem.getScene() + prefix + capitalizeFirstLetter(FULL_HELP));
        }
        stateItem.setHelpState(HelpState.MISSION);
        updateSession();
        reply += wrap(getPhrase(LEARN_MORE));
        return new DialogItem(reply, false, slotName, true);
    }

    private DialogItem continueMission() {
        State state = stateItem.getPendingState();
        state = state != null ? state : State.INTRO;
        stateItem.setState(state);
        stateItem.setIndex(stateItem.getPendingIndex());
        return nextResponse();
    }
}
