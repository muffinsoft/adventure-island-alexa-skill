package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.ADDITIONAL_INDEX_SEPARATOR;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class PhraseManager {
    private static final String PATH = "phrases/en-US.json";
    private static Map<String, String> phrases = new HashMap<>();
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    static {
        phrases = contentLoader.loadContent(phrases, PATH, new TypeReference<HashMap<String, String>>(){});

    }

    public static String getPhrase(String key) {
        String result = null;
        int additionalIndex = 0;
        String phrase = getAudioAndText(key, additionalIndex);
        while (phrase != null && !phrase.isEmpty()) {
            result = Utils.combine(result, phrase);
            additionalIndex++;
            phrase = getAudioAndText(key, additionalIndex);
        }
        return result;
    }

    private static String getAudioAndText(String key, int additionalIndex) {
        if (additionalIndex > 0) {
            key = key + ADDITIONAL_INDEX_SEPARATOR + additionalIndex;
        }
        String audio = AudioManager.getPhrase(key);
        String phrase = phrases.get(key);
        phrase = TagProcessor.insertTags(phrase);
        phrase = Utils.combine(audio, phrase);
        return phrase;
    }

    public static String getTextOnly(String key) {
        return phrases.get(key);
    }

    public static String nameToKey(String name) {
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            String firstUp = word.substring(0, 1).toUpperCase() + word.substring(1);
            result.append(firstUp);
        }
        return result.substring(0, 1).toLowerCase() + result.substring(1);
    }

    public static String getExclamation() {
        int index = random.nextInt(10);
        String key = "exclamation" + (index + 1);
        return AudioManager.getPhrase(key);
    }
}
