package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.Mission;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleItem;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleSetupItem;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class ObstacleManager {

    private static final String PATH = "phrases/obstacles.json";
    private static final String PATH_SETUP = "phrases/obstacles-setup.json";
    private static final String PATH_COINS = "phrases/coins.json";

    private static Map<String, List<ObstacleItem>> obstacles = new HashMap<>();
    private static Map<String, Map<String, ObstacleSetupItem>> obstacleSetup = new HashMap<>();
    private static ObstacleItem treasure = new ObstacleItem();

    static {
        obstacles = contentLoader.loadContent(obstacles, PATH, new TypeReference<HashMap<String, List<ObstacleItem>>>(){});
        obstacleSetup = contentLoader.loadContent(obstacleSetup, PATH_SETUP, new TypeReference<HashMap<String, Map<String, ObstacleSetupItem>>>(){});
        treasure = contentLoader.loadContent(treasure, PATH_COINS, new TypeReference<ObstacleItem>(){});
    }

    public static String getTreasureName() {
        return treasure.getName();
    }

    public static List<String> getTreasureResponses() {
        return treasure.getResponses();
    }

    public static String getTreasurePre() {
        return treasure.getPreObstacle();
    }

    public static List<String> getObstacleResponses(StateItem state, String key) {
        List<ObstacleItem> obstacleItems = obstacles.get(state.getLocation());
        return obstacleItems.stream()
                .filter(o -> Objects.equals(o.getName(), key))
                .map(ObstacleItem::getResponses)
                .findAny()
                .orElseThrow(() -> new RuntimeException("No such obstacle name: " + key));
    }
}
