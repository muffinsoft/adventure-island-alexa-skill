package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
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
    private static CoinItem treasure = new CoinItem();

    static {
        obstacles = contentLoader.loadContent(obstacles, PATH, new TypeReference<HashMap<String, List<ObstacleItem>>>() {
        });
        obstacleSetup = contentLoader.loadContent(obstacleSetup, PATH_SETUP, new TypeReference<HashMap<String, Map<String, List<ObstacleSetupItem>>>>() {
        });
        treasure = contentLoader.loadContent(treasure, PATH_COINS, new TypeReference<CoinItem>() {
        });
    }

    public static boolean isTreasure(String obstacle) {
        if (obstacle == null) {
            return false;
        }
        return Objects.equals(treasure.getName(), obstacle);
    }

    public static List<String> getTreasureResponses(String obstacle) {
        if (Objects.equals(treasure.getName(), obstacle)) {
            return treasure.getResponses();
        }

        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getTreasurePre(String obstacle) {
        if (Objects.equals(treasure.getName(), obstacle)) {
            return treasure.getPreObstacle();
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getTreasureHeadsUp(String obstacle, StateItem state) {
        if (Objects.equals(treasure.getName(), obstacle)) {
            return treasure.getHeadsUp().get(state.getLocation());
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getObstacleExplanation(StateItem state) {
        logger.debug("Getting obstacle explanation for location {},  scene {}", state.getLocation(), state.getScene());
        String explanation = obstacleSetup.get(state.getLocation()).get(state.getScene()).get(state.getTierIndex()).getExplanation();
        explanation = TagProcessor.insertTags(explanation);
        return explanation;
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
        String preObstacle = getObstacleByName(state, obstacle).getPreObstacle();
        preObstacle = TagProcessor.insertTags(preObstacle);
        return preObstacle;
    }

    public static String getHeadsUp(StateItem state, String obstacle) {
        if (isTreasure(obstacle)) {
            return getTreasureHeadsUp(obstacle, state);
        }
        return getObstacleByName(state, obstacle).getHeadsUp();
    }

    public static List<String> getObstacleResponses(StateItem state, String key) {
        return getObstacleByName(state, key).getResponses();
    }

    public static ObstacleSetupItem getObstacleSetup(String location, String scene, int tier) {
        return obstacleSetup.get(location).get(scene).get(tier);
    }

    public static String getObstacleByIndex(String location, int index) {
        if (index < 0) {
            return getTreasureName();
        }
        return obstacles.get(location).get(index).getName();
    }

    public static String getTreasureName() {
        return treasure.getName();
    }
}
