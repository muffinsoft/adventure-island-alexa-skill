package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.game.TagProcessor.insertTags;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.capitalizeFirstLetter;

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
            String response = treasure.getPreObstacle();
            response = "Lily: " + response;
            return insertTags(response);
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getTreasureHeadsUp(String obstacle, StateItem state) {
        if (Objects.equals(treasure.getName(), obstacle)) {
            String response = treasure.getHeadsUp().get(state.getLocation());
            response = "Lily: " + response;
            return insertTags(response);
        }
        throw new NoSuchElementException("No responses for treasure: " + obstacle);
    }

    public static String getObstacleExplanation(StateItem state) {
        logger.debug("Getting obstacle explanation for location {},  scene {}", state.getLocation(), state.getScene());
        String explanation = AudioManager.getObstacleExplanation(state.getScene(), state.getTierIndex());
        if (explanation == null) {
            explanation = obstacleSetup.get(state.getLocation()).get(state.getScene()).get(state.getTierIndex()).getExplanation();
            explanation = "Ben: " + explanation;
            explanation = insertTags(explanation);
        }
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
        String preObstacle = AudioManager.getObstaclePre(obstacle + OBSTACLE_PRE);
        if (preObstacle == null) {
            if (isTreasure(obstacle)) {
                return getTreasurePre(obstacle);
            }
            preObstacle = getObstacleByName(state, obstacle).getPreObstacle();
            preObstacle = "Lily: " + preObstacle;
            preObstacle = insertTags(preObstacle);
        }
        return preObstacle;
    }

    public static String getHeadsUp(StateItem state, String obstacle) {
        String response = AudioManager.getObstacleHeadsUp(obstacle + OBSTACLE_HEADS_UP);
        if (response == null) {
            if (isTreasure(obstacle)) {
                return getTreasureHeadsUp(obstacle, state);
            }
            response = getObstacleByName(state, obstacle).getHeadsUp();
            response = "Lily: " + response;
            response = insertTags(response);
        }
        return  response;
    }

    public static List<String> getObstacleResponses(StateItem state, String key) {
        return getObstacleByName(state, key).getResponses();
    }

    public static ObstacleSetupItem getObstacleSetup(String location, String scene, int tier) {
        return obstacleSetup.get(location).get(scene).get(tier);
    }

    public static String getObstacleByIndex(String location, int index) {
        String response;
        if (index < 0) {
            response = getTreasureName();
        } else {
            response = obstacles.get(location).get(index).getName();
        }
        return insertTags(response);
    }

    public static String getTreasureName() {
        return treasure.getName();
    }

    public static String getWarning(String obstacle) {
        String response = AudioManager.getObstacleName(obstacle);
        return response != null ? response : insertTags("Ben: " + capitalizeFirstLetter(obstacle) + "!");
    }
}
