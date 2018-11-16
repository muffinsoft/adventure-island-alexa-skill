package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.NicknameManager;
import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PowerupManager;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager.getObstacleExplanation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.ReplyManager.getReply;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.*;

public class SessionStateManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);

    private AttributesManager attributesManager;
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> persistentAttributes;
    private String slotName = SlotName.ACTION.text;
    private String userReply;
    private String replyResolution;
    private String additionalResponse;

    private StateItem stateItem;
    private PersistentState persistentState;

    private int health;
    private int coins;
    private String currentObstacle;
    private int toNextExclamation;
    private int toNextHeadsUp;
    private boolean skipReadyPrompt;
    private boolean justFailed;
    private List<String> powerups;


    public SessionStateManager(Map<String, Slot> slots, AttributesManager attributesManager) {
        this.attributesManager = attributesManager;
        this.sessionAttributes = verifyMap(attributesManager.getSessionAttributes());
        this.persistentAttributes = verifyMap(attributesManager.getPersistentAttributes());

        stateItem = new StateItem(attributesManager);
        persistentState = new PersistentState(attributesManager);

        populateFields();
        if (slots != null && !slots.isEmpty()) {
            Slot slot = slots.get(slotName);
            userReply = slot.getValue();
            replyResolution = Utils.extractSlotResolution(slot, userReply);
        }
    }

    @SuppressWarnings("unchecked")
    private void populateFields() {

        health = (int) sessionAttributes.getOrDefault(HEALTH, getNumber(HEALTH));
        coins = (int) sessionAttributes.getOrDefault(COINS, 0);
        Object obstacle = sessionAttributes.get(OBSTACLE);
        currentObstacle = obstacle != null ? String.valueOf(obstacle) : null;
        toNextExclamation = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_EXCLAMATION, getTurnsToNextExclamation());
        toNextHeadsUp = (int) sessionAttributes.getOrDefault(TURNS_TO_NEXT_HEADS_UP, getNumber(HEADS_UP));
        justFailed = sessionAttributes.get(JUST_FAILED) != null;
        powerups = (List<String>) sessionAttributes.getOrDefault(POWERUPS, new ArrayList<>());

        BigDecimal totalCoinsBD = (BigDecimal) persistentAttributes.getOrDefault(TOTAL_COINS, BigDecimal.ZERO);
        totalCoins = totalCoinsBD.intValue();
        visitedLocations = (List<String>) persistentAttributes.getOrDefault(VISITED_LOCATIONS, new ArrayList<String>());
        oldObstacles = (List<String>) persistentAttributes.getOrDefault(OLD_OBSTACLES, new ArrayList<String>());
        completedMissions = (List<List<BigDecimal>>) persistentAttributes.getOrDefault(COMPLETED_MISSIONS, new ArrayList<>());
        checkpoint = (List<BigDecimal>) persistentAttributes.get(CHECKPOINT);
        nicknames = (Map<String, List<String>>) persistentAttributes.getOrDefault(NICKNAMES, new HashMap<>());
        achievements = (Map<String, List<String>>) persistentAttributes.getOrDefault(ACHIEVEMENTS, new HashMap<>());
        hitsHistory = (Map<String, List<String>>) persistentAttributes.getOrDefault(HITS_HISTORY, new HashMap<>());
        stateItem.setLocationIntros((Map<String, List<String>>) persistentAttributes.getOrDefault(LOCATION_INTROS, new HashMap<>()));
        stateItem.setSceneIntros((Map<String, List<String>>) persistentAttributes.getOrDefault(SCENE_INTROS, new HashMap<>()));
    }

    public DialogItem nextResponse() {

        logger.debug("Starting to process user reply {}, resolved to {}, state: {}", userReply, replyResolution, stateItem.getState());
        DialogItem dialog = getDialogByState();

        String responseText = dialog.getResponseText().replace(USERNAME_PLACEHOLDER, userName);
        dialog.setResponseText(responseText);

        updateSession();
        return dialog;
    }

    private DialogItem getDialogByState() {
        DialogItem dialog;
        switch (stateItem.getState()) {
            case HELP:
                dialog = processHelp();
                break;
            case CANCEL:
                dialog = processCancel();
                break;
            case QUIT:
                dialog = processQuit();
                break;
            case RESET:
                dialog = processReset();
                break;
            case RESTART:
                dialog = processRestart();
                break;
            case CHECKPOINT:
                restoreFromCheckpoint();
                dialog = nextResponse();
                break;
            case FAILED:
                dialog = getFailedChoice();
                break;
            case INTRO:
            case OUTRO:
                dialog = getIntroOutroDialog();
                break;
            default:
                dialog = getActionDialog();
                break;
        }

        updateSession();

        dialog.setResponseText(TagProcessor.insertTags(dialog.getResponseText()));
        return dialog;
    }

    private DialogItem processRestart() {
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            return restartMission();
        } else {
            return quitToRoot();
        }
    }

    private DialogItem restartMission() {
        persistentState.setTotalCoins(0);
        coins = 0;
        currentObstacle = null;
        justFailed = false;
        powerups.clear();
        persistentState.setCheckpoint(null);
        health = getNumber(HEALTH);
        stateItem.setState(State.INTRO);
        stateItem.setLocation(stateItem.getMission());
        stateItem.setLocationIndex(0);
        stateItem.setScene(stateItem.getMission());
        stateItem.setLocationIndex(0);
        stateItem.setIndex(0);
        return nextResponse();
    }

    private DialogItem processReset() {
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            String response = getPhrase(State.RESTART.getKey().toLowerCase() + PROMPT);
            stateItem.setState(State.RESTART);
            return new DialogItem(response, false, slotName, true);
        } else {
            stateItem.setState(stateItem.getPendingState());
            return nextResponse();
        }
    }

    private DialogItem processCancel() {
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            return quitToRoot();
        } else {
            String response = getPhrase(State.QUIT.getKey().toLowerCase() + PROMPT);
            stateItem.setState(State.QUIT);
            return new DialogItem(response, false, slotName, true);
        }
    }

    private DialogItem processQuit() {
        if (Objects.equals(replyResolution, YES.toLowerCase())) {
            String response = getPhrase(STOP);
            return new DialogItem(response, true);
        } else {
            stateItem.setState(stateItem.getPendingState());
            return nextResponse();
        }
    }

    private void restoreFromCheckpoint() {
        List<BigDecimal> checkpoint = persistentState.getCheckpoint();
        if (checkpoint != null && Objects.equals(CONTINUE, userReply)) {
            stateItem.setTierIndex(checkpoint.get(0).intValue());
            stateItem.setMissionIndex(checkpoint.get(1).intValue());
            stateItem.setLocationIndex(checkpoint.get(2).intValue());
            stateItem.setSceneIndex(checkpoint.get(3).intValue());
            Mission currentMission = game.getMissions().get(stateItem.getMissionIndex());
            stateItem.setMission(nameToKey(currentMission.getName()));
            Location currentLocation = currentMission.getLocations().get(stateItem.getLocationIndex());
            stateItem.setLocation(nameToKey(currentLocation.getName()));
            stateItem.setScene(nameToKey(currentLocation.getActivities().get(stateItem.getSceneIndex()).getName()));
        }
        stateItem.setState(State.INTRO);
    }

    private DialogItem getFailedChoice() {
        String basicKey = State.FAILED.getKey().toLowerCase();
        if (userReply.contains(getReply(basicKey + 1))) {
            stateItem.setState(State.ACTION);
            stateItem.setIndex(0);
            coins = 0;
            powerups = null;
            justFailed = false;
            skipReadyPrompt = true;
            return getActionDialog();
        }
        if (userReply.contains(getReply(basicKey + 2))) {
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
        persistentState.setCheckpoint(null);
        persistentState.setTotalCoins(0);
        currentObstacle = null;
        coins = 0;
        health = getNumber(HEALTH);
        powerups.clear();
        justFailed = false;
        return MissionSelector.promptForMission(slotName, persistentState.getCompletedMissions());
    }

    private DialogItem getCoinsDialog() {
        List<String> expectedReplies = ObstacleManager.getTreasureResponses(currentObstacle);
        String speechText;
        if (expectedReplies != null && expectedReplies.contains(userReply)) {
            coins++;
            speechText = useMultiplicationPowerUp();
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

    private String useMultiplicationPowerUp() {
        String speechText = "";
        Powerup powerup = PowerupManager.useFirstRelevant(powerups, currentObstacle, MULTIPLY);
        if (powerup != null) {
            coins++;
            speechText = wrap(getPhrase(POWERUP_USED).replace(POWERUP_PLACEHOLDER, powerup.getName()));
        }
        return speechText;
    }

    private DialogItem finishScene(String speechText) {
        persistentState.addCoins(coins);
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
        Map<String, List<String>> hitsHistory = persistentState.getHitsHistory();
        logger.debug("Saving hits history to {}", hitsHistory);
        List<String> allHits = hitsHistory.getOrDefault(stateItem.getMission(), new ArrayList<>());
        allHits.add("" + hits);
        hitsHistory.put(stateItem.getMission(), allHits);
        persistentState.setHitsHistory(hitsHistory);
    }

    private void setCheckpoint() {
        int locationIndex = stateItem.getLocationIndex();

        List<Location> locations = game.getMissions().get(stateItem.getMissionIndex()).getLocations();
        List<Activity> scenes = locations.get(locationIndex).getActivities();

        int sceneIndex = stateItem.getSceneIndex() + 1;
        // location finished, proceed to next location
        if (sceneIndex >= scenes.size()) {
            sceneIndex = 0;
            locationIndex = stateItem.getLocationIndex() + 1;
            // location finished, nothing to save
            if (locationIndex >= locations.size()) {
                return;
            }
        }
        List<BigDecimal> checkpoint = new ArrayList<>();
        checkpoint.add(BigDecimal.valueOf(stateItem.getTierIndex()));
        checkpoint.add(BigDecimal.valueOf(stateItem.getMissionIndex()));
        checkpoint.add(BigDecimal.valueOf(locationIndex));
        checkpoint.add(BigDecimal.valueOf(sceneIndex));

        persistentState.setCheckpoint(checkpoint);
    }

    private String getSceneOutro() {
        String phrase = getPhrase(getNameKey(stateItem, State.OUTRO));
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
        if (userReply.contains(getReply(DEMO + 1))) {
            responseText = nextObstacle(responseText);
            stateItem.setIndex(stateItem.getIndex() + 1);
            return new DialogItem(responseText, false, slotName);
        }

        if (stateItem.getTierIndex() == 0 && stateItem.getLocationIndex() == 0 && stateItem.getSceneIndex() == 0) {
            // Lily first
            if (userReply.contains(getReply(DEMO + 2))) {
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
                    dialog = MissionSelector.promptForMission(slotName, persistentState.getCompletedMissions());
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
                    persistentState.addVisitedLocation(stateItem.getLocation());
                }
                String responseText = dialog.getResponseText();
                dialog = getResponse();
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
            } else {
                String responseText = dialog.getResponseText();
                dialog = getActionDialog();
                if (!persistentState.getVisitedLocations().contains(stateItem.getLocation())) {
                    responseText = combineWithBreak(responseText, getObstacleExplanation(stateItem));
                }
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
                break;
            }
        }

        return dialog;
    }

    private boolean isLastStep() {
        String key = getNameKey(stateItem, stateItem.getState());
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

        String nameKey = getNameKey(stateItem, stateItem.getState());
        logger.debug("Will look up the following phrase: {}", nameKey);
        String expectedReply = getReply(nameKey);
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

            int tier = MissionSelector.getTier(i, persistentState.getCompletedMissions());
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
                persistentState.setCheckpoint(null);

                return true;
            }
        }
        return false;
    }

    private void getNextScene() {
        String oldMission = stateItem.getMission();
        int oldTier = stateItem.getTierIndex();
        stateItem = game.nextActivity(stateItem);
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            updateCompletedMissions();
            persistentState.setCheckpoint(null);
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
        totalCoins = 0;
    }

    private void updateSession() {
        sessionAttributes.put(OBSTACLE, currentObstacle);
        sessionAttributes.put(COINS, coins);
        sessionAttributes.put(HEALTH, health);
        sessionAttributes.put(TURNS_TO_NEXT_HEADS_UP, toNextHeadsUp);
        sessionAttributes.put(TURNS_TO_NEXT_EXCLAMATION, toNextExclamation);
        sessionAttributes.put(JUST_FAILED, justFailed ? "yes" : null);
        sessionAttributes.put(POWERUPS, powerups);

        attributesManager.setSessionAttributes(sessionAttributes);

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
            if (!Objects.equals(SILENT_SCENE, stateItem.getScene())) {
                speechText = getPreObstacle(speechText, obstacle);
            }
        }

        currentObstacle = obstacle;
        speechText += wrap(capitalizeFirstLetter(obstacle) + "!");

        // handle silent scenes
        if (Objects.equals(SILENT_SCENE, stateItem.getScene())) {
            speechText = "<amazon:effect name=\"whispered\">" + speechText + "</amazon:effect>";
        }
        return speechText;
    }

    private String getPreObstacle(String speechText, String obstacle) {
        if (persistentState.getOldObstacles().contains(obstacle)) {
            if (--toNextHeadsUp <= 0) {
                speechText += wrap(ObstacleManager.getHeadsUp(stateItem, obstacle));
                toNextHeadsUp = getNumber(HEADS_UP);
            }
        } else {
            persistentState.addOldObstacle(obstacle);
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
            resetAction();
            updateSession();
            return new DialogItem(reply, false, null, true);
        }

        return getInMissionHelp(null);
    }

    private void resetAction() {
        stateItem.setPendingIndex(0);
        coins = 0;
        health = getNumber(HEALTH);
        powerups.clear();
        currentObstacle = null;
    }

    private DialogItem getInMissionHelp(String inSlotName) {
        String reply = wrap(getPhrase(stateItem.getMission() + HELP).replace(TOTAL_COINS_PLACEHOLDER, "" + persistentState.getTotalCoins()));
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
                if (Objects.equals(replyResolution, YES.toLowerCase())) {
                    return getInMissionHelp(slotName);
                } else {
                    return continueMission();
                }
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
