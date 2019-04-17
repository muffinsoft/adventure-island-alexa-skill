package com.muffinsoft.alexa.skills.adventureisland.content;

import com.muffinsoft.alexa.skills.adventureisland.model.Game;
import com.muffinsoft.alexa.skills.adventureisland.util.ContentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * These constants are used both internally and in external configuration
 * files as keys.
 */
public class Constants {

    public static final String DEFAULT_IMAGE_EXTENSION = "jpg";
    private static final Logger logger = LoggerFactory.getLogger(Constants.class);

    public static final Properties props;
    public static final String TABLE_NAME = "adventure_island";
    public static final String WELCOME = "welcome";
    public static final String WELCOME_BACK = "welcomeBack";
    public static final String WELCOME_BACK_ROYAL = "welcomeBackRoyal";
    public static final String NICKNAMES = "Nicknames";
    public static final String ACHIEVEMENTS = "Achievements";
    public static final String LAST = "Last";
    public static final String WELCOME_CHECKPOINT = "welcomeBackCheckpoint";
    public static final String CONTINUE = "continue";
    public static final String ROOT = "root";
    public static final String DEMO = "demo";
    public static final String HELP = "Help";
    public static final String QUIT = "quit";
    public static final String STOP = "stop";
    public static final String HEALTH = "health";
    public static final String NEW_MISSION = "newMission";
    public static final String PROMPT = "Prompt";
    public static final String LEARN_MORE = "learnMore";
    public static final String FULL_HELP = "helpFull";
    public static final String READY = "ready";
    public static final String RHETORICAL = "Rhetorical";
    public static final String ANYTHING = "any";
    public static final String REPLACE = "replace";
    public static final String REPLACEMENT_PREFIX = "with";
    public static final String SKIP = "skip";
    public static final String RETRY = "retry";
    public static final String MULTIPLY = "multiply";
    public static final String SILENT_SCENE = "finale";
    public static final String COINS_TO_COLLECT = "coinsToCollect";
    public static final String SCENE_CONFIRM = "sceneConfirm";
    public static final String SCENE_FAIL = "sceneFail";
    public static final String ACTION_APPROVE = "actionApprove";
    public static final String ACTION_FAIL = "actionFail";
    public static final String COIN_NOT_PICKED = "coinNotPicked";
    public static final String YOU_HAVE = "youHave";
    public static final String COIN_SINGLE = "coinSingle";
    public static final String COIN_PLURAL = "coinPlural";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String REPROMPT = "Reprompt";
    public static final String MIN_OBSTACLES_EXCLAIM = "minObstaclesExclamation";
    public static final String MAX_OBSTACLES_EXCLAIM = "maxObstaclesExclamation";
    public static final String HEADS_UP = "headsUp";
    public static final String SELECT_MISSION = "selectMission";
    public static final String NICKNAME_PLACEHOLDER = "__NICKNAMES__";
    public static final String TOTAL_COINS_PLACEHOLDER = "__TOTAL_COINS__";
    public static final String MISSION_NAME_PLACEHOLDER = "__MISSION_NAME__";
    public static final String MISSIONS_AVAILABLE = "__MISSIONS_AVAILABLE__";
    public static final String CARD = "Card";
    public static final List<String> COINS_FOUND = new ArrayList<>();
    public static final String SCENE_TRANSITION = "SceneTransition";
    public static final String FINISHED = "finished";
    public static final String RESTART = "restart";
    public static final String ALL = "all";
    public static final String TIERS = "tiers";
    public static final String TOTAL_MISSIONS = "missions";
    public static final String ADDITIONAL_INDEX_SEPARATOR = "_";
    public static final String OBSTACLE_EXPLANATION = "ObstacleExplanation";
    public static final String OBSTACLE_HEADS_UP = "HeadsUp";
    public static final String OBSTACLE_PRE = "PreObstacle";
    public static final String GOT = "Got";
    public static final String USED = "Used";
    public static final String UPSELL = "upSell";
    public static final String BUY = "buy";

    public static final ContentLoader contentLoader = new ContentLoader();
    public static final Game game = LayoutManager.loadGame();

    public static final int SCENES_PER_MISSION = 12;
    public static final int SCENES_PER_LOCATION = 4;
    public static final int INTRO_VARIANTS = 3;

    static {
        COINS_FOUND.add("coins");
        COINS_FOUND.add("treasure");

        props = new Properties();
        try {
            props.load(AudioManager.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            logger.error("Exception caught", e);
        }
    }
}
