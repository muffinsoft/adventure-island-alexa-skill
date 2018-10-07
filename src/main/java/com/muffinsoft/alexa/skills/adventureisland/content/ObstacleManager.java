package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ObstacleManager {
    private static final Logger logger = LoggerFactory.getLogger(ObstacleManager.class);

    private static final String PATH = "phrases/obstacles.json";
    private static Map<String, String> obstacles;
    static {
        File file = new File(PATH);
        try {
            obstacles = new ObjectMapper().readValue(file, new TypeReference<HashMap<String, String>>(){});
        } catch (IOException e) {
            logger.error("Exception", e);
        }
    }

    public static String getObstacle() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> obstacleList = new ArrayList<>(obstacles.keySet());
        int nextObstacle = random.nextInt(obstacles.size());
        return obstacleList.get(nextObstacle);
    }

    public static String getObstacleResponse(String key) {
        return obstacles.get(key);
    }
}
