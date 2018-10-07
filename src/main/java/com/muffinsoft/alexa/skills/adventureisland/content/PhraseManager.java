package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PhraseManager {
    private static final Logger logger = LoggerFactory.getLogger(PhraseManager.class);

    private static final String PATH = "phrases/en-US.json";
    private static Map<String, String> phrases;
    static {
        File file = new File(PATH);
        try {
            phrases = new ObjectMapper().readValue(file, new TypeReference<HashMap<String, String>>(){});
        } catch (IOException e) {
            logger.error("Exception", e);
        }
    }

    public static String getPhrase(String key) {
        return phrases.get(key);
    }
}
