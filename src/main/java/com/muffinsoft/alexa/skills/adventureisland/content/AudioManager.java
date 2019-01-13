package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.props;

public class AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    private static final String PATH_OBSTACLES = "audio/obstacles-sound.json";
    private static final String PATH_INTROS = "audio/intros.json";
    private static final String PATH_PHRASES = "audio/phrases.json";
    private static List<String> obstacle_sounds = new ArrayList<>();
    private static List<String> intros = new ArrayList<>();
    private static List<String> phrases = new ArrayList<>();
    private static String baseAudioUrl;
    private static String soundsDir;
    private static String obstaclesDir;
    private static String introsDir;
    public static String generalDir;
    public static String phrasesDir;
    private static final String extension = ".mp3";
    private static final String openTag = "<audio src=\"";
    private static final String closeTag = "\" />";

    static {
        baseAudioUrl = props.getProperty("base-storage-url") + props.getProperty("base-audio-url");
        soundsDir = props.getProperty("sounds-dir");
        obstaclesDir = props.getProperty("obstacles-dir");
        introsDir = props.getProperty("intros-dir");
        generalDir = props.getProperty("general-dir");
        phrasesDir = props.getProperty("phrases-dir");
        obstacle_sounds = Constants.contentLoader.loadContent(obstacle_sounds, PATH_OBSTACLES, new TypeReference<ArrayList<String>>() {});
        intros = Constants.contentLoader.loadContent(intros, PATH_INTROS, new TypeReference<ArrayList<String>>() {});
        phrases = Constants.contentLoader.loadContent(phrases, PATH_PHRASES, new TypeReference<ArrayList<String>>() {});

        logger.debug("Loaded {} sounds", obstacle_sounds.size());
    }

    public static String getObstacleSound(String key) {
        logger.debug("Looking for obstacle: {}", key);
        if (obstacle_sounds.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + obstaclesDir);
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
