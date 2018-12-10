package com.muffinsoft.alexa.skills.adventureisland;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.util.ContentLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AudioGenerator {

    private static final String PATH = "phrases/en-US.json";
    private static final String CHARACTERS = "phrases/characters.json";
    private static final String OUT = "phrases-for-audio.csv";
    private static final String ALEXA = "Alexa";
    private static final String SPEECHCON = "Alexa Speechcon";
    private static int noAudioIndex = 0;

    private static Map<String, String> phrases = new LinkedHashMap<>();
    private static Map<String, String> characters = new LinkedHashMap<>();

    public static void main(String[] args) {
        ContentLoader contentLoader = new ContentLoader();
        phrases = contentLoader.loadContent(phrases, PATH, new TypeReference<LinkedHashMap<String, String>>() {
        });
        characters = contentLoader.loadContent(characters, CHARACTERS, new TypeReference<HashMap<String, String>>() {
        });

        Map<String, String> result = new LinkedHashMap<>();

        parsePhrases(result);

        for (String key : result.keySet()) {
            System.out.println(key + ";" + result.get(key));
        }
    }

    private static void parsePhrases(Map<String, String> result) {
        for (String key : phrases.keySet()) {
            boolean isDialog = false;
            int filenameIndex = 0;
            String text = phrases.get(key);
            int start = voiceStarts(text);
            while (start >= 0) {
                isDialog = true;
                String phrase = "";
                if (start > 0) {
                    phrase = text.substring(0, start);
                    if (checkAlexa(phrase, result)) {
                        phrase = "";
                    }
                    text = text.substring(start);
                }
                int end = getEnd(text);
                phrase += text.substring(0, end);
                String filename = getFilename(key, filenameIndex++);
                phrase = parsePhrasesMultiline(phrase);
                result.put(filename, phrase);
                text = text.substring(end);
                start = voiceStarts(text);
            }
            if (isDialog) {
                checkAlexa(text, result);
            }

        }
    }

    private static String getFilename(String key, int filenameIndex) {
        String filename = key;
        if (filenameIndex > 0) {
            filename = filename + "_" + filenameIndex;
        }
        filename += ".mp3";
        return filename;
    }

    private static boolean checkAlexa(String text, Map<String, String> result) {
        if (text.contains(ALEXA) || text.contains(SPEECHCON)) {
            result.put("NoAudio" + noAudioIndex++, "\"" + text + "\"");
            return true;
        }
        return false;
    }

    private static String parsePhrasesMultiline(String text) {
        StringBuilder phrase = new StringBuilder("\"");
        while (containsCharacters(text)) {
            int phraseEnd = nextCharacter(text, 0);
            if (phrase.length() > 1) {
                phrase.append("\n");
            }
            phrase.append(text, 0, phraseEnd);
            text = text.substring(phraseEnd);
        }
        phrase.append(text);
        phrase.append("\"");
        return phrase.toString();
    }

    private static boolean containsCharacters(String text) {
        for (String character : characters.keySet()) {
            String placeholder = character + ": ";
            if (text.contains(placeholder)) {
                return true;
            }
        }
        return false;
    }

    private static File getOutputFile() {
        URL url = ContentLoader.class.getClassLoader().getResource(OUT);
        try {
            return Paths.get(url.toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getEnd(String text) {
        String alexaStart = ALEXA + ": ";
        String speechconStart = SPEECHCON + ": ";

        int i = text.indexOf(alexaStart);
        int j = text.indexOf(speechconStart);
        if (j > 0 && j < i) {
            i = j;
        }
        if (i > 0) {
            return i;
        } else {
            return text.length();
        }
    }

    private static int voiceStarts(String text) {
        int smallest = text.length();
        for (String character : characters.keySet()) {
            if (character.equals(ALEXA) || character.equals(SPEECHCON)) {
                continue;
            }
            int end = text.indexOf(character + ": ");
            if (end > 0 && end < smallest) {
                smallest = end;
            }
        }
        if (smallest == text.length()) {
            smallest = -1;
        }
        return smallest;
    }

    public static int nextCharacter(String text, int start) {
        int smallest = text.length();
        for (String character : characters.keySet()) {
            int end = text.indexOf(character + ": ", start + 1);
            if (end > 0 && end < smallest) {
                smallest = end;
            }
        }
        return smallest;
    }

}
