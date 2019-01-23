package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;

public class ImageManager {

    private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);

    private static final String PATH_OBSTACLES = "apl/obstacles.json";
    private static final String PATH_CONNECTORS = "apl/connectors.json";
    private static final String PATH_EXPLANATIONS = "apl/explanations.json";
    private static final String PATH_GENERAL = "apl/general.json";
    private static final String PATH_MISSION = "apl/mission.json";
    private static final String PATH_POWERUPS = "apl/powerups.json";
    private static final String PATH_HEARTS = "apl/hearts.json";

    private static List<String> obstacleImages = new ArrayList<>();
    private static List<String> obstacleExplanations = new ArrayList<>();
    private static List<String> locationConnectors = new ArrayList<>();
    private static List<String> generalImages = new ArrayList<>();
    private static List<String> missionImages = new ArrayList<>();
    private static List<String> powerupsImages = new ArrayList<>();
    private static List<String> heartsImages = new ArrayList<>();

    private static String baseImageDir;
    private static String obstaclesDir;
    private static String connectorsDir;
    private static String explanationsDir;
    private static String missionDir;
    private static String powerupsDir;
    private static String heartsDir;

    private static final String extension = ".jpg";

    static {
        baseImageDir = props.getProperty("base-storage-url") + props.getProperty("images-dir");
        obstaclesDir = baseImageDir + props.getProperty("obstacles-dir");
        connectorsDir = baseImageDir + props.getProperty("connectors-dir");
        explanationsDir = baseImageDir + props.getProperty("obstacle-explanation");
        missionDir = baseImageDir + props.getProperty("mission-dir");
        powerupsDir = baseImageDir + props.getProperty("powerups-dir");
        heartsDir = baseImageDir + props.getProperty("hearts-dir");

        obstacleImages = contentLoader.loadContent(obstacleImages, PATH_OBSTACLES, new TypeReference<ArrayList<String>>() {});
        obstacleExplanations = contentLoader.loadContent(obstacleExplanations, PATH_EXPLANATIONS, new TypeReference<ArrayList<String>>() {});
        locationConnectors = contentLoader.loadContent(locationConnectors, PATH_CONNECTORS, new TypeReference<ArrayList<String>>() {});
        generalImages = contentLoader.loadContent(generalImages, PATH_GENERAL, new TypeReference<ArrayList<String>>() {});
        missionImages = contentLoader.loadContent(missionImages, PATH_MISSION, new TypeReference<ArrayList<String>>() {});
        powerupsImages = contentLoader.loadContent(powerupsImages, PATH_POWERUPS, new TypeReference<ArrayList<String>>() {});
        heartsImages = contentLoader.loadContent(heartsImages, PATH_HEARTS, new TypeReference<ArrayList<String>>() {});
    }

    public static String getObstacleImageUrl(String obstacle) {
        String key = PhraseManager.nameToKey(obstacle);
        if (obstacleImages.contains(key)) {
            return obstaclesDir + key + extension;
        } else {
            return null;
        }
    }

    public static String getObstacleExplanation(StateItem stateItem) {
        String tier = stateItem.getTierIndex() == 0 ? "" : "" + stateItem.getTierIndex();
        String scene = SILENT_SCENE.equals(stateItem.getScene()) ? getPreviousSceneName(stateItem) : stateItem.getScene();
        String key = scene + tier;

        logger.debug("Looking for image key: {}", key);

        if (obstacleExplanations.contains(key)) {
            return explanationsDir + key + extension;
        } else {
            return null;
        }
    }

    private static String getPreviousSceneName(StateItem stateItem) {
        try {
            String missionName = game.getMissions().get(stateItem.getMissionIndex()).getLocations().get(stateItem.getLocationIndex()).getActivities().get(stateItem.getSceneIndex() - 1).getName();
            logger.debug("Extracted {} for silent scene", missionName);
            return PhraseManager.nameToKey(missionName);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            return "";
        }
    }

    public static String getGeneralImageByKey(String key) {
        if (generalImages.contains(key)) {
            return baseImageDir + key + extension;
        } else {
            return null;
        }
    }

    public static String getConnector(StateItem stateItem) {
        String tier = stateItem.getTierIndex() == 0 ? "" : "" + stateItem.getTierIndex();
        String key = stateItem.getMission() + Utils.capitalizeFirstLetter(stateItem.getLocation()) + tier + State.OUTRO.getKey();
        logger.debug("Looking for connector under the key: {}", key);
        if (locationConnectors.contains(key)) {
            return connectorsDir + key + extension;
        } else {
            return null;
        }
    }

    public static String getMissionImage(StateItem stateItem) {
        String tier = stateItem.getTierIndex() == 0 ? "" : "" + stateItem.getTierIndex();
        String key = stateItem.getMission() + tier + stateItem.getState().getKey();
        return getMissionImageByKey(key);
    }

    public static String getMissionImageByKey(String key) {
        if (missionImages.contains(key)) {
            return missionDir + key + extension;
        } else {
            return null;
        }
    }

    public static String getPowerupImage(String key) {
        if (powerupsImages.contains(key)) {
            return powerupsDir + key + extension;
        } else {
            return null;
        }
    }

    public static String getHeartsImage(String key) {
        if (heartsImages.contains(key)) {
            return heartsDir + key + extension;
        } else {
            return null;
        }
    }

}
