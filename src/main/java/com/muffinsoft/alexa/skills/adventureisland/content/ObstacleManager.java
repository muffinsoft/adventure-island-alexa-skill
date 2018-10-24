package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.Mission;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleItem;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleSetupItem;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class ObstacleManager {

    private static final Logger logger = LoggerFactory.getLogger(ObstacleManager.class);

    private static final String PATH = "phrases/obstacles.json";
    private static final String PATH_SETUP = "phrases/obstacles-setup.json";
    private static final String PATH_COINS = "phrases/coins.json";

    private static Map<String, List<ObstacleItem>> obstacles = new HashMap<>();
    private static Map<String, Map<String, List<ObstacleSetupItem>>> obstacleSetup = new HashMap<>();
    private static List<ObstacleItem> treasure = new ArrayList<>();

    static {
        obstacles = contentLoader.loadContent(obstacles, PATH, new TypeReference<HashMap<String, List<ObstacleItem>>>(){});
        obstacleSetup = contentLoader.loadContent(obstacleSetup, PATH_SETUP, new TypeReference<HashMap<String, Map<String, List<ObstacleSetupItem>>>>(){});
        treasure = contentLoader.loadContent(treasure, PATH_COINS, new TypeReference<ArrayList<ObstacleItem>>(){});
    }

    public static boolean isTreasure(String obstacle) {
        for (ObstacleItem item : treasure) {
            if (Objects.equals(item.getName(), obstacle)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getTreasureResponses(String obstacle) {
        for (ObstacleItem item : treasure) {
            if (Objects.equals(item.getName(), obstacle)) {
                return item.getResponses();
            }
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getTreasurePre(String obstacle) {
        for (ObstacleItem item : treasure) {
            if (Objects.equals(item.getName(), obstacle)) {
                return item.getPreObstacle();
            }
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getTreasureHeadsUp(String obstacle) {
        for (ObstacleItem item : treasure) {
            if (Objects.equals(item.getName(), obstacle)) {
                return item.getHeadsUp();
            }
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getObstacleExplanation(StateItem state) {
        logger.debug("Getting obstacle explanation for location {},  scene {}", state.getLocation(), state.getScene());
        return obstacleSetup.get(state.getLocation()).get(state.getScene()).get(state.getTierIndex()).getExplanation();
    }

    private static ObstacleItem getObstacleByName(StateItem state, String obstacle) {
        List<ObstacleItem> obstacleItems = obstacles.get(state.getLocation());
        return obstacleItems.stream()
                .filter(o -> Objects.equals(o.getName(), obstacle))
                .findAny()
                .orElseThrow(() -> new RuntimeException("No such obstacle name: " + obstacle));
    }

    public static String getPreObstacle(StateItem state, String obstacle) {
        if (isTreasure(obstacle)) {
            return getTreasurePre(obstacle);
        }
        return getObstacleByName(state, obstacle).getPreObstacle();
    }

    public static String getHeadsUp(StateItem state, String obstacle) {
        if (isTreasure(obstacle)) {
            return getTreasureHeadsUp(obstacle);
        }
        return getObstacleByName(state, obstacle).getHeadsUp();
    }

    public static List<String> getObstacleResponses(StateItem state, String key) {
        return getObstacleByName(state, key).getResponses();
    }
}
