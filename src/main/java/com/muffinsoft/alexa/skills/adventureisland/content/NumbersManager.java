package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;

public class NumbersManager {
    public static final int TIERS;
    public static final int MISSIONS;

    private static final String PATH = "numbers/main.json";
    private static Map<String, Object> numbers = new HashMap<>();
    private static List<Integer> coinsToCollect;
    private static final int MIN_OBSTACLES;
    private static final int MAX_OBSTACLES;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    static {
        numbers = contentLoader.loadContent(numbers, PATH, new TypeReference<HashMap<String, Object>>(){});
        MIN_OBSTACLES = (int) numbers.get(MIN_OBSTACLES_EXCLAIM);
        MAX_OBSTACLES = (int) numbers.get(MAX_OBSTACLES_EXCLAIM);
        TIERS = (int) numbers.get(Constants.TIERS);
        MISSIONS = (int) numbers.get(Constants.TOTAL_MISSIONS);
        coinsToCollect = (List<Integer>) numbers.get(COINS_TO_COLLECT);
    }

    public static Integer getNumber(String key) {
        return (int) numbers.get(key);
    }

    public static Integer getCoinsToCollect(int tierIndex) {
        return coinsToCollect.get(tierIndex);
    }

    public static int getTurnsToNextExclamation() {
        int difference = MAX_OBSTACLES - MIN_OBSTACLES;
        int nextInt = random.nextInt(difference + 1);
        return MIN_OBSTACLES + nextInt;
    }
}
