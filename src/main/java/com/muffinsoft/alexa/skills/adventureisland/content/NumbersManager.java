package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class NumbersManager {
    private static final String PATH = "numbers/main.json";
    private static Map<String, Integer> numbers = new HashMap<>();

    static {
        numbers = contentLoader.loadContent(numbers, PATH, new TypeReference<HashMap<String, Integer>>(){});
    }

    public static Integer getNumber(String key) {
        return numbers.get(key);
    }
}
