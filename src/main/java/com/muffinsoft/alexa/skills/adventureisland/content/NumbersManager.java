package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NumbersManager {
    private static final Logger logger = LoggerFactory.getLogger(NumbersManager.class);

    private static final String PATH = "numbers/main.json";
    private static Map<String, Integer> numbers;

    static {
        File file = new File(PATH);
        try {
            numbers = new ObjectMapper().readValue(file, new TypeReference<HashMap<String, Integer>>(){});
        } catch (IOException e) {
            logger.error("Exception", e);
        }
    }

    public static Integer getNumber(String key) {
        return numbers.get(key);
    }
}
