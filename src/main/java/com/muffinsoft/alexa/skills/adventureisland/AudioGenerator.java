package com.muffinsoft.alexa.skills.adventureisland;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.util.ContentLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AudioGenerator {

    private static final String PATH = "phrases/en-US.json";
    private static final String CHARACTERS = "phrases/characters.json";
    private static final String OUT = "phrases-for-audio.csv";
    private static final String ALEXA = "Alexa";
    private static final String SPEECHCON = "Alexa Speechcon";

    private static Map<String, String> phrases = new LinkedHashMap<>();
    private static Map<String, String> characters = new LinkedHashMap<>();

    public static void main(String[] args) {
        ContentLoader contentLoader = new ContentLoader();
        phrases = contentLoader.loadContent(phrases, PATH, new TypeReference<LinkedHashMap<String, String>>() {});
        characters = contentLoader.loadContent(characters, CHARACTERS, new TypeReference<HashMap<String, String>>() {});

        Map<String, String> result = new LinkedHashMap<>();

        for (String key : phrases.keySet()) {
            int filenameIndex = 0;
            String text = phrases.get(key);
            int start = voiceStarts(text);
            while (start >= 0) {
                if (start > 0) {
                    text = text.substring(start);
                }
                int end = getEnd(text);
                String phrase = text.substring(0, end);
                String filename = key;
                if (filenameIndex > 0) {
                    filename = filename + "_" + filenameIndex;
                }
                filename += ".mp3";
                result.put(filename, phrase);
                filenameIndex++;
                text = text.substring(end);
                start = voiceStarts(text);
            }

        }

        for (String key : result.keySet()) {
            System.out.println(key + ";" + result.get(key));
        }
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
        for (String character : characters.keySet()) {
            if (character.equals(ALEXA) || character.equals(SPEECHCON)) {
                continue;
            }
            String placeholder = character + ": ";
            if (text.contains(placeholder)) {
                return text.indexOf(placeholder);
            }
        }
        return -1;
    }

}
