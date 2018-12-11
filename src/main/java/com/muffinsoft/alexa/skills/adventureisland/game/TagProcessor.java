package com.muffinsoft.alexa.skills.adventureisland.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.content.AudioManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class TagProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TagProcessor.class);

    private static final String PATH = "phrases/characters.json";
    private static final String PATH_SOUNDS = "audio/sounds.json";
    private static final String SPEECHCON = "speechcon";
    private static final String NONE = "none";
    private static final String SOUNDS_OPEN = "[";
    private static final String SOUNDS_CLOSE = "]";

    private static Map<String, String> characters = new HashMap<>();
    private static List<String> sounds = new ArrayList<>();

    static {
        characters = contentLoader.loadContent(characters, PATH, new TypeReference<HashMap<String, String>>() {});
        sounds = contentLoader.loadContent(sounds, PATH_SOUNDS, new TypeReference<List<String>>() {});
    }

    public static String insertTags(String text) {
        if (text == null) {
            return null;
        }
        text = replaceSpeechcon(text);
        while (containsVoices(text)) {
            text = replaceVoices(text);
        }
        text = replaceNone(text);
        text = insertSounds(text);
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

    private static String insertSounds(String text) {
        while (text.contains(SOUNDS_OPEN)) {
            String name = text.substring(text.indexOf(SOUNDS_OPEN) + 1, text.indexOf(SOUNDS_CLOSE));
            String replacement = "";
            String key = PhraseManager.nameToKey(name);
            if (sounds.contains(key)) {
                replacement = AudioManager.getSound(key, AudioManager.generalDir);
            }
            text = text.replace(SOUNDS_OPEN + name + SOUNDS_CLOSE, replacement);
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
            int afterExclamation = text.indexOf("!", start) + 1;
            int end = nextCharacter(text, start);
            if (afterExclamation > 1 && afterExclamation < end) {
                end = afterExclamation;
            }
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
                    text = boundSpeechcon(tag, text);
                }
            }
        }
        return text;
    }

    private static String boundSpeechcon(String tag, String text) {
        int i = text.indexOf(" <say-as");
        String endTag = "</say-as>";
        while (i >= 0 && i > text.indexOf(tag) && i < text.lastIndexOf("</voice>")) {
            int end = text.indexOf(endTag, i + 1);
            text = text.substring(0, i + 1) + "</voice>" + text.substring(i + 1, end + endTag.length()) + tag + text.substring(end + endTag.length());
            i = text.indexOf(" <say-as");
        }
        return text;
    }

    private static int nextCharacter(String text, int start) {
        int smallest = text.length();
        for (String character : characters.keySet()) {
            int end = text.indexOf(character + ": ", start + 1);
            int tagStart = text.indexOf("<voice", start + 2);
            if (tagStart > 0 && tagStart < end) {
                end = tagStart;
            }
            if (end > 0 && end < smallest) {
                smallest = end;
            }
        }
        return smallest;
    }


    public static void getReprompt(DialogItem dialog) {
        String response = dialog.getResponseText().trim();
        int i = response.lastIndexOf(".");
        int j = response.lastIndexOf("!");
        if (j > i) {
            i = j;
        }
        String reprompt = response.substring(i + 1).trim();
        if (reprompt.endsWith("</voice>")) {
            int tagStart = response.lastIndexOf("<voice");
            int tagEnd = response.indexOf(">", tagStart+1);
            String tag = response.substring(tagStart, tagEnd + 1);
            reprompt = tag + reprompt;
        } else {
            reprompt = reprompt.replace("</voice>", "");
        }
        dialog.setReprompt(reprompt);

    }
}
