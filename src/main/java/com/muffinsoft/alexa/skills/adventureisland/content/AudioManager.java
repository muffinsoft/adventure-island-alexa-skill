package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.OBSTACLE_EXPLANATION;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.props;

public class AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    private static final String PATH_OBSTACLE_SOUNDS = "audio/obstacles-sound.json";
    private static final String PATH_OBSTACLE_EXPLANATIONS = "audio/obstacles-explanation.json";
    private static final String PATH_OBSTACLE_NAMES = "audio/obstacles-name.json";
    private static final String PATH_OBSTACLE_PRE = "audio/obstacles-pre.json";
    private static final String PATH_OBSTACLE_HEADSUP = "audio/obstacles-headsup.json";
    private static final String PATH_POWERUPS = "audio/powerups.json";
    private static final String PATH_INTROS = "audio/intros.json";
    private static final String PATH_PHRASES = "audio/phrases.json";

    private static List<String> obstacleSounds = new ArrayList<>();
    private static List<String> obstacleExplanations = new ArrayList<>();
    private static List<String> obstacleNames = new ArrayList<>();
    private static List<String> obstaclePre = new ArrayList<>();
    private static List<String> obstacleHeadsUp = new ArrayList<>();
    private static List<String> powerups = new ArrayList<>();
    private static List<String> intros = new ArrayList<>();
    private static List<String> phrases = new ArrayList<>();

    private static String baseAudioUrl;
    private static String soundsDir;
    private static String obstaclesSoundsDir;
    private static String explanationsDir;
    private static String obstacleNamesDir;
    private static String obstaclePreDir;
    private static String obstacleHeadsUpDir;
    private static String powerupsDir;
    private static String introsDir;
    public static String generalDir;
    public static String phrasesDir;

    private static final String extension = ".mp3";
    private static final String openTag = "<audio src=\"";
    private static final String closeTag = "\" />";

    static {
        baseAudioUrl = props.getProperty("base-storage-url") + props.getProperty("base-audio-url");
        soundsDir = props.getProperty("sounds-dir");
        obstaclesSoundsDir = props.getProperty("obstacles-dir") + props.getProperty("obstacles-sounds");
        explanationsDir = props.getProperty("obstacles-dir") + props.getProperty("obstacle-explanation");
        obstacleNamesDir = props.getProperty("obstacles-dir") + props.getProperty("obstacle-name");
        obstaclePreDir = props.getProperty("obstacles-dir") + props.getProperty("obstacle-pre");
        obstacleHeadsUpDir = props.getProperty("obstacles-dir") + props.getProperty("obstacle-headsup");
        powerupsDir = props.getProperty("powerups");
        introsDir = props.getProperty("intros-dir");
        generalDir = props.getProperty("general-dir");
        phrasesDir = props.getProperty("phrases-dir");

        obstacleSounds = Constants.contentLoader.loadContent(obstacleSounds, PATH_OBSTACLE_SOUNDS, new TypeReference<ArrayList<String>>() {});
        obstacleExplanations = Constants.contentLoader.loadContent(obstacleExplanations, PATH_OBSTACLE_EXPLANATIONS, new TypeReference<ArrayList<String>>() {});
        obstacleNames = Constants.contentLoader.loadContent(obstacleNames, PATH_OBSTACLE_NAMES, new TypeReference<ArrayList<String>>() {});
        obstaclePre = Constants.contentLoader.loadContent(obstaclePre, PATH_OBSTACLE_PRE, new TypeReference<ArrayList<String>>() {});
        obstacleHeadsUp = Constants.contentLoader.loadContent(obstacleHeadsUp, PATH_OBSTACLE_HEADSUP, new TypeReference<ArrayList<String>>() {});
        powerups = Constants.contentLoader.loadContent(powerups, PATH_POWERUPS, new TypeReference<ArrayList<String>>() {});
        intros = Constants.contentLoader.loadContent(intros, PATH_INTROS, new TypeReference<ArrayList<String>>() {});
        phrases = Constants.contentLoader.loadContent(phrases, PATH_PHRASES, new TypeReference<ArrayList<String>>() {});

        logger.debug("Loaded {} sounds", obstacleSounds.size());
    }

    public static String getObstacleSound(String key) {
        logger.debug("Looking for obstacle: {}", key);
        if (obstacleSounds.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + obstaclesSoundsDir);
        }
        return null;
    }

    public static String getObstacleExplanation(String scene, int tier) {
        String key = scene + tier + OBSTACLE_EXPLANATION;
        if (obstacleExplanations.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + explanationsDir);
        }
        return null;
    }

    public static String getObstacleName(String key) {
        logger.debug("Looking for obstacle name: {}", key);
        if (obstacleNames.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + obstacleNamesDir);
        }
        return null;
    }

    public static String getObstaclePre(String key) {
        if (obstaclePre.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + obstaclePreDir);
        }
        return null;
    }

    public static String getObstacleHeadsUp(String key) {
        if (obstacleHeadsUp.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + obstacleHeadsUpDir);
        }
        return null;
    }

    public static String getPowerup(String key) {
        if (powerups.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + powerupsDir);
        }
        return null;
    }

    public static String getSound(String key, String additionalPath) {
        return openTag + baseAudioUrl + soundsDir + additionalPath + key + extension + closeTag;
    }

    public static String getForKey(String key, String url) {
        return openTag + url + key + extension + closeTag;
    }

    public static String getPhrase(String key) {
        if (phrases.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + phrasesDir);
        }
        return null;
    }

    public static String getLocationIntro(String location) {
        String key = location + State.INTRO.getKey();
        logger.debug("Looking for location intro {}", key);
        if (intros.contains(key)) {
            return getSound(key, introsDir);
        }
        return null;
    }

    public static String getSceneTransition(String location) {
        String key = location + Constants.SCENE_TRANSITION;
        logger.debug("Looking for scene transition {}", key);
        if (intros.contains(key)) {
            return getSound(key, introsDir);
        }
        return null;
    }

}
