package com.muffinsoft.alexa.skills.adventureisland.content;

import com.muffinsoft.alexa.skills.adventureisland.util.ContentLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * These constants are used both internally and in external configuration
 * files as keys.
 */
public class Constants {

    public static final String ROOT = "root";
    public static final String INTRO = "intro";
    public static final String COINS_TO_COLLECT = "coinsToCollect";
    public static final String SCENE_CONFIRM = "sceneConfirm";
    public static final String SCENE_FAIL = "sceneFail";
    public static final String ACTION_APPROVE = "actionApprove";
    public static final String ACTION_FAIL = "actionFail";
    public static final String COIN_NOT_PICKED = "coinNotPicked";
    public static final String MIN_OBSTACLES_TO_COIN = "minObstaclesToCoin";
    public static final String MAX_OBSTACLES_TO_COIN = "maxObstaclesToCoin";
    public static final String COUNT = "Count";
    public static final String YOU_HAVE = "youHave";
    public static final String COIN_SINGLE = "coinSingle";
    public static final String COIN_PLURAL = "coinPlural";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String USERNAME_PLACEHOLDER = "__USERNAME__";
    public static final List<String> COINS_FOUND = new ArrayList<>();

    public static final ContentLoader contentLoader = new ContentLoader();

    static {
        COINS_FOUND.add("coins");
        COINS_FOUND.add("treasure");
    }

    /**
     * Database name for DynamoDB.
     */
    public static final String TABLE_NAME = "adventure_island";
}
