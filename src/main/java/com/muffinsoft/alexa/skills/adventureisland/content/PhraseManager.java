package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class PhraseManager {
    private static final String PATH = "phrases/en-US.json";
    private static Map<String, String> phrases = new HashMap<>();
    static {
        phrases = contentLoader.loadContent(phrases, PATH, new TypeReference<HashMap<String, String>>(){});
    }

    public static String getPhrase(String key) {
        return phrases.get(key);
    }
}
