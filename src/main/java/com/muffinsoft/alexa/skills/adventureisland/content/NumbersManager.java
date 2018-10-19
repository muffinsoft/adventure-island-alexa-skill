package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.MAX_OBSTACLES_EXCLAIM;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.MIN_OBSTACLES_EXCLAIM;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class NumbersManager {
    private static final String PATH = "numbers/main.json";
    private static Map<String, Integer> numbers = new HashMap<>();
    private static final int MIN_OBSTACLES;
    private static final int MAX_OBSTACLES;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    static {
        numbers = contentLoader.loadContent(numbers, PATH, new TypeReference<HashMap<String, Integer>>(){});
        MIN_OBSTACLES = numbers.get(MIN_OBSTACLES_EXCLAIM);
        MAX_OBSTACLES = numbers.get(MAX_OBSTACLES_EXCLAIM);
    }

    public static Integer getNumber(String key) {
        return numbers.get(key);
    }

    public static int getTurnsToNextExclamation() {
        int difference = MAX_OBSTACLES - MIN_OBSTACLES;
        int nextInt = random.nextInt(difference + 1);
        return MIN_OBSTACLES + nextInt;
    }
}
