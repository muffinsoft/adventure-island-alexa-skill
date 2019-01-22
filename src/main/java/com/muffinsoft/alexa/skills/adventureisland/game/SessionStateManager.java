package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.muffinsoft.alexa.skills.adventureisland.content.*;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.ImageManager.getHeartsImage;
import static com.muffinsoft.alexa.skills.adventureisland.content.ImageManager.getPowerupImage;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager.getObstacleExplanation;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.ReplyManager.getReply;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.*;

public class SessionStateManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);

    private AttributesManager attributesManager;
    private String slotName = SlotName.ACTION.text;
    private String userReply;
    private SpecialReply specialReply;
    private String additionalResponse;

    private StateItem stateItem;
    private PersistentState persistentState;
    private GameProperties props;


    public SessionStateManager(String userReply, AttributesManager attributesManager, SpecialReply specialReply) {
        this.attributesManager = attributesManager;

        attributesManager.setSessionAttributes(verifyMap(attributesManager.getSessionAttributes()));
        attributesManager.setPersistentAttributes(verifyMap(attributesManager.getPersistentAttributes()));

        stateItem = new StateItem(attributesManager);
        persistentState = new PersistentState(attributesManager);
        props = new GameProperties(attributesManager);

        this.userReply = userReply;
        this.specialReply = specialReply;
    }

    public DialogItem nextResponse() {

        logger.debug("Starting to process user reply {}, state: {}, special reply: {}", userReply, stateItem.getState(), specialReply);
        DialogItem dialog = getDialogByState();

        attributesManager.savePersistentAttributes();
        logger.debug("Sending response {}", dialog.getResponseText());

        if (dialog.getBackgroundImage() == null && stateItem.getImageToInsert() != null) {
            dialog.setBackgroundImage(stateItem.getImageToInsert());
        }
        dialog.setBackgroundImage1(stateItem.getImage1ToInsert());

        insertMissionOutroImage(dialog);

        return dialog;
    }

    private void insertMissionOutroImage(DialogItem dialog) {
        if (stateItem.getState() == State.OUTRO && stateItem.getMission().equals(stateItem.getLocation())) {
            String imageToInsert = ImageManager.getMissionImageByKey(stateItem.getMission() + stateItem.getTierIndexForKey() + State.OUTRO.getKey());
            dialog.setBackgroundImage(imageToInsert);
        }
    }

    private DialogItem getDialogByState() {
        DialogItem dialog;
        switch (stateItem.getState()) {
            case HELP:
                dialog = processHelp();
                break;
            case CANCEL:
            case QUIT:
                dialog = processCancel();
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
            case WELCOME:
                dialog = getIntroOutroDialog();
                break;
            default:
                dialog = getActionDialog();
                break;
        }

        if ((stateItem.getState() == State.INTRO || stateItem.getState() == State.OUTRO)) {
            if (Utils.isLocationConnector(stateItem)) {
                logger.debug("Is location connector, will insert image");
                dialog.setBackgroundImage(ImageManager.getConnector(stateItem));
            } else if (dialog.getReprompt() == null) {
                logger.debug("Extracting reprompt");
                TagProcessor.getReprompt(dialog);
            }
        }

        return dialog;
    }

    private DialogItem processRestart() {
        if (isYes()) {
            return restartMission();
        } else {
            return quitToRoot();
        }
    }

    private DialogItem restartMission() {
        List<BigDecimal> completed = persistentState.getCompletedMissions().get(stateItem.getTierIndex());
        if (completed != null) {
            completed.remove(BigDecimal.valueOf(stateItem.getMissionIndex()));
            persistentState.setCompletedMissions(persistentState.getCompletedMissions());
        }
        props.setCoins(0);
        props.setCurrentObstacle(null);
        props.setJustFailed(false);
        props.resetPowerups();
        props.resetHealth();
        persistentState.setTotalCoins(0);
        persistentState.setCheckpoint(null);
        stateItem.setState(State.INTRO);
        stateItem.setLocation(stateItem.getMission());
        stateItem.setLocationIndex(0);
        stateItem.setScene(stateItem.getMission());
        stateItem.setLocationIndex(0);
        stateItem.setIndex(0);
        return nextResponse();
    }

    private DialogItem processReset() {
        if (isYes()) {
            return quitToRoot();
        } else {
            return goToLastAction();
        }
    }

    private DialogItem processCancel() {
        if (isYes()) {
            String response = getPhrase(STOP);
            return DialogItem.builder()
                    .responseText(response)
                    .end(true)
                    .build();
        } else {
            return goToLastAction();
        }
    }

    private DialogItem goToLastAction() {
        stateItem.setState(stateItem.getPendingState());

        if (stateItem.getState() == State.ACTION) {
            if (stateItem.getIndex() > 0) {
                stateItem.setIndex(stateItem.getIndex() - 1);
            }
            props.setCurrentObstacle(null);
            props.setSkipReadyPrompt(true);
        }

        boolean setToReprompt = false;

        if (stateItem.getState() == State.INTRO || stateItem.getState() == State.OUTRO) {
            if (stateItem.getIndex() > 0) {
                stateItem.setIndex(stateItem.getIndex() - 1);
                setToReprompt = true;
            }
        }
        DialogItem dialog = nextResponse();
        if (setToReprompt) {
            dialog.setResponseText(dialog.getReprompt());
        }
        return dialog;
    }

    private void restoreFromCheckpoint() {
        List<BigDecimal> checkpoint = persistentState.getCheckpoint();
        if (checkpoint != null && (Objects.equals(CONTINUE, userReply) || isYes())) {
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
        if (isYes()) {
            stateItem.setState(State.ACTION);
            stateItem.setIndex(0);
            props.setCoins(0);
            props.resetPowerups();
            props.resetHealth();
            props.setJustFailed(false);
            props.setSkipReadyPrompt(true);
            return getActionDialog();
        } else {
            return quitToRoot();
        }
    }

    private DialogItem quitToRoot() {
        goToIntro(ROOT);
        stateItem.setMissionIndex(0);
        stateItem.setLocationIndex(0);
        stateItem.setSceneIndex(0);
        persistentState.setCheckpoint(null);
        persistentState.setTotalCoins(0);
        props.setCurrentObstacle(null);
        props.setCoins(0);
        props.resetHealth();
        props.resetPowerups();
        props.setJustFailed(false);
        return MissionSelector.promptForMission(slotName, persistentState.getCompletedMissions());
    }

    private DialogItem getCoinsDialog() {
        List<String> expectedReplies = ObstacleManager.getTreasureResponses(props.getCurrentObstacle());
        String speechText;
        if (expectedReplies != null && expectedReplies.contains(userReply)) {
            props.addCoin();
            speechText = useMultiplicationPowerUp();
            if (props.getCoins() >= getCoinsToCollect(stateItem.getTierIndex())) {
                props.setCurrentObstacle(null);
                return finishScene(speechText);
            }
        } else {
            speechText = wrap(getPhrase(COIN_NOT_PICKED));
        }

        props.setCurrentObstacle(null);
        return nextObstacle(speechText);
    }

    private String useMultiplicationPowerUp() {
        String speechText = "";
        Powerup powerup = PowerupManager.useFirstRelevant(props, MULTIPLY);
        if (powerup != null) {
            props.addCoin();
            speechText = powerup.getUsed();
        }
        return speechText;
    }

    private DialogItem finishScene(String speechText) {
        persistentState.addCoins(props.getCoins());
        props.setCoins(0);
        stateItem.setIndex(0);

        int hits = getNumber(HEALTH) - props.getHealth();
        saveHits(hits);

        props.resetHealth();
        props.resetPowerups();
        props.setJustFailed(false);
        setCheckpoint();
        String sceneOutro = getSceneOutro();
        if (sceneOutro != null) {
            speechText = speechText + sceneOutro;
        }
        getNextScene();
        DialogItem response = getIntroOutroDialog();
        response.setResponseText(combineWithBreak(speechText, response.getResponseText()));
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
        String phrase = getPhrase(getNameKey(stateItem, State.OUTRO, persistentState));
        if (phrase != null) {
            phrase = wrap(phrase);
        }
        return phrase;
    }

    private DialogItem getActionDialog() {

        if (ObstacleManager.isTreasure(props.getCurrentObstacle())) {
            return getCoinsDialog();
        }

        String speechText = "";

        if (props.getCurrentObstacle() != null) {

            List<String> expectedReplies = ObstacleManager.getObstacleResponses(stateItem, props.getCurrentObstacle());
            // correct reply
            if (expectedReplies != null && expectedReplies.contains(userReply)) {
                speechText = getPowerup();
                if (speechText.isEmpty() && props.decrementAndGetToNextExclamation() <= 0) {
                    speechText += wrap(getExclamation());
                    props.setToNextExclamation(getTurnsToNextExclamation());
                }
                // wrong reply
            } else {
                // check if a powerup is available
                Powerup powerup = PowerupManager.useFirstRelevant(props, SKIP, RETRY);
                if (powerup != null) {
                    speechText = powerup.getUsed();
                    stateItem.setImage1ToInsert(getPowerupImage(nameToKey(powerup.getName()) + USED));
                    if (powerup.getAction().toLowerCase().contains(RETRY)) {
                        return DialogItem.builder()
                                .responseText(speechText)
                                .slotName(slotName)
                                .build();
                    }
                    // lose a heart if no powerup
                } else {
                    props.decrementHealth();
                    stateItem.setImage1ToInsert(getHeartsImage(ACTION_FAIL + props.getHealth()));
                    if (props.getHealth() <= 0) {
                        return processSceneFail();
                    }
                    props.setJustFailed(true);
                    speechText = wrap(getPhrase(ACTION_FAIL + props.getHealth()));
                }
                props.setCurrentObstacle(null);
            }
        } else if (props.isSkipReadyPrompt()) {
            props.setSkipReadyPrompt(false);
        } else {
            return getStartConfirmation();
        }

        return nextObstacle(speechText);
    }

    private String getPowerup() {
        String previous = "";
        List<String> powerUps = props.getPowerups();
        if (!powerUps.isEmpty()) {
            previous = powerUps.get(powerUps.size() - 1);
        }
        if (props.isJustFailed()) {
            Powerup powerup = PowerupManager.getPowerup(previous);
            powerUps.add(powerup.getName());
            props.setPowerups(powerUps);
            props.setJustFailed(false);
            stateItem.setImage1ToInsert(getPowerupImage(nameToKey(powerup.getName()) + GOT));
            return powerup.getGot();
        }
        return "";
    }

    private DialogItem getStartConfirmation() {
        String responseText = "";

        if (stateItem.getState() != State.READY) {
            if (isFirstScene()) {
                responseText += wrap(getPhrase(DEMO + PROMPT));
            } else {
                responseText += wrap(getPhrase(READY + PROMPT));
            }
            stateItem.setState(State.READY);
            return DialogItem.builder()
                    .responseText(responseText)
                    .slotName(slotName)
                    .reprompt(responseText)
                    .build();
        }

        if (isFirstScene()) {
            // lily first
            if (isYes()) {
                responseText += wrap(getPhrase(stateItem.getScene() + capitalizeFirstLetter(DEMO)));
            }
            // not ready without demo
        } else if (!isYes()) {
            stateItem.setState(State.ACTION);
            return initHelp();
        }

        // after demo -> action
        props.setSkipReadyPrompt(true);
        stateItem.setState(State.ACTION);
        DialogItem result = getActionDialog();
        result.setResponseText(responseText + wrap(combine(getPhrase(READY + RHETORICAL), result.getResponseText())));
        return result;
    }

    private boolean isFirstScene() {
        return stateItem.getTierIndex() == 0 && stateItem.getLocationIndex() == 0 && stateItem.getSceneIndex() == 0;
    }

    private DialogItem processSceneFail() {
        props.setCurrentObstacle(null);
        stateItem.setState(State.FAILED);
        stateItem.setIndex(0);
        return DialogItem.builder()
                .responseText(getPhrase(SCENE_FAIL))
                .slotName(slotName)
                .reprompt(getPhrase(SCENE_FAIL + REPROMPT))
                .build();
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
                    if (stateItem.getImageToInsert() != null) {
                        dialog.setBackgroundImage(stateItem.getImageToInsert());
                        dialog.setCardText("");
                    }
                    dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
                    return dialog;
                }
            } else {
                if (stateItem.getState() == State.OUTRO &&
                        !Objects.equals(stateItem.getMission(), stateItem.getLocation())) {
                    persistentState.addVisitedLocation(stateItem.getLocation());
                }
                getNextScene();
            }
            if (stateItem.getState() == State.RESTART) {
                String response = getPhrase(FINISHED);
                String prompt = getPhrase(RESTART + PROMPT);
                response = combine(response, prompt);
                return DialogItem.builder()
                        .responseText(response)
                        .reprompt(prompt)
                        .slotName(slotName)
                        .build();
            }
            if (stateItem.getState() != State.ACTION) {
                String responseText = dialog.getResponseText();
                dialog = getResponse();
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
            } else {
                String responseText = dialog.getResponseText();
                dialog = getActionDialog();
                if (!persistentState.getVisitedLocations().contains(stateItem.getLocation())) {
                    responseText = combineWithBreak(responseText, getObstacleExplanation(stateItem));
                    String imageUrl = ImageManager.getObstacleExplanation(stateItem);
                    dialog.setBackgroundImage(imageUrl);
                }
                dialog.setResponseText(combineWithBreak(responseText, dialog.getResponseText()));
                break;
            }
        }

        return dialog;
    }

    private boolean isLastStep() {
        String key = getNameKey(stateItem, stateItem.getState(), persistentState);
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

        String soundIntro = IntroSoundManager.getIntroSound(stateItem);

        String nameKey = getNameKey(stateItem, stateItem.getState(), persistentState);
        logger.debug("Will look up the following phrase: {}", nameKey);
        String expectedReply = getReply(nameKey);
        String responseText;
        String lastPhrase;
        if (expectedReply != null) {
            if (Objects.equals(expectedReply, userReply)) {
                responseText = getPhrase(nameKey + YES);
                lastPhrase = getPhraseNoAudio(nameKey + YES);
            } else {
                responseText = getPhrase(nameKey + NO);
                lastPhrase = getPhraseNoAudio(nameKey + NO);
            }

        } else {
            responseText = getPhrase(nameKey);
            lastPhrase = getPhraseNoAudio(nameKey);
        }
        if (responseText != null) {
            responseText = wrap(responseText);
        }

        responseText = combine(soundIntro, responseText);

        String imageUrl = getImageUrl(stateItem);

        stateItem.setIndex(stateItem.getIndex() + 1);

        logger.debug("Got response {}", responseText);
        logger.debug("Last phrase {}", lastPhrase);

        return DialogItem.builder()
                .responseText(responseText)
                .lastPhrase(lastPhrase)
                .slotName(slotName)
                .backgroundImage(imageUrl)
                .build();

    }

    private boolean detectMission() {
        List<Mission> missions = game.getMissions();
        List<List<BigDecimal>> completedMissions = persistentState.getCompletedMissions();
        for (int i = 0; i < missions.size(); i++) {

            int tier = MissionSelector.getTier(i, completedMissions);
            String missionName = missions.get(i).getTierNames().get(tier);
            logger.debug("Comparing reply {} with mission name {}", userReply, missionName);
            if (missionName.toLowerCase().contains(userReply)) {
                String key = PhraseManager.nameToKey(missions.get(i).getName());

                stateItem.setTierIndex(tier);
                goToIntro(key);
                stateItem.setMissionIndex(i);
                stateItem.setImageToInsert(ImageManager.getMissionImage(stateItem));

                props.resetHealth();
                persistentState.setCheckpoint(null);
                if (!completedMissions.isEmpty()) {
                    List<BigDecimal> completed = completedMissions.get(tier);
                    if (completed != null && completed.contains(BigDecimal.valueOf(i))) {
                        stateItem.setState(State.RESTART);
                    }
                }

                return true;
            }
        }
        return false;
    }

    private void goToIntro(String key) {
        stateItem.setMission(key);
        stateItem.setLocation(key);
        stateItem.setScene(key);
        stateItem.setState(State.INTRO);
        stateItem.setIndex(0);
    }

    private void getNextScene() {
        String oldMission = stateItem.getMission();
        int oldTier = stateItem.getTierIndex();
        stateItem = game.nextActivity(stateItem);
        userReply = null;
        if (Objects.equals(stateItem.getMission(), ROOT)) {
            updateCompletedMissions();
            persistentState.setCheckpoint(null);
            updateNicknames(oldMission, oldTier);
            String tier = oldTier == 0 ? "" : "" + oldTier;
            String imageToInsert = ImageManager.getMissionImageByKey(oldMission + tier + State.OUTRO.getKey());
            stateItem.setImage1ToInsert(imageToInsert);
        }
    }

    private void updateNicknames(String oldMission, int oldTier) {
        List<String> nicknamesForMission = persistentState.getNicknames().getOrDefault(oldMission, new ArrayList<>());
        String newNickname = NicknameManager.getNickname(oldMission, oldTier);
        if (!nicknamesForMission.contains(newNickname)) {
            nicknamesForMission.add(newNickname);
            persistentState.addNickname(oldMission, nicknamesForMission);
        }

        //additionalResponse = wrap(getPhrase(NICKNAME_GOT).replace(NICKNAME_PLACEHOLDER, newNickname));
    }

    private void updateCompletedMissions() {
        List<List<BigDecimal>> completedMissions = persistentState.getCompletedMissions();
        while (completedMissions.size() < stateItem.getTierIndex() + 1) {
            completedMissions.add(new ArrayList<>());
        }
        List<BigDecimal> tier = completedMissions.get(stateItem.getTierIndex());
        tier.add(BigDecimal.valueOf(stateItem.getMissionIndex()));
        persistentState.setCompletedMissions(completedMissions);
        persistentState.setTotalCoins(0);
    }

    private DialogItem nextObstacle(String speechText) {
        String obstacle = game.nextObstacle(stateItem);

        props.setCurrentObstacle(obstacle);

        Powerup powerup = null;
        if (!ObstacleManager.isTreasure(obstacle)) {
            powerup = PowerupManager.useFirstRelevant(props, REPLACE);
        }

        if (powerup != null) {
            String action = powerup.getAction().toLowerCase();
            obstacle = action.substring(action.indexOf(REPLACEMENT_PREFIX) + REPLACEMENT_PREFIX.length());
            props.setCurrentObstacle(obstacle);
            speechText += powerup.getUsed();
            stateItem.setImage1ToInsert(getPowerupImage(nameToKey(powerup.getName()) + USED));
        } else {
            logger.debug("Got obstacle {} for {} {} {}", obstacle, stateItem.getMission(), stateItem.getLocation(), stateItem.getScene());
            if (!Objects.equals(SILENT_SCENE, stateItem.getScene())) {
                speechText = getPreObstacle(speechText, obstacle);
            }
        }

        String obstacleSound = AudioManager.getObstacleSound(nameToKey(obstacle));
        speechText = combine(speechText, obstacleSound);

        String warn = ObstacleManager.getWarning(obstacle);
        String obstacleKey = SILENT_SCENE.equals(stateItem.getScene()) ? SILENT_SCENE : obstacle;
        String imageUrl = ImageManager.getObstacleImageUrl(obstacleKey);

        if (!Objects.equals(SILENT_SCENE, stateItem.getScene())) {
            speechText = combine(speechText, warn);
        } else if (obstacleSound == null) {
            speechText = combine(speechText,"<amazon:effect name=\"whispered\">" + warn + "</amazon:effect>");
        }

        stateItem.setIndex(stateItem.getIndex() + 1);

        return DialogItem.builder()
                .responseText(speechText)
                .slotName(slotName)
                .backgroundImage(imageUrl)
                .build();
    }

    private String getPreObstacle(String speechText, String obstacle) {
        if (persistentState.getOldObstacles().contains(obstacle)) {
            if (props.decrementAndGetToNextHeadsUp() <= 0) {
                speechText += wrap(ObstacleManager.getHeadsUp(stateItem, obstacle));
                props.resetToNextHeadsUp();
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
            return getActionHelpLong();
        }

        return getInMissionHelp(null);
    }

    private DialogItem getInMissionHelp(String inSlotName) {
        String reply = wrap(getPhrase(stateItem.getMission() + HELP).replace(TOTAL_COINS_PLACEHOLDER, "" + persistentState.getTotalCoins()));
        reply += wrap(getPhrase(QUIT + HELP + capitalizeFirstLetter(CONTINUE)));
        stateItem.setHelpState(HelpState.ROOT);
        attributesManager.savePersistentAttributes();
        return DialogItem.builder()
                .responseText(reply)
                .reprompt(reply)
                .slotName(inSlotName)
                .build();
    }

    private DialogItem getRootHelp() {
        String reply = wrap(getPhrase(ROOT + HELP));
        DialogItem dialog = MissionSelector.promptForMission(null, persistentState.getCompletedMissions());
        dialog.setResponseText(reply + dialog.getResponseText());
        return dialog;
    }

    private DialogItem processHelp() {
        switch (stateItem.getHelpState()) {
            case QUIT:
                return startNewMission();
            case ROOT:
                return getRootHelpOrContinue();
            case MISSION:
                if (isYes()) {
                    return getInMissionHelp(slotName);
                } else {
                    return continueMission();
                }
            default:
                throw new RuntimeException("Unexpected help state: " + stateItem.getHelpState());
        }
    }

    private DialogItem startNewMission() {
        if (isYes()) {
            return quitToRoot();
        } else {
            return continueMission();
        }
    }

    private DialogItem getRootHelpOrContinue() {
        if (isYes()) {
            return continueMission();
        } else {
            quitToRoot();
            attributesManager.savePersistentAttributes();
            return getRootHelp();
        }
    }

    private boolean isYes() {
        return specialReply == SpecialReply.YES;
    }

    private DialogItem getActionHelpLong() {
        String prefix = stateItem.getTierIndex() > 0 ? "" + stateItem.getTierIndex() : "";
        String sceneKey = stateItem.getScene();
        if (Objects.equals(stateItem.getScene(), SILENT_SCENE)) {
            sceneKey = nameToKey(game.getMissions().get(stateItem.getMissionIndex())
                    .getLocations().get(stateItem.getLocationIndex())
                    .getActivities().get(stateItem.getSceneIndex() - 1).getName());
        }
        String reply = getPhrase(sceneKey + prefix + capitalizeFirstLetter(HELP));
        stateItem.setHelpState(HelpState.MISSION);
        attributesManager.savePersistentAttributes();
        reply += wrap(getPhrase(LEARN_MORE));

        return DialogItem.builder()
                .responseText(reply)
                .reprompt(getPhrase(LEARN_MORE))
                .build();
    }

    private DialogItem continueMission() {
        State state = stateItem.getPendingState();
        state = state != null ? state : State.INTRO;
        stateItem.setState(state);
        stateItem.setPendingState(state);
        stateItem.setIndex(stateItem.getPendingIndex());
        return goToLastAction();
    }
}
