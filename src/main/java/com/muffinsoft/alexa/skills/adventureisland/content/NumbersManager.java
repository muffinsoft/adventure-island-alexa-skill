package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;

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
