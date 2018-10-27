package com.muffinsoft.alexa.skills.adventureisland.content;

import com.muffinsoft.alexa.skills.adventureisland.model.Game;
import com.muffinsoft.alexa.skills.adventureisland.util.ContentLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * These constants are used both internally and in external configuration
 * files as keys.
 */
public class Constants {

    public static final String TABLE_NAME = "adventure_island";
    public static final String WELCOME = "welcome";
    public static final String WELCOME_BACK = "welcomeBack";
    public static final String ROOT = "root";
    public static final String DEMO = "demo";
    public static final String NAME = "name";
    public static final String PROMPT = "Prompt";
    public static final String READY = "ready";
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
    public static final String SELECT_MISSION2 = "selectMission2";
    public static final String USERNAME_PLACEHOLDER = "__USERNAME__";
    public static final String POWERUP_USED = "powerupUsed";
    public static final String POWERUP_GOT = "powerupGot";
    public static final String POWERUP_PLACEHOLDER = "__POWERUP__";
    public static final List<String> COINS_FOUND = new ArrayList<>();

    public static final ContentLoader contentLoader = new ContentLoader();
    public static final Game game = LayoutManager.loadGame();

    static {
        COINS_FOUND.add("coins");
        COINS_FOUND.add("treasure");
    }
}
