package com.muffinsoft.alexa.skills.adventureisland;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.CoinItem;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleItem;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleSetupItem;
import com.muffinsoft.alexa.skills.adventureisland.model.Powerup;
import com.muffinsoft.alexa.skills.adventureisland.util.ContentLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class AudioGenerator {

    private static final String PATH = "phrases/en-US.json";
    private static final String CHARACTERS = "phrases/characters.json";

    private static final String PATH_OBSTACLES = "phrases/obstacles.json";
    private static final String PATH_SETUP = "phrases/obstacles-setup.json";
    private static final String PATH_COINS = "phrases/coins.json";
    private static final String PATH_POWERUPS = "phrases/powerups.json";

    private static final String OUT = "phrases-for-audio.csv";
    private static final String ALEXA = "Alexa";
    private static final String SPEECHCON = "Alexa Speechcon";

    private static int noAudioIndex = 0;
    private static int wrap = 140;

    private static Map<String, String> phrases = new LinkedHashMap<>();
    private static Map<String, String> characters = new LinkedHashMap<>();
    private static Map<String, List<ObstacleItem>> obstacles = new LinkedHashMap<>();
    private static Map<String, Map<String, List<ObstacleSetupItem>>> obstacleSetup = new LinkedHashMap<>();
    private static CoinItem treasure = new CoinItem();
    private static List<Powerup> powerups = new ArrayList<>();

    public static void main(String[] args) {
        ContentLoader contentLoader = new ContentLoader();
        phrases = contentLoader.loadContent(phrases, PATH, new TypeReference<LinkedHashMap<String, String>>() {});
        characters = contentLoader.loadContent(characters, CHARACTERS, new TypeReference<HashMap<String, String>>() {});

        obstacles = contentLoader.loadContent(obstacles, PATH_OBSTACLES, new TypeReference<LinkedHashMap<String, List<ObstacleItem>>>() {});
        obstacleSetup = contentLoader.loadContent(obstacleSetup, PATH_SETUP, new TypeReference<LinkedHashMap<String, Map<String, List<ObstacleSetupItem>>>>() {});
        treasure = contentLoader.loadContent(treasure, PATH_COINS, new TypeReference<CoinItem>() {});
        powerups = Constants.contentLoader.loadContent(powerups, PATH_POWERUPS, new TypeReference<ArrayList<Powerup>>() {});


        Map<String, String> result = new LinkedHashMap<>();

        parsePhrases(result);

        addObstacleExplanations(result);

        addObstaclesStuff(result);

        addTreasureStuff(result);

        addPowerUps(result);

        for (String key : result.keySet()) {
            System.out.println(key + ";" + result.get(key));
        }
    }

    private static void addPowerUps(Map<String, String> result) {
        for (Powerup powerup : powerups) {
            String name = PhraseManager.nameToKey(powerup.getName());
            StringBuilder stringBuilder = new StringBuilder();
            String got = powerup.getGotRaw();
            doAppend(got, stringBuilder);
            got = "\"" + stringBuilder.toString() + "\"";
            stringBuilder = new StringBuilder();
            String used = powerup.getUsedRaw();
            doAppend(used, stringBuilder);
            used = "\"" + stringBuilder.toString() + "\"";
            result.put(name + "Got", got);
            result.put(name + "Used", used);
        }
    }

    private static void addTreasureStuff(Map<String, String> result) {
        String name = treasure.getName();
        String preObstacle = "\"Lily: " + treasure.getPreObstacle().replace(SPEECHCON + ": ", "") + "\"";
        result.put(name + "PreObstacle", preObstacle);
        for (String location : treasure.getHeadsUp().keySet()) {
            String headUp = "\"Lily: " + treasure.getHeadsUp().get(location) + "\"";
            result.put(name + "HeadsUp" + Utils.capitalizeFirstLetter(location), headUp);
        }
    }

    private static void addObstaclesStuff(Map<String, String> result) {
        for (String location : obstacles.keySet()) {
            List<ObstacleItem> items = obstacles.get(location);
            for (ObstacleItem item : items) {
                String name = PhraseManager.nameToKey(item.getName());
                String headsUp = "\"Lily: " + item.getHeadsUp() + "\"";
                String preObstacle = "\"Lily: " + item.getPreObstacle().replace(SPEECHCON + ": ", "") + "\"";
                result.put(name + "PreObstacle", preObstacle);
                result.put(name + "HeadsUp", headsUp);
            }
        }
    }

    private static void addObstacleExplanations(Map<String, String> result) {
        for (String location : obstacleSetup.keySet()) {
            Map<String, List<ObstacleSetupItem>> setups = obstacleSetup.get(location);
            for (String scene : setups.keySet()) {
                List<ObstacleSetupItem> items = setups.get(scene);
                for (int i = 0; i < items.size(); i++) {
                    String explanation = items.get(i).getExplanation();
                    if (!explanation.isEmpty()) {
                        String filename = scene + i + "ObstacleExplanation";
                        StringBuilder phraseBuilder = new StringBuilder();
                        doAppend("Ben: " + explanation, phraseBuilder);
                        String phrase = "\"" + phraseBuilder.toString() + "\"";

                        phrase = phrase.replace(SPEECHCON + ": ", "");
                        result.put(filename, phrase);
                    }
                }
            }
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
        if (text.contains(ALEXA + ": ") || text.contains(SPEECHCON + ": ")) {
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
            String newPhrase = text.substring(0, phraseEnd);
            doAppend(newPhrase, phrase);
            text = text.substring(phraseEnd);
        }
        doAppend(text, phrase);
        phrase.append("\"");
        return phrase.toString();
    }

    private static void doAppend(String text, StringBuilder phrase) {
        if (text.length() > wrap) {
            int i = text.lastIndexOf(" ", wrap);
            phrase.append(text, 0, i);
            phrase.append("\n");
            phrase.append(text.substring(i + 1));
        } else {
            phrase.append(text);
        }
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
            if (end >= 0 && end < smallest) {
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
