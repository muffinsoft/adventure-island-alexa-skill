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

    private static final String PATH = "phrases/audio.json";
    private static List<String> audio = new ArrayList<>();
    private static String baseAudioUrl;
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
        audio = Constants.contentLoader.loadContent(audio, PATH, new TypeReference<ArrayList<String>>() {});
    }

    public static String getAudio(String key) {
        if (audio.contains(key)) {
            return getForKey(key);
        }
        return null;
    }

    public static String getForKey(String key) {
        return openTag + baseAudioUrl + key + extension + closeTag;
    }

}
