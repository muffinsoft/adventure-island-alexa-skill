package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class PhraseManager {
    private static final String PATH = "phrases/en-US.json";
    private static Map<String, String> phrases = new HashMap<>();
    private static List<String> exclamations = new ArrayList<>();
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    static {
        phrases = contentLoader.loadContent(phrases, PATH, new TypeReference<HashMap<String, String>>(){});

        exclamations.add(phrases.get("exclamation1"));
        exclamations.add(phrases.get("exclamation2"));
        exclamations.add(phrases.get("exclamation3"));
        exclamations.add(phrases.get("exclamation4"));
        exclamations.add(phrases.get("exclamation5"));
        exclamations.add(phrases.get("exclamation6"));
        exclamations.add(phrases.get("exclamation7"));
        exclamations.add(phrases.get("exclamation8"));
        exclamations.add(phrases.get("exclamation9"));
        exclamations.add(phrases.get("exclamation10"));
    }

    public static String getPhrase(String key) {
        return TagProcessor.insertTags(phrases.get(key));
    }

    public static String getTextOnly(String key) {
        return phrases.get(key);
    }

    public static String nameToKey(String name) {
        String result = name.replace(" ", "");
        return result.substring(0, 1).toLowerCase() + result.substring(1);
    }

    public static String getExclamation() {
        int index = random.nextInt(exclamations.size());
        return TagProcessor.insertTags(exclamations.get(index));
    }
}
