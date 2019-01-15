package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.game.Utils;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;

import java.util.ArrayList;
import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.props;

public class ImageManager {

    private static final String PATH_OBSTACLES = "apl/obstacles.json";
    private static final String PATH_CONNECTORS = "apl/connectors.json";
    private static final String PATH_EXPLANATIONS = "apl/explanations.json";
    private static final String PATH_GENERAL = "apl/general.json";

    private static List<String> obstacleImages = new ArrayList<>();
    private static List<String> obstacleExplanations = new ArrayList<>();
    private static List<String> locationConnectors = new ArrayList<>();
    private static List<String> generalImages = new ArrayList<>();

    private static String baseImageDir;
    private static String obstaclesDir;
    private static String connectorsDir;
    private static String explanationsDir;

    private static final String extension = ".jpg";

    static {
        baseImageDir = props.getProperty("base-storage-url") + props.getProperty("images-dir");
        obstaclesDir = baseImageDir + props.getProperty("obstacles-dir");
        connectorsDir = baseImageDir + props.getProperty("connectors-dir");
        explanationsDir = baseImageDir + props.getProperty("obstacle-explanation");

        obstacleImages = contentLoader.loadContent(obstacleImages, PATH_OBSTACLES, new TypeReference<ArrayList<String>>() {});
        obstacleExplanations = contentLoader.loadContent(obstacleExplanations, PATH_EXPLANATIONS, new TypeReference<ArrayList<String>>() {});
        locationConnectors = contentLoader.loadContent(locationConnectors, PATH_CONNECTORS, new TypeReference<ArrayList<String>>() {});
        generalImages = contentLoader.loadContent(generalImages, PATH_GENERAL, new TypeReference<ArrayList<String>>() {});
    }

    public static String getObstacleImageUrl(String obstacle) {
        if (obstacleImages.contains(obstacle)) {
            return obstaclesDir + obstacle + extension;
        } else {
            return null;
        }
    }

    public static String getObstacleExplanation(StateItem stateItem) {
        String tier = stateItem.getTierIndex() == 0 ? "" : "" + stateItem.getTierIndex();
        String key = stateItem.getScene() + tier;

        if (obstacleExplanations.contains(key)) {
            return explanationsDir + key + extension;
        } else {
            return null;
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
        if (locationConnectors.contains(key)) {
            return connectorsDir + key + extension;
        } else {
            return null;
        }
    }

}
