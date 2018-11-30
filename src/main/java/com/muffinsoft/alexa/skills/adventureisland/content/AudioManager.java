package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    private static final String PATH_OBSTACLES = "audio/obstacles-sound.json";
    private static List<String> obstacle_sounds = new ArrayList<>();
    private static String baseAudioUrl;
    private static String soundsDir;
    private static String obstaclesDir;
    private static final String extension = ".mp3";
    private static final String openTag = "<audio src=\"";
    private static final String closeTag = "\" />";

    static {
        Properties props = new Properties();
        try {
            props.load(AudioManager.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            logger.error("Exception caught", e);
        }
        baseAudioUrl = props.getProperty("base-audio-url");
        soundsDir = props.getProperty("sounds-dir");
        obstaclesDir = props.getProperty("obstacles-dir");
        obstacle_sounds = Constants.contentLoader.loadContent(obstacle_sounds, PATH_OBSTACLES, new TypeReference<ArrayList<String>>() {});
        logger.debug("Loaded {} sounds", obstacle_sounds.size());
    }

    public static String getObstacleSound(String key) {
        logger.debug("Looking for obstacle: {}", key);
        if (obstacle_sounds.contains(key)) {
            return getForKey(key, baseAudioUrl + soundsDir + obstaclesDir);
        }
        return null;
    }

    public static String getForKey(String key, String url) {
        return openTag + url + key + extension + closeTag;
    }

}
