package com.muffinsoft.alexa.skills.adventureisland.game;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class TagProcessor {

    private static final String PATH = "phrases/characters.json";
    private static final String SPEECHCON = "speechcon";
    private static final String NONE = "none";

    private static Map<String, String> characters = new HashMap<>();

    static {
        characters = contentLoader.loadContent(characters, PATH, new TypeReference<HashMap<String, String>>() {
        });
    }

    public static String insertTags(String text) {
        if (text == null) {
            return null;
        }
        while (containsVoices(text)) {
            text = replaceVoices(text);
        }
        text = replaceSpeechcon(text);
        text = replaceNone(text);
        return text;
    }

    private static String replaceNone(String text) {
        for (String character : characters.keySet()) {
            String replacement = characters.get(character);
            if (Objects.equals(replacement, NONE)) {
                String placeholder = character + ": ";
                text = text.replace(placeholder, "").trim();
            }
        }
        return text;
    }

    private static String replaceSpeechcon(String text) {
        String placeholder = "";
        for (String character : characters.keySet()) {
            if (Objects.equals(characters.get(character), SPEECHCON)) {
                placeholder = character;
            }
        }
        placeholder = placeholder + ": ";
        int start = text.indexOf(placeholder);
        while (start >= 0) {
            text = text.replaceFirst(placeholder, "<say-as interpret-as=\"interjection\">").trim();
            int end = nextCharacter(text, start);
            text = text.substring(0, end) + "</say-as>" + text.substring(end);
            start = text.indexOf(placeholder);
        }

        return text;
    }

    private static boolean containsVoices(String text) {
        for (String character : characters.keySet()) {
            String value = characters.get(character);
            if (Objects.equals(value, NONE) || Objects.equals(value, SPEECHCON)) {
                continue;
            }
            String placeholder = character + ": ";
            if (text.contains(placeholder)) {
                return true;
            }
        }
        return false;
    }

    private static String replaceVoices(String text) {
        for (String character : characters.keySet()) {
            String placeholder = character + ": ";
            int start = text.indexOf(placeholder);
            String replacement = characters.get(character);
            if (start >= 0) {
                if (!Objects.equals(replacement, NONE) && !Objects.equals(replacement, SPEECHCON)) {
                    String tag = "<voice name=\"" + replacement + "\">";
                    text = text.replaceFirst(placeholder, tag);
                    int end = nextCharacter(text, start);
                    text = text.substring(0, end) + "</voice>" + text.substring(end);
                }
            }
        }
        return text;
    }

    private static int nextCharacter(String text, int start) {
        int smallest = text.length();
        for (String character : characters.keySet()) {
            int end = text.indexOf(character + ": ", start + 1);
            int tagStart = text.indexOf("<", start + 2);
            if (tagStart > 0 && tagStart < end) {
                end = tagStart;
            }
            if (end > 0 && end < smallest) {
                smallest = end;
            }
        }
        return smallest;
    }


}
